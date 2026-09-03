package com.example.data.remote

import android.content.Context
import android.util.Log
import com.example.data.local.entity.Budget
import com.example.data.local.entity.Transaction
import com.example.data.repository.BudgetRepository
import com.example.data.repository.TransactionRepository
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.http.ByteArrayContent
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.util.Collections

@Serializable
data class BackupData(
    val transactions: List<Transaction>,
    val budgets: List<Budget>
)

class DriveBackupManager(
    private val context: Context,
    private val transactionRepository: TransactionRepository,
    private val budgetRepository: BudgetRepository
) {
    private val json = Json { ignoreUnknownKeys = true; prettyPrint = true }
    private val FILE_NAME = "spendwise_backup.json"
    private val FOLDER_NAME = "DO NOT DELETE -> SPEND WISE DATA"
    private val TAG = "DriveBackupManager"

    private fun getDriveService(account: GoogleSignInAccount): Drive {
        val credential = GoogleAccountCredential.usingOAuth2(
            context, Collections.singleton(DriveScopes.DRIVE_FILE)
        )
        credential.selectedAccount = account.account ?: android.accounts.Account(account.email ?: "", "com.google")
        
        return Drive.Builder(
            NetHttpTransport(),
            GsonFactory.getDefaultInstance(),
            credential
        ).setApplicationName("SpendWise").build()
    }

    private fun getOrCreateBackupFolder(driveService: Drive): String {
        val fileList = driveService.files().list()
            .setSpaces("drive")
            .setQ("mimeType='application/vnd.google-apps.folder' and name='$FOLDER_NAME' and trashed=false")
            .execute()
            
        if (!fileList.files.isNullOrEmpty()) {
            return fileList.files[0].id
        }
        
        val folderMetadata = com.google.api.services.drive.model.File()
        folderMetadata.name = FOLDER_NAME
        folderMetadata.mimeType = "application/vnd.google-apps.folder"
        
        val folder = driveService.files().create(folderMetadata)
            .setFields("id")
            .execute()
        return folder.id
    }

    suspend fun backupData(account: GoogleSignInAccount, userId: Int): Boolean = withContext(Dispatchers.IO) {
        try {
            val transactions = transactionRepository.getAllTransactionsFlow(userId).first()
            val budgets = budgetRepository.getAllBudgetsFlow(userId).first()
            
            val backupData = BackupData(transactions, budgets)
            val jsonString = json.encodeToString(backupData)
            val byteArrayContent = ByteArrayContent.fromString("application/json", jsonString)

            val driveService = getDriveService(account)
            val folderId = getOrCreateBackupFolder(driveService)
            
            // Check if file exists in the folder
            val fileList = driveService.files().list()
                .setSpaces("drive")
                .setQ("name='$FILE_NAME' and trashed=false and '$folderId' in parents")
                .execute()
                
            if (!fileList.files.isNullOrEmpty()) {
                val fileId = fileList.files[0].id
                driveService.files().update(fileId, null, byteArrayContent).execute()
                Log.d(TAG, "Updated existing backup file on Drive")
            } else {
                val fileMetadata = com.google.api.services.drive.model.File()
                fileMetadata.name = FILE_NAME
                fileMetadata.parents = listOf(folderId)
                driveService.files().create(fileMetadata, byteArrayContent).execute()
                Log.d(TAG, "Created new backup file on Drive")
            }
            true
        } catch (e: Exception) {
            Log.e(TAG, "Backup failed: ${e.message}", e)
            false
        }
    }

    suspend fun restoreData(account: GoogleSignInAccount, userId: Int): Boolean = withContext(Dispatchers.IO) {
        try {
            val driveService = getDriveService(account)
            
            val folderList = driveService.files().list()
                .setSpaces("drive")
                .setQ("mimeType='application/vnd.google-apps.folder' and name='$FOLDER_NAME' and trashed=false")
                .execute()
                
            if (folderList.files.isNullOrEmpty()) {
                Log.d(TAG, "No backup folder found on Drive")
                return@withContext false
            }
            
            val folderId = folderList.files[0].id
            
            val fileList = driveService.files().list()
                .setSpaces("drive")
                .setQ("name='$FILE_NAME' and trashed=false and '$folderId' in parents")
                .execute()
                
            if (!fileList.files.isNullOrEmpty()) {
                val fileId = fileList.files[0].id
                val outputStream = ByteArrayOutputStream()
                driveService.files().get(fileId).executeMediaAndDownloadTo(outputStream)
                
                val jsonString = outputStream.toString("UTF-8")
                val backupData = json.decodeFromString<BackupData>(jsonString)
                
                // Clear existing and restore
                // Wait, it might be safer to insert them.
                // Assuming IDs are correct, or we ignore IDs and insert new ones.
                // Actually, restoring replaces local data or merges it. Let's merge or insert.
                backupData.transactions.forEach {
                    // Update user ID to current user if different
                    transactionRepository.insertTransaction(it.copy(id = 0, userId = userId))
                }
                backupData.budgets.forEach {
                    budgetRepository.insertBudget(it.copy(id = 0, userId = userId))
                }
                
                Log.d(TAG, "Successfully restored data from Drive")
                true
            } else {
                Log.d(TAG, "No backup file found on Drive")
                false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Restore failed: ${e.message}", e)
            false
        }
    }

    suspend fun backupDataForCurrentUser(userId: Int): Boolean {
        val account = com.google.android.gms.auth.api.signin.GoogleSignIn.getLastSignedInAccount(context)
        return if (account != null) {
            backupData(account, userId)
        } else {
            Log.d(TAG, "Skipping Drive Backup: No Google account signed in (likely Guest mode)")
            false
        }
    }
}

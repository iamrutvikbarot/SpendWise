package com.example.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.DataStoreManager
import com.example.data.local.entity.User
import com.example.data.repository.TransactionRepository
import com.example.data.repository.UserRepository
import com.example.data.remote.DriveBackupManager
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    data class Success(val user: User) : AuthState()
    data class Error(val message: String) : AuthState()
}

class AuthViewModel(
    private val userRepository: UserRepository,
    private val transactionRepository: TransactionRepository,
    private val dataStoreManager: DataStoreManager,
    private val driveBackupManager: DriveBackupManager
) : ViewModel() {
    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState

    fun loginWithGoogle(account: GoogleSignInAccount) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                val email = account.email ?: ""
                val fullName = account.displayName ?: "User"
                
                if (email.isBlank()) {
                    _authState.value = AuthState.Error("Email is required from Google account")
                    return@launch
                }

                // Check if user exists
                var user = userRepository.getUserByEmail(email)
                var isNewUser = false
                
                if (user == null) {
                    // Create new user for google sign in
                    val newUser = User(
                        fullName = fullName,
                        email = email,
                        pinHash = "GOOGLE_AUTH" // Or we could make pin nullable, but this works for now
                    )
                    val userId = userRepository.insertUser(newUser).toInt()
                    user = newUser.copy(id = userId)
                    isNewUser = true
                }

                // Restore data from Drive if user has no local transactions
                val currentTransactions = transactionRepository.getAllTransactionsFlow(user.id).first()
                if (currentTransactions.isEmpty()) {
                    try {
                        driveBackupManager.restoreData(account, user.id)
                    } catch (e: Exception) {
                        e.printStackTrace()
                        // Proceed to success even if restore fails
                    }
                }

                dataStoreManager.saveLoginState(true, user.id)
                _authState.value = AuthState.Success(user)
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "Login failed")
            }
        }
    }

    fun setError(message: String) {
        _authState.value = AuthState.Error(message)
    }

    fun resetState() {
        _authState.value = AuthState.Idle
    }
}

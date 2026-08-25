package com.example.ui.transactions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.DataStoreManager
import com.example.data.local.entity.Transaction
import com.example.data.repository.TransactionRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class TransactionViewModel(
    private val transactionRepository: TransactionRepository,
    private val dataStoreManager: DataStoreManager
) : ViewModel() {
    fun addTransaction(amount: Double, title: String, isExpense: Boolean, category: String, paymentMethod: String, onComplete: () -> Unit) {
        viewModelScope.launch {
            val userId = dataStoreManager.userId.first() ?: return@launch
            val cleanTitle = title.replace("\r", " ").replace("\n", " ").trim().replace(Regex("\\s+"), " ").ifBlank { category }
            val cleanCategory = category.replace("\r", " ").replace("\n", " ").trim().ifBlank { "Other" }
            val cleanPayment = paymentMethod.replace("\r", " ").replace("\n", " ").trim().ifBlank { "UPI" }
            val transaction = Transaction(
                userId = userId,
                type = if (isExpense) "EXPENSE" else "INCOME",
                amount = amount,
                category = cleanCategory,
                title = cleanTitle,
                paymentMethod = cleanPayment,
                date = System.currentTimeMillis()
            )
            transactionRepository.insertTransaction(transaction)
            onComplete()
        }
    }
}

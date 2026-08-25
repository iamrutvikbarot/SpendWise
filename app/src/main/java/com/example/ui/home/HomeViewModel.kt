package com.example.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.DataStoreManager
import com.example.data.local.entity.Transaction
import com.example.data.local.entity.User
import com.example.data.repository.TransactionRepository
import com.example.data.repository.UserRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class HomeViewModel(
    private val userRepository: UserRepository,
    private val transactionRepository: TransactionRepository,
    private val dataStoreManager: DataStoreManager
) : ViewModel() {
    private val _user = MutableStateFlow<User?>(null)
    val user: StateFlow<User?> = _user

    private val _transactions = MutableStateFlow<List<Transaction>>(emptyList())
    val transactions: StateFlow<List<Transaction>> = _transactions

    val totalIncome = _transactions.map { list -> list.filter { !it.type.equals("EXPENSE", ignoreCase = true) }.sumOf { it.amount } }
        .stateIn(viewModelScope, SharingStarted.Lazily, 0.0)

    val totalExpense = _transactions.map { list -> list.filter { it.type.equals("EXPENSE", ignoreCase = true) }.sumOf { it.amount } }
        .stateIn(viewModelScope, SharingStarted.Lazily, 0.0)

    val totalBalance = combine(totalIncome, totalExpense) { inc, exp -> inc - exp }
        .stateIn(viewModelScope, SharingStarted.Lazily, 0.0)

    val monthlyBudget = dataStoreManager.monthlyBudget
        .stateIn(viewModelScope, SharingStarted.Lazily, 0f)

    fun setMonthlyBudget(amount: Float) {
        viewModelScope.launch {
            dataStoreManager.setMonthlyBudget(amount)
        }
    }

    fun deleteTransaction(transaction: Transaction) {
        viewModelScope.launch {
            transactionRepository.deleteTransaction(transaction)
        }
    }

    init {
        viewModelScope.launch {
            dataStoreManager.userId.filterNotNull().collect { uid ->
                userRepository.getUserByIdFlow(uid).collect { _user.value = it }
            }
        }
        viewModelScope.launch {
            dataStoreManager.userId.filterNotNull().collect { uid ->
                transactionRepository.getAllTransactionsFlow(uid).collect { _transactions.value = it }
            }
        }
    }
}

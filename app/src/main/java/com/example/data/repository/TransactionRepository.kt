package com.example.data.repository

import com.example.data.local.dao.TransactionDao
import com.example.data.local.entity.Transaction
import kotlinx.coroutines.flow.Flow

class TransactionRepository(private val transactionDao: TransactionDao) {
    fun getAllTransactionsFlow(userId: Int): Flow<List<Transaction>> = transactionDao.getAllTransactionsFlow(userId)

    fun getExpensesFlow(userId: Int): Flow<List<Transaction>> = transactionDao.getExpensesFlow(userId)

    fun getIncomesFlow(userId: Int): Flow<List<Transaction>> = transactionDao.getIncomesFlow(userId)

    suspend fun insertTransaction(transaction: Transaction) = transactionDao.insertTransaction(transaction)

    suspend fun updateTransaction(transaction: Transaction) = transactionDao.updateTransaction(transaction)

    suspend fun deleteTransaction(transaction: Transaction) = transactionDao.deleteTransaction(transaction)
}

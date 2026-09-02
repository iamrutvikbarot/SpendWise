package com.example.data.repository

import com.example.data.local.dao.BudgetDao
import com.example.data.local.entity.Budget
import kotlinx.coroutines.flow.Flow

class BudgetRepository(private val budgetDao: BudgetDao) {
    fun getBudgetsForMonthFlow(userId: Int, month: Int, year: Int): Flow<List<Budget>> = 
        budgetDao.getBudgetsForMonthFlow(userId, month, year)

    fun getAllBudgetsFlow(userId: Int): Flow<List<Budget>> = budgetDao.getAllBudgetsFlow(userId)

    suspend fun insertBudget(budget: Budget) = budgetDao.insertBudget(budget)

    suspend fun updateBudget(budget: Budget) = budgetDao.updateBudget(budget)
}

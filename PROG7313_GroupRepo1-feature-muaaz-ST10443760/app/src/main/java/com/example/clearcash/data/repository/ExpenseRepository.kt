package com.example.clearcash.data.repository

import androidx.lifecycle.LiveData
import com.example.clearcash.data.db.dao.ExpenseDao
import com.example.clearcash.data.db.entities.Expense

/**
 * Repository for Expense data operations.
 * Abstracts database access from the ViewModel layer.
 * Author: Muaaz Abdool Gaffoor (ST10443760)
 */
class ExpenseRepository(private val expenseDao: ExpenseDao) {

    // LiveData of all expenses
    val allExpenses: LiveData<List<Expense>> = expenseDao.getAllExpenses()

    // Insert a new expense
    suspend fun insertExpense(expense: Expense) {
        expenseDao.insertExpense(expense)
    }

    // Delete an expense
    suspend fun deleteExpense(expense: Expense) {
        expenseDao.deleteExpense(expense)
    }

    // Get expenses filtered by date range
    fun getExpensesByDateRange(startDate: Long, endDate: Long): LiveData<List<Expense>> {
        return expenseDao.getExpensesByDateRange(startDate, endDate)
    }

    // Get expenses for a specific category
    fun getExpensesByCategory(categoryId: Int): LiveData<List<Expense>> {
        return expenseDao.getExpensesByCategory(categoryId)
    }

    // Get total spent per category in a date range
    fun getCategoryTotals(
        startDate: Long,
        endDate: Long
    ): LiveData<List<ExpenseDao.CategoryTotal>> {
        return expenseDao.getCategoryTotals(startDate, endDate)
    }
}
package com.example.clearcash.ui.expense

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.clearcash.data.db.AppDatabase
import com.example.clearcash.data.db.dao.ExpenseDao
import com.example.clearcash.data.db.entities.Expense
import com.example.clearcash.data.repository.ExpenseRepository
import kotlinx.coroutines.launch

/**
 * ViewModel for Expense screens.
 * Handles expense insertion, deletion and date-range filtering.
 * Author: Muaaz Abdool Gaffoor (ST10443760)
 */
class ExpenseViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: ExpenseRepository
    val allExpenses: LiveData<List<Expense>>

    init {
        val expenseDao = AppDatabase.getDatabase(application).expenseDao()
        repository = ExpenseRepository(expenseDao)
        allExpenses = repository.allExpenses
    }

    // Insert a new expense entry
    fun insertExpense(expense: Expense) = viewModelScope.launch {
        repository.insertExpense(expense)
    }

    // Delete an expense entry
    fun deleteExpense(expense: Expense) = viewModelScope.launch {
        repository.deleteExpense(expense)
    }

    // Get expenses filtered by date range
    fun getExpensesByDateRange(startDate: Long, endDate: Long): LiveData<List<Expense>> {
        return repository.getExpensesByDateRange(startDate, endDate)
    }

    // Get category totals for a date range
    fun getCategoryTotals(
        startDate: Long,
        endDate: Long
    ): LiveData<List<ExpenseDao.CategoryTotal>> {
        return repository.getCategoryTotals(startDate, endDate)
    }
}
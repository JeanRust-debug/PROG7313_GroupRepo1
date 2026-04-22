package com.example.clearcash.ui.reports

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.switchMap
import com.example.clearcash.data.db.AppDatabase
import com.example.clearcash.data.db.dao.ExpenseDao
import com.example.clearcash.data.db.entities.Expense
import com.example.clearcash.data.repository.ExpenseRepository


class ReportsViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: ExpenseRepository = ExpenseRepository(AppDatabase.getDatabase(application).expenseDao())

    // Data class representing the user-selected from/to date range in millis
    data class DateRange(val start: Long, val end: Long)

    // Backing LiveData for the current selected range — drives the queries below
    private val _dateRange = MutableLiveData<DateRange>()
    val dateRange: LiveData<DateRange> get() = _dateRange

    // Filtered expense list — automatically swaps source when the date range changes
    val filteredExpenses: LiveData<List<Expense>> = _dateRange.switchMap { range ->
        repository.getExpensesByDateRange(range.start, range.end)
    }

    // Category totals — automatically swaps source when the date range changes
    val categoryTotals: LiveData<List<ExpenseDao.CategoryTotal>> =
        _dateRange.switchMap { range ->
            repository.getCategoryTotals(range.start, range.end)
        }



    fun setDateRange(startMillis: Long, endMillis: Long) {
        _dateRange.value = DateRange(startMillis, endMillis)
    }
}
package com.example.clearcash.data.db.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.clearcash.data.db.entities.Expense

/**
 * DAO for Expense database operations.
 * Supports insert, delete, and date-range filtered queries.
 * Author: Muaaz Abdool Gaffoor (ST10443760)
 */
@Dao
interface ExpenseDao {

    // Insert a new expense entry
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpense(expense: Expense)

    // Delete a specific expense
    @Delete
    suspend fun deleteExpense(expense: Expense)

    // Get all expenses as LiveData
    @Query("SELECT * FROM expenses ORDER BY date DESC")
    fun getAllExpenses(): LiveData<List<Expense>>

    // Get expenses filtered by date range (for the list view)
    @Query("SELECT * FROM expenses WHERE date BETWEEN :startDate AND :endDate ORDER BY date DESC")
    fun getExpensesByDateRange(startDate: Long, endDate: Long): LiveData<List<Expense>>

    // Get expenses for a specific category
    @Query("SELECT * FROM expenses WHERE categoryId = :categoryId ORDER BY date DESC")
    fun getExpensesByCategory(categoryId: Int): LiveData<List<Expense>>

    // Get total spent per category within a date range (for category totals view)
    @Query("""
        SELECT categoryId, SUM(amount) as total 
        FROM expenses 
        WHERE date BETWEEN :startDate AND :endDate 
        GROUP BY categoryId
    """)
    fun getCategoryTotals(startDate: Long, endDate: Long): LiveData<List<CategoryTotal>>

    // Data class for category total query result
    data class CategoryTotal(
        val categoryId: Int,
        val total: Double
    )
}
package com.example.prog7313.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import java.util.Date

@Dao
interface ExpenseDao {
    @Insert
    suspend fun insertExpense(expense: Expense): Long

    @Query("""
        SELECT * FROM expenses 
        WHERE userId = :userId 
        AND date BETWEEN :startDate AND :endDate
        ORDER BY date DESC, startTime DESC
    """)
    suspend fun getExpensesInPeriod(
        userId: Int,
        startDate: Date,
        endDate: Date
    ): List<Expense>

    @Query("""
        SELECT categoryId, SUM(amount) as total 
        FROM expenses 
        WHERE userId = :userId 
        AND date BETWEEN :startDate AND :endDate
        GROUP BY categoryId
    """)
    suspend fun getCategoryTotalsInPeriod(
        userId: Int,
        startDate: Date,
        endDate: Date
    ): List<CategoryTotal>

    data class CategoryTotal(
        val categoryId: Int,
        val total: Double
    )
}
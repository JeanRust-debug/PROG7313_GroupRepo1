package com.clearcash.app.data.db.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.clearcash.app.data.db.entities.Expense

// Returned by the GROUP BY query used in the dashboard and graph
data class CategoryTotal(val categoryId: Long?, val total: Double)

@Dao
interface ExpenseDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(expense: Expense): Long

    @Update  suspend fun update(expense: Expense)
    @Delete  suspend fun delete(expense: Expense)

    @Query("SELECT * FROM expenses WHERE userId = :userId ORDER BY date DESC")
    fun getExpensesByUser(userId: Long): LiveData<List<Expense>>

    @Query("SELECT * FROM expenses WHERE userId = :userId AND date BETWEEN :startDate AND :endDate ORDER BY date DESC")
    fun getExpensesByUserAndPeriod(userId: Long, startDate: Long, endDate: Long): LiveData<List<Expense>>

    @Query("SELECT * FROM expenses WHERE userId = :userId AND date BETWEEN :startDate AND :endDate ORDER BY date DESC")
    suspend fun getExpensesByUserAndPeriodSync(userId: Long, startDate: Long, endDate: Long): List<Expense>

    @Query("SELECT SUM(amount) FROM expenses WHERE userId = :userId AND categoryId = :categoryId AND date BETWEEN :startDate AND :endDate")
    suspend fun getTotalByCategory(userId: Long, categoryId: Long, startDate: Long, endDate: Long): Double?

    @Query("SELECT SUM(amount) FROM expenses WHERE userId = :userId AND date BETWEEN :startDate AND :endDate")
    suspend fun getTotalByPeriod(userId: Long, startDate: Long, endDate: Long): Double?

    @Query("SELECT * FROM expenses WHERE id = :id LIMIT 1")
    suspend fun getExpenseById(id: Long): Expense?

    @Query("SELECT categoryId, SUM(amount) as total FROM expenses WHERE userId = :userId AND date BETWEEN :startDate AND :endDate GROUP BY categoryId")
    suspend fun getCategoryTotals(userId: Long, startDate: Long, endDate: Long): List<CategoryTotal>
}
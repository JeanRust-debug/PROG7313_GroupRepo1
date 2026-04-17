package com.clearcash.app.data.db.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.clearcash.app.data.db.entities.Budget

@Dao
interface BudgetDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(budget: Budget): Long

    @Update suspend fun update(budget: Budget)
    @Delete suspend fun delete(budget: Budget)

    @Query("SELECT * FROM budgets WHERE userId = :userId AND month = :month AND year = :year LIMIT 1")
    suspend fun getBudgetByMonth(userId: Long, month: Int, year: Int): Budget?

    @Query("SELECT * FROM budgets WHERE userId = :userId AND month = :month AND year = :year LIMIT 1")
    fun getBudgetByMonthLive(userId: Long, month: Int, year: Int): LiveData<Budget?>

    @Query("SELECT * FROM budgets WHERE userId = :userId ORDER BY year DESC, month DESC LIMIT 1")
    suspend fun getLatestBudget(userId: Long): Budget?
}
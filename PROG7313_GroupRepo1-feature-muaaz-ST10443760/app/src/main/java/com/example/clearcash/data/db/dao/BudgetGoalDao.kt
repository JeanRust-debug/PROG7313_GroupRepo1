package com.example.clearcash.data.db.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.clearcash.data.db.entities.BudgetGoal

/**
 * DAO for BudgetGoal database operations.
 * Handles inserting and retrieving monthly budget goals.
 * Author: Muaaz Abdool Gaffoor (ST10443760)
 */
@Dao
interface BudgetGoalDao {

    // Insert or update a budget goal for a month
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBudgetGoal(budgetGoal: BudgetGoal)

    // Get the budget goal for a specific month e.g. "2026-04"
    @Query("SELECT * FROM budget_goals WHERE month = :month LIMIT 1")
    fun getBudgetGoalForMonth(month: String): LiveData<BudgetGoal?>

    // Get all budget goals
    @Query("SELECT * FROM budget_goals ORDER BY month DESC")
    fun getAllBudgetGoals(): LiveData<List<BudgetGoal>>

    // Delete a specific budget goal
    @Delete
    suspend fun deleteBudgetGoal(budgetGoal: BudgetGoal)
}
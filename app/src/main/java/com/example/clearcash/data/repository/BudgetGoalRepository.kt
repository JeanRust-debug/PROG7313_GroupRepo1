package com.example.clearcash.data.repository

import androidx.lifecycle.LiveData
import com.example.clearcash.data.db.dao.BudgetGoalDao
import com.example.clearcash.data.db.entities.BudgetGoal

/**
 * Repository for BudgetGoal data operations.
 * Separates database logic from the ViewModel.
 * Author: Muaaz Abdool Gaffoor (ST10443760)
 */
class BudgetGoalRepository(private val budgetGoalDao: BudgetGoalDao) {

    // Get budget goal for a specific month
    fun getBudgetGoalForMonth(month: String): LiveData<BudgetGoal?> {
        return budgetGoalDao.getBudgetGoalForMonth(month)
    }

    // Get all budget goals
    val allBudgetGoals: LiveData<List<BudgetGoal>> = budgetGoalDao.getAllBudgetGoals()

    // Insert or update a budget goal
    suspend fun insertBudgetGoal(budgetGoal: BudgetGoal) {
        budgetGoalDao.insertBudgetGoal(budgetGoal)
    }

    // Delete a budget goal
    suspend fun deleteBudgetGoal(budgetGoal: BudgetGoal) {
        budgetGoalDao.deleteBudgetGoal(budgetGoal)
    }
}
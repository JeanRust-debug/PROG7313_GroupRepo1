package com.example.clearcash.ui.budget

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.example.clearcash.data.db.AppDatabase
import com.example.clearcash.data.db.entities.BudgetGoal
import com.example.clearcash.data.repository.BudgetGoalRepository
import kotlinx.coroutines.launch

/**
 * ViewModel for Budget Goal screens.
 * Exposes budget goal data and handles save operations.
 * Author: Muaaz Abdool Gaffoor (ST10443760)
 */
class BudgetGoalViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: BudgetGoalRepository
    val allBudgetGoals: LiveData<List<BudgetGoal>>

    init {
        val budgetGoalDao = AppDatabase.getDatabase(application).budgetGoalDao()
        repository = BudgetGoalRepository(budgetGoalDao)
        allBudgetGoals = repository.allBudgetGoals
    }

    // Get budget goal for a specific month e.g. "2026-04"
    fun getBudgetGoalForMonth(month: String): LiveData<BudgetGoal?> {
        return repository.getBudgetGoalForMonth(month)
    }

    // Insert or update a budget goal
    fun insertBudgetGoal(budgetGoal: BudgetGoal) = viewModelScope.launch {
        repository.insertBudgetGoal(budgetGoal)
    }

    // Delete a budget goal
    fun deleteBudgetGoal(budgetGoal: BudgetGoal) = viewModelScope.launch {
        repository.deleteBudgetGoal(budgetGoal)
    }
}
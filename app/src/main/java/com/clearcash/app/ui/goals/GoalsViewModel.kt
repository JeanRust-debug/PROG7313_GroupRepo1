package com.clearcash.app.ui.goals

import androidx.lifecycle.*
import com.clearcash.app.data.db.entities.SavingsGoal
import com.clearcash.app.data.repository.ClearCashRepository
import kotlinx.coroutines.launch

class GoalsViewModel(private val repo: ClearCashRepository) : ViewModel() {

    private val _saveResult = MutableLiveData<Result<Long>>()
    val saveResult: LiveData<Result<Long>> = _saveResult

    fun getGoals(userId: Long): LiveData<List<SavingsGoal>> = repo.getGoals(userId)

    fun addGoal(name: String, target: Double, userId: Long) = viewModelScope.launch {
        try {
            val id = repo.addGoal(SavingsGoal(userId = userId, name = name, targetAmount = target))
            _saveResult.postValue(Result.success(id))
        } catch (e: Exception) {
            _saveResult.postValue(Result.failure(e))
        }
    }

    fun addFunds(goal: SavingsGoal, amount: Double) = viewModelScope.launch {
        repo.applyToGoal(goal, amount)
    }

    fun deleteGoal(goal: SavingsGoal) = viewModelScope.launch {
        repo.deleteGoal(goal)
    }

    class Factory(private val repo: ClearCashRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(c: Class<T>): T = GoalsViewModel(repo) as T
    }
}

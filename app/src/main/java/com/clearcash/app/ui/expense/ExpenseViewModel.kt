package com.clearcash.app.ui.expense

import androidx.lifecycle.*
import com.clearcash.app.data.db.entities.Category
import com.clearcash.app.data.db.entities.Expense
import com.clearcash.app.data.repository.ClearCashRepository
import kotlinx.coroutines.launch

class ExpenseViewModel(private val repo: ClearCashRepository) : ViewModel() {

    private val _categories  = MutableLiveData<List<Category>>()
    val categories: LiveData<List<Category>> = _categories

    private val _saveResult  = MutableLiveData<Result<Long>>()
    val saveResult: LiveData<Result<Long>> = _saveResult

    private val _detail      = MutableLiveData<Expense?>()
    val detail: LiveData<Expense?> = _detail

    fun loadCategories(userId: Long) = viewModelScope.launch {
        _categories.postValue(repo.getCategoriesByUserSync(userId))
    }

    fun getByPeriod(userId: Long, start: Long, end: Long) =
        repo.getExpensesByPeriod(userId, start, end)

    fun save(expense: Expense) = viewModelScope.launch {
        try { _saveResult.postValue(Result.success(repo.addExpense(expense))) }
        catch (e: Exception) { _saveResult.postValue(Result.failure(e)) }
    }

    fun delete(expense: Expense) = viewModelScope.launch { repo.deleteExpense(expense) }

    fun loadById(id: Long) = viewModelScope.launch {
        _detail.postValue(repo.getExpenseById(id))
    }

    class Factory(private val repo: ClearCashRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(c: Class<T>): T = ExpenseViewModel(repo) as T
    }
}
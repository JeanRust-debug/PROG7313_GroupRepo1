package com.clearcash.app.ui.category

import androidx.lifecycle.*
import com.clearcash.app.data.db.entities.Category
import com.clearcash.app.data.repository.ClearCashRepository
import kotlinx.coroutines.launch

class CategoryViewModel(private val repo: ClearCashRepository) : ViewModel() {

    private val _saveResult = MutableLiveData<Result<Long>>()
    val saveResult: LiveData<Result<Long>> = _saveResult

    fun getCategories(userId: Long): LiveData<List<Category>> = repo.getCategoriesByUser(userId)

    fun add(userId: Long, name: String, limit: Double) = viewModelScope.launch {
        _saveResult.postValue(repo.addCategory(userId, name, limit))
    }

    fun delete(cat: Category) = viewModelScope.launch { repo.deleteCategory(cat) }

    class Factory(private val repo: ClearCashRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(c: Class<T>): T = CategoryViewModel(repo) as T
    }
}
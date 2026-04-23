package com.example.clearcash.ui.category

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.example.clearcash.data.db.AppDatabase
import com.example.clearcash.data.db.entities.Category
import com.example.clearcash.data.repository.CategoryRepository
import kotlinx.coroutines.launch

/**
 * ViewModel for Category screens.
 * Survives configuration changes and exposes LiveData to the UI.
 * Author: Muaaz Abdool Gaffoor (ST10443760)
 */
class CategoryViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: CategoryRepository
    val allCategories: LiveData<List<Category>>

    init {
        // Initialise repository with the DAO from the database singleton
        val categoryDao = AppDatabase.getDatabase(application).categoryDao()
        repository = CategoryRepository(categoryDao)
        allCategories = repository.allCategories
    }

    // Insert category — launches on background coroutine
    fun insertCategory(category: Category) = viewModelScope.launch {
        repository.insertCategory(category)
    }

    // Delete category — launches on background coroutine
    fun deleteCategory(category: Category) = viewModelScope.launch {
        repository.deleteCategory(category)
    }
}
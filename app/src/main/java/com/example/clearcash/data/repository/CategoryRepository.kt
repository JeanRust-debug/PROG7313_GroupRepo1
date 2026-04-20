package com.example.clearcash.data.repository

import androidx.lifecycle.LiveData
import com.example.clearcash.data.db.dao.CategoryDao
import com.example.clearcash.data.db.entities.Category

/**
 * Repository for Category data operations.
 * Acts as the single source of truth between ViewModel and Database.
 * Author: Muaaz Abdool Gaffoor (ST10443760)
 */
class CategoryRepository(private val categoryDao: CategoryDao) {

    // LiveData list of all categories, auto-updates UI on change
    val allCategories: LiveData<List<Category>> = categoryDao.getAllCategories()

    // Insert a new category on a background coroutine
    suspend fun insertCategory(category: Category) {
        categoryDao.insertCategory(category)
    }

    // Delete a category on a background coroutine
    suspend fun deleteCategory(category: Category) {
        categoryDao.deleteCategory(category)
    }

    // Get a single category by ID
    suspend fun getCategoryById(id: Int): Category? {
        return categoryDao.getCategoryById(id)
    }
}
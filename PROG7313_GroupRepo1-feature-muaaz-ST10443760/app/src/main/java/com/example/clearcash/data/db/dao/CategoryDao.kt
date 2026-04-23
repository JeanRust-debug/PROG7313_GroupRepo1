package com.example.clearcash.data.db.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.clearcash.data.db.entities.Category

/**
 * DAO for Category database operations.
 * Provides methods to insert, delete and observe categories.
 * Author: Muaaz Abdool Gaffoor (ST10443760)
 */
@Dao
interface CategoryDao {

    // Insert a new category, replace if conflict
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategory(category: Category)

    // Delete a specific category
    @Delete
    suspend fun deleteCategory(category: Category)

    // Get all categories as LiveData so UI updates automatically
    @Query("SELECT * FROM categories ORDER BY name ASC")
    fun getAllCategories(): LiveData<List<Category>>

    // Get a single category by ID
    @Query("SELECT * FROM categories WHERE id = :id")
    suspend fun getCategoryById(id: Int): Category?
}
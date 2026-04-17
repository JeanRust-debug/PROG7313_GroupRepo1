package com.example.prog7313.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Delete

@Dao
interface CategoryDao {
    @Insert
    suspend fun insertCategory(category: Category): Long

    @Query("SELECT * FROM categories WHERE userId = :userId ORDER BY name")
    suspend fun getAllCategoriesForUser(userId: Int): List<Category>

    @Delete
    suspend fun deleteCategory(category: Category)
}
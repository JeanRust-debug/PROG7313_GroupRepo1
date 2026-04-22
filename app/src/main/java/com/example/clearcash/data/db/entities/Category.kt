package com.example.clearcash.data.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entity representing a spending category in the ClearCash app.
 * Each category has a name and an optional spending limit.
 * Author: Muaaz Abdool Gaffoor (ST10443760)
 */
@Entity(tableName = "categories")
data class Category(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    // Name of the category e.g. Groceries, Transport
    val name: String,

    // Optional monthly spending limit for this category
    val limit: Double = 0.0
)
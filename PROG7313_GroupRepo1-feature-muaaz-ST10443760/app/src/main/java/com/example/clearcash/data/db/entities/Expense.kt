package com.example.clearcash.data.db.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

/**
 * Entity representing a single expense entry in ClearCash.
 * Linked to a Category via foreign key.
 * Author: Muaaz Abdool Gaffoor (ST10443760)
 */
@Entity(
    tableName = "expenses",
    foreignKeys = [ForeignKey(
        entity = Category::class,
        parentColumns = ["id"],
        childColumns = ["categoryId"],
        onDelete = ForeignKey.CASCADE
    )]
)
data class Expense(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    // Amount spent in Rands
    val amount: Double,

    // Date of expense stored as a Long timestamp
    val date: Long,

    // Description of what was purchased
    val description: String,

    // Foreign key linking to Category
    val categoryId: Int,

    // Optional file path to receipt photo stored locally
    val photoPath: String? = null
)
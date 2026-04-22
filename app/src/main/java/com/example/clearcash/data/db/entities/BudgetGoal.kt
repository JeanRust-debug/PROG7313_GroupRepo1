package com.example.clearcash.data.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entity representing the user's monthly budget goals.
 * Stores minimum and maximum spending targets.
 * Author: Muaaz Abdool Gaffoor (ST10443760)
 */
@Entity(tableName = "budget_goals")
data class BudgetGoal(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    // The month this goal applies to (format: "YYYY-MM")
    val month: String,

    // Minimum amount the user wants to stay above
    val minGoal: Double,

    // Maximum amount the user wants to stay below
    val maxGoal: Double
)
package com.example.prog7313.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "expenses")
data class Expense(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val amount: Double,
    val date: Date,
    val startTime: String,
    val endTime: String,
    val description: String,
    val categoryId: Int,
    val userId: Int,
    val photoPath: String? = null
)
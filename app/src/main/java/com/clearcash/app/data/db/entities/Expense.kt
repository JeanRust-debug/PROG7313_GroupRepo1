package com.clearcash.app.data.db.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "expenses",
    foreignKeys = [
        ForeignKey(entity = User::class,     parentColumns = ["id"], childColumns = ["userId"],     onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = Category::class, parentColumns = ["id"], childColumns = ["categoryId"], onDelete = ForeignKey.SET_NULL)
    ],
    indices = [Index("userId"), Index("categoryId")]
)
data class Expense(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: Long,
    val categoryId: Long?,          // Nullable — can be uncategorized
    val amount: Double,
    val date: Long,                 // Unix timestamp ms
    val startTime: String = "",     // "HH:mm"
    val endTime: String   = "",     // "HH:mm"
    val description: String,
    val receiptPath: String? = null, // File path or URI string for photo
    val createdAt: Long = System.currentTimeMillis()
)
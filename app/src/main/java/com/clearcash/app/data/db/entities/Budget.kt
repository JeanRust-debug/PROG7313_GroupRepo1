package com.clearcash.app.data.db.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "budgets",
    foreignKeys = [ForeignKey(
        entity = User::class,
        parentColumns = ["id"],
        childColumns = ["userId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("userId")]
)
data class Budget(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: Long,
    val minGoal: Double,    // Minimum spending (savings floor) — set via SeekBar
    val maxGoal: Double,    // Maximum spending (budget ceiling) — set via SeekBar
    val month: Int,
    val year: Int,
    val updatedAt: Long = System.currentTimeMillis()
)
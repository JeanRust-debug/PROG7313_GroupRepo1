package com.clearcash.app.data.db.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.clearcash.app.data.db.entities.SavingsGoal

@Dao
interface SavingsGoalDao {

    @Query("SELECT * FROM savings_goals WHERE userId = :userId ORDER BY createdAt DESC")
    fun getGoalsByUser(userId: Long): LiveData<List<SavingsGoal>>

    @Query("SELECT * FROM savings_goals WHERE userId = :userId ORDER BY createdAt DESC")
    suspend fun getGoalsByUserOnce(userId: Long): List<SavingsGoal>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(goal: SavingsGoal): Long

    @Update
    suspend fun update(goal: SavingsGoal)

    @Delete
    suspend fun delete(goal: SavingsGoal)
}

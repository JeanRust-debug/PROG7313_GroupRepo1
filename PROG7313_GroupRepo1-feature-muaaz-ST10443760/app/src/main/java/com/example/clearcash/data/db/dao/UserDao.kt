package com.example.clearcash.data.db.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.clearcash.data.db.entities.User

/**
 * DAO for User database operations.
 * Handles registration and login queries.
 * Author: Diya Maharaj ST10327888
 */
@Dao
interface UserDao {

    // Register a new user — replace if conflict occurs
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun registerUser(user: User)

    // Get user by username for login validation
    @Query("SELECT * FROM users WHERE username = :username LIMIT 1")
    suspend fun getUserByUsername(username: String): User?

    // Get user by username and password for login
    @Query("SELECT * FROM users WHERE username = :username AND password = :password LIMIT 1")
    suspend fun login(username: String, password: String): User?

    // Check if username already exists (for registration validation)
    @Query("SELECT COUNT(*) FROM users WHERE username = :username")
    suspend fun isUsernameTaken(username: String): Int
}
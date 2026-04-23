package com.example.clearcash.data.repository

import com.example.clearcash.data.db.dao.UserDao
import com.example.clearcash.data.db.entities.User

/**
 * Repository for User authentication operations.
 * Handles login and registration logic.
 * Author: Diya Maharaj ST10327888
 */
class UserRepository(private val userDao: UserDao) {

    /**
     * Registers a new user.
     * @return true if registration successful, false if username taken
     */
    suspend fun registerUser(username: String, password: String, email: String): Boolean {
        // Check if username already exists
        val count = userDao.isUsernameTaken(username)
        if (count > 0) {
            return false // Username already taken
        }

        // Create and insert the new user
        val user = User(
            username = username,
            password = password,
            email = email
        )
        userDao.registerUser(user)
        return true
    }

    /**
     * Attempts to log in a user.
     * @return The User object if successful, null if credentials invalid
     */
    suspend fun loginUser(username: String, password: String): User? {
        return userDao.login(username, password)
    }

    /**
     * Gets a user by username.
     */
    suspend fun getUserByUsername(username: String): User? {
        return userDao.getUserByUsername(username)
    }
}
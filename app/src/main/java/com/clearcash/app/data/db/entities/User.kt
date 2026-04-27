package com.clearcash.app.data.db.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

// Author: Diya Maharaj ST10327888

@Entity(
    tableName = "users",
    indices = [
        Index(value = ["username"], unique = true), // Prevents duplicate usernames
        Index(value = ["email"], unique = true)      // Prevents duplicate emails
    ]
)
data class User(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,           // Auto-generated unique ID
    val username: String,                                         // User's login name
    val email: String,                                            // User's email address
    val passwordHash: String,   // SHA-256 hash — never plain text
    val createdAt: Long = System.currentTimeMillis()              // Account creation timestamp
)
package com.example.clearcash.data.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entity representing a registered user in ClearCash.
 * Stores login credentials and basic profile info.
 * Author: Diya Maharaj ST10327888
 */
@Entity(tableName = "users")
data class User(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    // Username chosen by the user (must be unique)
    val username: String,

    // Password stored as plain text for simplicity
    // In a production app, this should be hashed
    val password: String,

    // User's email address
    val email: String
)
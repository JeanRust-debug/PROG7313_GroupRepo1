package com.example.clearcash.util

import android.content.Context
import android.content.SharedPreferences

class SessionManager(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("user_session", Context.MODE_PRIVATE)

    // Save login state
    fun saveLogin(username: String, userId: Int) {
        prefs.edit().apply {
            putBoolean("IS_LOGGED_IN", true)
            putString("USERNAME", username)
            putInt("USER_ID", userId)
            apply()
        }
    }

    // Check if user is logged in
    fun isLoggedIn(): Boolean {
        return prefs.getBoolean("IS_LOGGED_IN", false)
    }

    // Get logged-in username
    fun getUsername(): String? {
        return prefs.getString("USERNAME", null)
    }

    // Get logged in user ID
    fun getUserId(): Int {
        return prefs.getInt("USER_ID", -1)
    }

    // Logout - clear saved data
    fun logout() {
        prefs.edit().clear().apply()
    }
}
package com.clearcash.app.utils

import android.content.Context

class SessionManager(context: Context) {
    private val prefs = context.getSharedPreferences("clearcash_session", Context.MODE_PRIVATE)

    fun saveSession(userId: Long, username: String) =
        prefs.edit().putLong("user_id", userId).putString("username", username)
            .putBoolean("logged_in", true).apply()

    fun getUserId(): Long    = prefs.getLong("user_id", -1L)
    fun getUsername(): String = prefs.getString("username", "User") ?: "User"
    fun isLoggedIn(): Boolean = prefs.getBoolean("logged_in", false)
    fun clearSession()        = prefs.edit().clear().apply()
}
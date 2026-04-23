package com.example.clearcash.ui.auth

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.clearcash.data.db.AppDatabase
import com.example.clearcash.data.db.entities.User
import com.example.clearcash.data.repository.UserRepository
import kotlinx.coroutines.launch

/**
 * ViewModel for Login and Register screens.
 * Handles authentication logic and exposes result states.
 * Author: Diya Maharaj ST10327888
 */
class AuthViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: UserRepository

    // LiveData for observing login/register results
    private val _loginResult = MutableLiveData<User?>()
    val loginResult: LiveData<User?> = _loginResult

    private val _registerResult = MutableLiveData<Boolean>()
    val registerResult: LiveData<Boolean> = _registerResult

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage

    init {
        val userDao = AppDatabase.getDatabase(application).userDao()
        repository = UserRepository(userDao)
    }

    /**
     * Attempts to log in the user with the given credentials.
     */
    fun login(username: String, password: String) = viewModelScope.launch {
        // Validate inputs first
        if (username.isBlank()) {
            _errorMessage.value = "Username is required"
            return@launch
        }
        if (password.isBlank()) {
            _errorMessage.value = "Password is required"
            return@launch
        }

        // Try to log in
        val user = repository.loginUser(username, password)
        if (user != null) {
            _loginResult.value = user
        } else {
            _errorMessage.value = "Invalid username or password"
        }
    }

    /**
     * Registers a new user with the given details.
     */
    fun register(username: String, password: String, email: String) = viewModelScope.launch {
        // Validate inputs
        if (username.isBlank()) {
            _errorMessage.value = "Username is required"
            return@launch
        }
        if (username.length < 3) {
            _errorMessage.value = "Username must be at least 3 characters"
            return@launch
        }
        if (password.isBlank()) {
            _errorMessage.value = "Password is required"
            return@launch
        }
        if (password.length < 4) {
            _errorMessage.value = "Password must be at least 4 characters"
            return@launch
        }
        if (email.isBlank() || !email.contains("@")) {
            _errorMessage.value = "Enter a valid email address"
            return@launch
        }

        // Try to register
        val success = repository.registerUser(username, password, email)
        if (success) {
            _registerResult.value = true
        } else {
            _errorMessage.value = "Username already taken"
        }
    }

    /**
     * Resets error message after it's been shown.
     */
    fun clearError() {
        _errorMessage.value = ""
    }

    /**
     * Resets login result after navigation.
     */
    fun clearLoginResult() {
        _loginResult.value = null
    }
}
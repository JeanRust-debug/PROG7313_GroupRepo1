package com.example.clearcash

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.clearcash.data.AppDatabase
import com.example.clearcash.data.User
import com.example.clearcash.databinding.ActivityRegisterBinding
import com.example.clearcash.util.SessionManager
import kotlinx.coroutines.launch

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private lateinit var sessionManager: SessionManager
    private lateinit var database: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)
        database = AppDatabase.getDatabase(this)

        binding.btnRegister.setOnClickListener {
            val username = binding.etUsername.text.toString().trim()
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()
            val confirmPassword = binding.etConfirmPassword.text.toString().trim()

            // Validation
            when {
                username.isEmpty() -> {
                    Toast.makeText(this, "Please enter username", Toast.LENGTH_SHORT).show()
                }
                username.length < 3 -> {
                    Toast.makeText(this, "Username must be at least 3 characters", Toast.LENGTH_SHORT).show()
                }
                email.isEmpty() -> {
                    Toast.makeText(this, "Please enter email", Toast.LENGTH_SHORT).show()
                }
                !email.contains("@") || !email.contains(".") -> {
                    Toast.makeText(this, "Please enter a valid email", Toast.LENGTH_SHORT).show()
                }
                password.isEmpty() -> {
                    Toast.makeText(this, "Please enter password", Toast.LENGTH_SHORT).show()
                }
                password.length < 6 -> {
                    Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show()
                }
                password != confirmPassword -> {
                    Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
                }
                else -> {
                    registerUser(username, email, password)
                }
            }
        }

        binding.tvLoginLink.setOnClickListener {
            finish() // Go back to login screen
        }
    }

    private fun registerUser(username: String, email: String, password: String) {
        lifecycleScope.launch {
            try {
                // Check if username already exists
                val existingUser = database.userDao().getUserByUsername(username)

                if (existingUser != null) {
                    Toast.makeText(this@RegisterActivity, "Username already taken", Toast.LENGTH_SHORT).show()
                    return@launch
                }

                // Create new user
                val user = User(
                    username = username,
                    password = password,
                    email = email
                )

                // Save to database
                database.userDao().insertUser(user)

                Toast.makeText(this@RegisterActivity, "Registration successful!", Toast.LENGTH_SHORT).show()

                // Go back to login screen
                finish()

            } catch (e: Exception) {
                Toast.makeText(this@RegisterActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
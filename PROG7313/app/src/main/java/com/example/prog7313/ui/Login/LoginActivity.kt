package com.example.prog7313.ui.Login

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.prog7313.PROG7313Application
import com.example.prog7313.R
import com.example.prog7313.data.User
import com.example.prog7313.ui.Main.MainActivity
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    private lateinit var usernameInput: EditText
    private lateinit var passwordInput: EditText
    private lateinit var loginButton: Button
    private lateinit var registerButton: Button

    private val database by lazy {
        (application as PROG7313Application).database
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        Log.d("LoginActivity", "Login screen created")

        usernameInput = findViewById(R.id.usernameInput)
        passwordInput = findViewById(R.id.passwordInput)
        loginButton = findViewById(R.id.loginButton)
        registerButton = findViewById(R.id.registerButton)

        loginButton.setOnClickListener {
            val username = usernameInput.text.toString().trim()
            val password = passwordInput.text.toString().trim()

            if (username.isNotEmpty() && password.isNotEmpty()) {
                loginUser(username, password)
            } else {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            }
        }

        registerButton.setOnClickListener {
            val username = usernameInput.text.toString().trim()
            val password = passwordInput.text.toString().trim()

            if (username.isNotEmpty() && password.isNotEmpty()) {
                registerUser(username, password)
            } else {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun loginUser(username: String, password: String) {
        lifecycleScope.launch {
            try {
                Log.d("LoginActivity", "Login attempt: $username")
                val user = database.userDao().login(username, password)

                if (user != null) {
                    val sharedPref = getSharedPreferences("app_prefs", MODE_PRIVATE)
                    sharedPref.edit().putInt("current_user_id", user.id).apply()

                    startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                    finish()
                    Toast.makeText(this@LoginActivity, "Welcome ${user.username}!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this@LoginActivity, "Invalid credentials", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e("LoginActivity", "Login error", e)
                Toast.makeText(this@LoginActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun registerUser(username: String, password: String) {
        lifecycleScope.launch {
            try {
                val existingUser = database.userDao().getUserByUsername(username)
                if (existingUser != null) {
                    Toast.makeText(this@LoginActivity, "Username exists", Toast.LENGTH_SHORT).show()
                    return@launch
                }

                val newUser = User(username = username, password = password)
                database.userDao().insertUser(newUser)

                Toast.makeText(this@LoginActivity, "Registered! Please login", Toast.LENGTH_SHORT).show()
                usernameInput.text.clear()
                passwordInput.text.clear()
            } catch (e: Exception) {
                Log.e("LoginActivity", "Register error", e)
                Toast.makeText(this@LoginActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
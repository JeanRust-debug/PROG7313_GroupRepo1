package com.example.clearcash

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.clearcash.databinding.ActivityDashboardBinding
import com.example.clearcash.util.SessionManager

class DashboardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDashboardBinding
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)

        // Check if user is logged in
        if (!sessionManager.isLoggedIn()) {
            goToLogin()
            return
        }

        // Display welcome message with username
        val username = sessionManager.getUsername() ?: "User"
        binding.tvWelcome.text = "Welcome, $username!"

        // Logout button
        binding.btnLogout.setOnClickListener {
            sessionManager.logout()
            Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show()
            goToLogin()
        }
    }

    private fun goToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }
}
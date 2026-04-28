package com.clearcash.app.ui.auth

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.clearcash.app.data.db.AppDatabase
import com.clearcash.app.data.repository.ClearCashRepository
import com.clearcash.app.databinding.ActivityLoginBinding
import com.clearcash.app.ui.main.MainActivity
import com.clearcash.app.utils.SessionManager
import kotlinx.coroutines.launch

// login screen. first screen the user sees if they are not already logged in
class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var repository: ClearCashRepository
    private lateinit var session: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()

        repository = ClearCashRepository(AppDatabase.getDatabase(this))
        session    = SessionManager(this)

        // if a session is already saved. skip login go to app if its saved
        if (session.isLoggedIn()) { goToMain(); return }

        // this will validate inputs then attempt login
        binding.btnLogin.setOnClickListener {
            val user = binding.etUsername.text.toString().trim()
            val pass = binding.etPassword.text.toString().trim()
            if (user.isEmpty()) { binding.tilUsername.error = "Required"; return@setOnClickListener }
            if (pass.isEmpty()) { binding.tilPassword.error = "Required"; return@setOnClickListener }
            binding.tilUsername.error = null; binding.tilPassword.error = null
            doLogin(user, pass)
        }

        // navigate to the registration screen
        binding.tvCreateAccount.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    // this will run the login on a background coroutine and handles the result on the UI thread
    private fun doLogin(username: String, password: String) {
        binding.progressBar.visibility = View.VISIBLE
        binding.btnLogin.isEnabled = false
        lifecycleScope.launch {
            val result = repository.loginUser(username, password)
            runOnUiThread {
                binding.progressBar.visibility = View.GONE
                binding.btnLogin.isEnabled = true
                result.onSuccess { user ->
                    Log.d("LoginActivity", "Login OK userId=${user.id}")
                    // Save the session so the user stays logged in next time
                    session.saveSession(user.id, user.username)
                    goToMain()
                }.onFailure {
                    Toast.makeText(this@LoginActivity, it.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // navigate to mainActivity and clear the back stack so the user cannot go back to login
    private fun goToMain() {
        startActivity(Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        })
        finish()
    }
}

// Reference: Karanpuria, R. and Roy, A.S. (2018) Kotlin Programming Cookbook: Explore More Than 100 Recipes That Show How to Build Robust Mobile and Web Applications with Kotlin, Spring Boot, and Android. Birmingham, UK: Packt Publishing. Available at: https://search-ebscohost-com.ezproxy.iielearn.ac.za/login.aspx?direct=true&db=e000xww&AN=1699229&site=ehost-live&scope=site [Accessed 20 August 2024].

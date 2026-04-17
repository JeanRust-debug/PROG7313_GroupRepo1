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

        if (session.isLoggedIn()) { goToMain(); return }

        // ── Event handling ────────────────────────────────────────────────────
        binding.btnLogin.setOnClickListener {
            val user = binding.etUsername.text.toString().trim()
            val pass = binding.etPassword.text.toString().trim()
            if (user.isEmpty()) { binding.tilUsername.error = "Required"; return@setOnClickListener }
            if (pass.isEmpty()) { binding.tilPassword.error = "Required"; return@setOnClickListener }
            binding.tilUsername.error = null; binding.tilPassword.error = null
            doLogin(user, pass)
        }

        // Intent to RegisterActivity
        binding.tvCreateAccount.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

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
                    session.saveSession(user.id, user.username)
                    goToMain()
                }.onFailure {
                    Toast.makeText(this@LoginActivity, it.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun goToMain() {
        startActivity(Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        })
        finish()
    }
}
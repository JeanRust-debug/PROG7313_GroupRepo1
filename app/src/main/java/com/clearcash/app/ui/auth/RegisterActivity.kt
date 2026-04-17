package com.clearcash.app.ui.auth

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.clearcash.app.data.db.AppDatabase
import com.clearcash.app.data.repository.ClearCashRepository
import com.clearcash.app.databinding.ActivityRegisterBinding
import com.clearcash.app.ui.main.MainActivity
import com.clearcash.app.utils.SessionManager
import kotlinx.coroutines.launch

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private lateinit var repository: ClearCashRepository
    private lateinit var session: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()

        repository = ClearCashRepository(AppDatabase.getDatabase(this))
        session    = SessionManager(this)

        binding.btnSignUp.setOnClickListener {
            val user    = binding.etUsername.text.toString().trim()
            val email   = binding.etEmail.text.toString().trim()
            val pass    = binding.etPassword.text.toString().trim()
            val confirm = binding.etConfirm.text.toString().trim()

            var valid = true
            if (user.length < 3)                              { binding.tilUsername.error = "Min 3 characters"; valid = false } else binding.tilUsername.error = null
            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) { binding.tilEmail.error    = "Invalid email";    valid = false } else binding.tilEmail.error = null
            if (pass.length < 6)                              { binding.tilPassword.error = "Min 6 characters"; valid = false } else binding.tilPassword.error = null
            if (pass != confirm)                              { binding.tilConfirm.error  = "Passwords don't match"; valid = false } else binding.tilConfirm.error = null
            if (!valid) return@setOnClickListener

            doRegister(user, email, pass)
        }

        binding.tvAlreadyRegistered.setOnClickListener { finish() }
    }

    private fun doRegister(username: String, email: String, password: String) {
        binding.progressBar.visibility = View.VISIBLE
        binding.btnSignUp.isEnabled = false
        lifecycleScope.launch {
            val result = repository.registerUser(username, email, password)
            runOnUiThread {
                binding.progressBar.visibility = View.GONE
                binding.btnSignUp.isEnabled = true
                result.onSuccess { user ->
                    Log.d("RegisterActivity", "Registered userId=${user.id}")
                    session.saveSession(user.id, user.username)
                    startActivity(Intent(this@RegisterActivity, MainActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    })
                    finish()
                }.onFailure {
                    Toast.makeText(this@RegisterActivity, it.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
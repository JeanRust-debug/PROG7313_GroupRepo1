package com.example.prog7313.ui.Main

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.prog7313.PROG7313Application
import com.example.prog7313.R
import com.example.prog7313.ui.Category.CategoryActivity
import com.example.prog7313.ui.Expense.ExpenseActivity
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var welcomeText: TextView
    private lateinit var manageCategoriesButton: Button
    private lateinit var addExpenseButton: Button
    private lateinit var viewExpensesButton: Button
    private lateinit var logoutButton: Button

    private val database by lazy {
        (application as PROG7313Application).database
    }

    private var currentUserId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val sharedPref = getSharedPreferences("app_prefs", MODE_PRIVATE)
        currentUserId = sharedPref.getInt("current_user_id", -1)

        if (currentUserId == -1) {
            Toast.makeText(this, "Please login first", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        welcomeText = findViewById(R.id.welcomeText)
        manageCategoriesButton = findViewById(R.id.manageCategoriesButton)
        addExpenseButton = findViewById(R.id.addExpenseButton)
        viewExpensesButton = findViewById(R.id.viewExpensesButton)
        logoutButton = findViewById(R.id.logoutButton)

        loadUserInfo()

        manageCategoriesButton.setOnClickListener {
            startActivity(Intent(this, CategoryActivity::class.java))
        }

        addExpenseButton.setOnClickListener {
            startActivity(Intent(this, ExpenseActivity::class.java))
        }

        logoutButton.setOnClickListener {
            sharedPref.edit().clear().apply()
            finish()
        }
    }

    private fun loadUserInfo() {
        lifecycleScope.launch {
            try {
                // In a real app, you'd get the username from the database
                welcomeText.text = "Welcome to your Budget Tracker!"
            } catch (e: Exception) {
                Toast.makeText(this@MainActivity, "Error loading data", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
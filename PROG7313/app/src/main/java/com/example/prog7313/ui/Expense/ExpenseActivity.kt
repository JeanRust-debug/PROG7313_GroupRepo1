package com.example.prog7313.ui.Expense

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.prog7313.PROG7313Application
import com.example.prog7313.R
import com.example.prog7313.data.Category
import com.example.prog7313.data.Expense
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class ExpenseActivity : AppCompatActivity() {

    private lateinit var amountInput: EditText
    private lateinit var descriptionInput: EditText
    private lateinit var dateButton: Button
    private lateinit var startTimeButton: Button
    private lateinit var endTimeButton: Button
    private lateinit var categorySpinner: Spinner
    private lateinit var saveButton: Button

    // FIXED: Initialize with current date
    private var selectedDate: Date = Date()
    private var startTime = "09:00"
    private var endTime = "10:00"
    private var categories = mutableListOf<Category>()
    private var currentUserId: Int = -1

    private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    private val database by lazy {
        (application as PROG7313Application).database
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_expense)

        Log.d("ExpenseActivity", "Expense screen created")

        val sharedPref = getSharedPreferences("app_prefs", MODE_PRIVATE)
        currentUserId = sharedPref.getInt("current_user_id", -1)

        if (currentUserId == -1) {
            Toast.makeText(this, "Please login first", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        initializeViews()
        setupClickListeners()
        loadCategories()
    }

    private fun initializeViews() {
        amountInput = findViewById(R.id.amountInput)
        descriptionInput = findViewById(R.id.descriptionInput)
        dateButton = findViewById(R.id.dateButton)
        startTimeButton = findViewById(R.id.startTimeButton)
        endTimeButton = findViewById(R.id.endTimeButton)
        categorySpinner = findViewById(R.id.categorySpinner)
        saveButton = findViewById(R.id.saveExpenseButton)

        updateDateTimeButtons()
    }

    private fun setupClickListeners() {
        dateButton.setOnClickListener {
            showDatePicker()
        }

        startTimeButton.setOnClickListener {
            showTimePicker(true)
        }

        endTimeButton.setOnClickListener {
            showTimePicker(false)
        }

        saveButton.setOnClickListener {
            saveExpense()
        }
    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        calendar.time = selectedDate

        DatePickerDialog(
            this,
            { _, year, month, day ->
                calendar.set(year, month, day)
                selectedDate = calendar.time
                updateDateTimeButtons()
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun showTimePicker(isStartTime: Boolean) {
        val calendar = Calendar.getInstance()
        val timeToParse = if (isStartTime) startTime else endTime
        val parts = timeToParse.split(":")

        if (parts.size == 2) {
            calendar.set(Calendar.HOUR_OF_DAY, parts[0].toInt())
            calendar.set(Calendar.MINUTE, parts[1].toInt())
        }

        TimePickerDialog(
            this,
            { _, hour, minute ->
                val time = String.format("%02d:%02d", hour, minute)
                if (isStartTime) {
                    startTime = time
                } else {
                    endTime = time
                }
                updateDateTimeButtons()
            },
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE),
            true
        ).show()
    }

    private fun updateDateTimeButtons() {
        dateButton.text = "Date: ${dateFormat.format(selectedDate)}"
        startTimeButton.text = "Start: $startTime"
        endTimeButton.text = "End: $endTime"
    }

    private fun loadCategories() {
        lifecycleScope.launch {
            try {
                categories = database.categoryDao().getAllCategoriesForUser(currentUserId).toMutableList()
                val categoryNames = categories.map { it.name }

                val adapter = ArrayAdapter(
                    this@ExpenseActivity,
                    android.R.layout.simple_spinner_item,
                    categoryNames
                )
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                categorySpinner.adapter = adapter

                Log.d("ExpenseActivity", "Loaded ${categories.size} categories")
            } catch (e: Exception) {
                Log.e("ExpenseActivity", "Error loading categories", e)
                Toast.makeText(this@ExpenseActivity, "Please create categories first", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun saveExpense() {
        val amountText = amountInput.text.toString().trim()
        val description = descriptionInput.text.toString().trim()

        if (amountText.isEmpty() || description.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            return
        }

        val amount = amountText.toDoubleOrNull()
        if (amount == null || amount <= 0) {
            Toast.makeText(this, "Please enter a valid amount", Toast.LENGTH_SHORT).show()
            return
        }

        if (categories.isEmpty()) {
            Toast.makeText(this, "Please create a category first", Toast.LENGTH_SHORT).show()
            return
        }

        val selectedPosition = categorySpinner.selectedItemPosition
        if (selectedPosition < 0 || selectedPosition >= categories.size) {
            Toast.makeText(this, "Please select a category", Toast.LENGTH_SHORT).show()
            return
        }

        val selectedCategory = categories[selectedPosition]

        lifecycleScope.launch {
            try {
                val expense = Expense(
                    amount = amount,
                    date = selectedDate,
                    startTime = startTime,
                    endTime = endTime,
                    description = description,
                    categoryId = selectedCategory.id,
                    userId = currentUserId
                )

                val expenseId = database.expenseDao().insertExpense(expense)
                Log.d("ExpenseActivity", "Expense saved with ID: $expenseId")

                Toast.makeText(this@ExpenseActivity, "Expense saved successfully!", Toast.LENGTH_SHORT).show()
                finish()
            } catch (e: Exception) {
                Log.e("ExpenseActivity", "Error saving expense", e)
                Toast.makeText(this@ExpenseActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
package com.example.prog7313.ui.Category

import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.prog7313.PROG7313Application
import com.example.prog7313.R
import com.example.prog7313.data.Category
import kotlinx.coroutines.launch

class CategoryActivity : AppCompatActivity() {

    private lateinit var categoryInput: EditText
    private lateinit var addButton: Button
    private lateinit var categoryListView: ListView
    private lateinit var adapter: ArrayAdapter<String>
    private var categories = mutableListOf<Category>()
    private var currentUserId: Int = -1

    private val database by lazy {
        (application as PROG7313Application).database
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_category)

        Log.d("CategoryActivity", "Category screen created")

        val sharedPref = getSharedPreferences("app_prefs", MODE_PRIVATE)
        currentUserId = sharedPref.getInt("current_user_id", -1)

        if (currentUserId == -1) {
            Toast.makeText(this, "Please login first", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        categoryInput = findViewById(R.id.categoryInput)
        addButton = findViewById(R.id.addCategoryButton)
        categoryListView = findViewById(R.id.categoryListView)

        adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, mutableListOf())
        categoryListView.adapter = adapter

        addButton.setOnClickListener {
            val categoryName = categoryInput.text.toString().trim()
            if (categoryName.isNotEmpty()) {
                addCategory(categoryName)
            } else {
                Toast.makeText(this, "Enter category name", Toast.LENGTH_SHORT).show()
            }
        }

        categoryListView.setOnItemLongClickListener { _, _, position, _ ->
            val category = categories[position]
            showDeleteDialog(category)
            true
        }

        loadCategories()
    }

    private fun addCategory(name: String) {
        lifecycleScope.launch {
            try {
                val category = Category(name = name, userId = currentUserId)
                database.categoryDao().insertCategory(category)
                categoryInput.text.clear()
                loadCategories()
                Toast.makeText(this@CategoryActivity, "Category added!", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(this@CategoryActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun loadCategories() {
        lifecycleScope.launch {
            try {
                categories = database.categoryDao().getAllCategoriesForUser(currentUserId).toMutableList()
                val categoryNames = categories.map { it.name }
                adapter.clear()
                adapter.addAll(categoryNames)
                adapter.notifyDataSetChanged()
            } catch (e: Exception) {
                Toast.makeText(this@CategoryActivity, "Error loading categories", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showDeleteDialog(category: Category) {
        AlertDialog.Builder(this)
            .setTitle("Delete Category")
            .setMessage("Delete '${category.name}'?")
            .setPositiveButton("Delete") { _, _ ->
                lifecycleScope.launch {
                    database.categoryDao().deleteCategory(category)
                    loadCategories()
                    Toast.makeText(this@CategoryActivity, "Deleted", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}
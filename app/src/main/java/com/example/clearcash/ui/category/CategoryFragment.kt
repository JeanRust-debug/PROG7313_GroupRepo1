package com.example.clearcash.ui.category
import com.example.clearcash.util.ValidationUtils
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.clearcash.data.db.entities.Category
import com.example.clearcash.databinding.FragmentCategoryBinding

/**
 * Fragment for managing expense categories.
 * Allows users to add and delete categories with optional spending limits.
 * Author: Muaaz Abdool Gaffoor (ST10443760)
 */
class CategoryFragment : Fragment() {

    private val TAG = "CategoryFragment"

    // ViewBinding reference
    private var _binding: FragmentCategoryBinding? = null
    private val binding get() = _binding!!

    // ViewModel scoped to this fragment
    private val viewModel: CategoryViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCategoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG, "CategoryFragment loaded")

        // Set up RecyclerView with adapter
        val adapter = CategoryAdapter { category ->
            // Delete category on delete button click
            viewModel.deleteCategory(category)
            Toast.makeText(requireContext(), "${category.name} deleted", Toast.LENGTH_SHORT).show()
            Log.d(TAG, "Deleted category: ${category.name}")
        }

        binding.rvCategories.layoutManager = LinearLayoutManager(requireContext())
        binding.rvCategories.adapter = adapter

        // Observe categories LiveData and update UI
        viewModel.allCategories.observe(viewLifecycleOwner) { categories ->
            adapter.submitList(categories)

            // Show empty state if no categories exist
            if (categories.isEmpty()) {
                binding.tvNoCategories.visibility = View.VISIBLE
                binding.rvCategories.visibility = View.GONE
            } else {
                binding.tvNoCategories.visibility = View.GONE
                binding.rvCategories.visibility = View.VISIBLE
            }

            Log.d(TAG, "Categories updated: ${categories.size} items")
        }

        // Handle save button click
        binding.btnSaveCategory.setOnClickListener {
            saveCategory()
        }
    }

    /**
     * Validates input and saves a new category to the database.
     */
    private fun saveCategory() {
        val name = binding.etCategoryName.text.toString().trim()
        val limitStr = binding.etCategoryLimit.text.toString().trim()

        // Use ValidationUtils for consistent validation
        val nameError = ValidationUtils.validateCategoryName(name)
        if (nameError != null) {
            binding.tilCategoryName.error = nameError
            Log.w(TAG, "Validation failed: $nameError")
            return
        } else {
            binding.tilCategoryName.error = null
        }

        // Validate limit only if provided
        if (limitStr.isNotEmpty()) {
            val limitError = ValidationUtils.validateAmount(limitStr)
            if (limitError != null) {
                binding.tilCategoryLimit.error = limitError
                return
            } else {
                binding.tilCategoryLimit.error = null
            }
        }

        val limit = if (limitStr.isEmpty()) 0.0 else limitStr.toDouble()
        val category = Category(name = name, limit = limit)
        viewModel.insertCategory(category)

        binding.etCategoryName.text?.clear()
        binding.etCategoryLimit.text?.clear()

        Toast.makeText(requireContext(), "$name added!", Toast.LENGTH_SHORT).show()
        Log.d(TAG, "Category saved: $name with limit R$limit")
    }

    // Clean up binding when fragment view is destroyed
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
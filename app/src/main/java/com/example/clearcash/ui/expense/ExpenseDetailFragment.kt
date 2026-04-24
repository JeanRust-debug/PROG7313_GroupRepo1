package com.example.clearcash.ui.expense

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.clearcash.databinding.FragmentExpenseDetailBinding
import com.example.clearcash.ui.category.CategoryViewModel
import com.example.clearcash.util.DateUtils
import java.io.File

/**
 * Fragment showing full details of a single expense entry.
 * Displays receipt photo if available.
 * Author: Muaaz Abdool Gaffoor (ST10443760)
 */
class ExpenseDetailFragment : Fragment() {

    private var _binding: FragmentExpenseDetailBinding? = null
    private val binding get() = _binding!!
    private val categoryViewModel: CategoryViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentExpenseDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val amount = arguments?.getFloat("amount") ?: 0f
        val date = arguments?.getLong("date") ?: 0L
        val description = arguments?.getString("description") ?: ""
        val categoryId = arguments?.getInt("categoryId") ?: 0
        val photoPath = arguments?.getString("photoPath")

        binding.tvDetailDescription.text = description
        binding.tvDetailAmount.text = "R%.2f".format(amount)
        binding.tvDetailDate.text = "Date: ${DateUtils.formatDate(date)}"

        // Back button
        binding.btnBack.setOnClickListener {
            findNavController().popBackStack()
        }

        categoryViewModel.allCategories.observe(viewLifecycleOwner) { categories ->
            val category = categories.find { it.id == categoryId }
            binding.tvDetailCategory.text = "Category: ${category?.name ?: "Unknown"}"
        }

        // Load photo from file path only
        if (!photoPath.isNullOrEmpty()) {
            val file = File(photoPath)
            if (file.exists()) {
                binding.ivReceiptFull.visibility = View.VISIBLE
                binding.tvNoPhoto.visibility = View.GONE
                Glide.with(this)
                    .load(file)
                    .centerCrop()
                    .into(binding.ivReceiptFull)
            } else {
                binding.ivReceiptFull.visibility = View.GONE
                binding.tvNoPhoto.visibility = View.VISIBLE
            }
        } else {
            binding.ivReceiptFull.visibility = View.GONE
            binding.tvNoPhoto.visibility = View.VISIBLE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
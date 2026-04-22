package com.example.clearcash.ui.expense

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.clearcash.data.db.entities.Expense
import com.example.clearcash.databinding.ItemExpenseBinding
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * RecyclerView Adapter for displaying expense entries.
 * Shows receipt photo thumbnail if available.
 * Author: Muaaz Abdool Gaffoor (ST10443760)
 */
class ExpenseAdapter(
    private val categoryNames: Map<Int, String>,
    private val onItemClick: (Expense) -> Unit
) : ListAdapter<Expense, ExpenseAdapter.ExpenseViewHolder>(DiffCallback()) {

    inner class ExpenseViewHolder(
        private val binding: ItemExpenseBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(expense: Expense) {
            // Format date from timestamp
            val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
            binding.tvExpenseDate.text = dateFormat.format(Date(expense.date))

            binding.tvExpenseDescription.text = expense.description
            binding.tvExpenseAmount.text = "R%.2f".format(expense.amount)

            // Look up category name from map
            binding.tvExpenseCategory.text = categoryNames[expense.categoryId] ?: "Unknown"

            // Load receipt photo if available using Glide
            if (!expense.photoPath.isNullOrEmpty()) {
                val file = File(expense.photoPath)
                if (file.exists()) {
                    Glide.with(binding.root.context)
                        .load(file)
                        .centerCrop()
                        .into(binding.ivReceiptThumb)
                }
            }

            // Handle item click to view full receipt
            binding.root.setOnClickListener { onItemClick(expense) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExpenseViewHolder {
        val binding = ItemExpenseBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ExpenseViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ExpenseViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class DiffCallback : DiffUtil.ItemCallback<Expense>() {
        override fun areItemsTheSame(oldItem: Expense, newItem: Expense) =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: Expense, newItem: Expense) =
            oldItem == newItem
    }
}
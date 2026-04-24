package com.example.clearcash.ui.expense

import android.view.View
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
 * Shows receipt photo thumbnail if available, supports delete.
 * Author: Muaaz Abdool Gaffoor (ST10443760)
 */
class ExpenseAdapter(
    private val categoryNames: Map<Int, String>,
    private val onItemClick: (Expense) -> Unit,
    private val onDeleteClick: (Expense) -> Unit
) : ListAdapter<Expense, ExpenseAdapter.ExpenseViewHolder>(DiffCallback()) {

    inner class ExpenseViewHolder(
        private val binding: ItemExpenseBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(expense: Expense) {
            val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
            binding.tvExpenseDate.text = dateFormat.format(Date(expense.date))
            binding.tvExpenseDescription.text = expense.description
            binding.tvExpenseAmount.text = "R%.2f".format(expense.amount)
            binding.tvExpenseCategory.text = categoryNames[expense.categoryId] ?: "Unknown"

            // Load photo if path exists and file is accessible
            val path = expense.photoPath
            if (!path.isNullOrEmpty()) {
                val file = File(path)
                if (file.exists()) {
                    Glide.with(binding.root.context)
                        .load(file)
                        .centerCrop()
                        .placeholder(android.R.drawable.ic_menu_camera)
                        .error(android.R.drawable.ic_menu_camera)
                        .into(binding.ivReceiptThumb)
                } else {
                    binding.ivReceiptThumb.setImageResource(android.R.drawable.ic_menu_camera)
                }
            } else {
                binding.ivReceiptThumb.setImageResource(android.R.drawable.ic_menu_camera)
            }

            binding.root.setOnClickListener { onItemClick(expense) }
            binding.btnDeleteExpense.setOnClickListener { onDeleteClick(expense) }
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
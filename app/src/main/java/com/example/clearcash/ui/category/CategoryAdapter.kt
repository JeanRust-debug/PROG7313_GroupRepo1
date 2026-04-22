package com.example.clearcash.ui.category

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.clearcash.data.db.entities.Category
import com.example.clearcash.databinding.ItemCategoryBinding

/**
 * RecyclerView Adapter for displaying categories.
 * Uses ListAdapter with DiffUtil for efficient updates.
 * Author: Muaaz Abdool Gaffoor (ST10443760)
 */
class CategoryAdapter(
    private val onDeleteClick: (Category) -> Unit
) : ListAdapter<Category, CategoryAdapter.CategoryViewHolder>(DiffCallback()) {

    inner class CategoryViewHolder(
        private val binding: ItemCategoryBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(category: Category) {
            binding.tvCategoryName.text = category.name

            // Show limit if set, otherwise show "No limit set"
            binding.tvCategoryLimit.text = if (category.limit > 0) {
                "Limit: R%.2f".format(category.limit)
            } else {
                "No limit set"
            }

            // Handle delete button click
            binding.btnDeleteCategory.setOnClickListener {
                onDeleteClick(category)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val binding = ItemCategoryBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return CategoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    // DiffUtil tells RecyclerView exactly what changed for smooth animations
    class DiffCallback : DiffUtil.ItemCallback<Category>() {
        override fun areItemsTheSame(oldItem: Category, newItem: Category) =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: Category, newItem: Category) =
            oldItem == newItem
    }
}
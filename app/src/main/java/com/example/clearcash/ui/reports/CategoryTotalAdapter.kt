package com.example.clearcash.ui.reports

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.clearcash.databinding.ItemCategoryTotalBinding

// this is just a test commit.
class CategoryTotalAdapter :
    ListAdapter<CategoryTotalAdapter.Row, CategoryTotalAdapter.ViewHolder>(DiffCallback()) {


    data class Row(
        val categoryId: Int,
        val categoryName: String,
        val total: Double
    )

    inner class ViewHolder(
        private val binding: ItemCategoryTotalBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(row: Row) {
            binding.tvCategoryName.text = row.categoryName
            binding.tvCategoryTotal.text = "R%.2f".format(row.total)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemCategoryTotalBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class DiffCallback : DiffUtil.ItemCallback<Row>() {
        override fun areItemsTheSame(oldItem: Row, newItem: Row) =
            oldItem.categoryId == newItem.categoryId

        override fun areContentsTheSame(oldItem: Row, newItem: Row) =
            oldItem == newItem
    }
}
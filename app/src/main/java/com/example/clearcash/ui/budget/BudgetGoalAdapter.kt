package com.example.clearcash.ui.budget

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.clearcash.data.db.entities.BudgetGoal
import com.example.clearcash.databinding.ItemBudgetGoalBinding

/**
 * RecyclerView Adapter for displaying budget goal history.
 * Author: Muaaz Abdool Gaffoor (ST10443760)
 */
class BudgetGoalAdapter : ListAdapter<BudgetGoal, BudgetGoalAdapter.BudgetGoalViewHolder>(
    DiffCallback()
) {

    inner class BudgetGoalViewHolder(
        private val binding: ItemBudgetGoalBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(goal: BudgetGoal) {
            binding.tvGoalMonth.text = goal.month
            binding.tvGoalMin.text = "R%.2f".format(goal.minGoal)
            binding.tvGoalMax.text = "R%.2f".format(goal.maxGoal)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BudgetGoalViewHolder {
        val binding = ItemBudgetGoalBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return BudgetGoalViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BudgetGoalViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class DiffCallback : DiffUtil.ItemCallback<BudgetGoal>() {
        override fun areItemsTheSame(oldItem: BudgetGoal, newItem: BudgetGoal) =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: BudgetGoal, newItem: BudgetGoal) =
            oldItem == newItem
    }
}
package com.clearcash.app.ui.goals

import android.view.*
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.clearcash.app.data.db.entities.SavingsGoal
import com.clearcash.app.databinding.ItemGoalBinding
import com.clearcash.app.utils.CurrencyFormatter
import kotlin.math.roundToInt

class GoalAdapter(
    private val onAddFunds: (SavingsGoal) -> Unit,
    private val onDelete: (SavingsGoal) -> Unit
) : ListAdapter<SavingsGoal, GoalAdapter.VH>(Diff()) {

    override fun onCreateViewHolder(p: ViewGroup, t: Int) =
        VH(ItemGoalBinding.inflate(LayoutInflater.from(p.context), p, false))

    override fun onBindViewHolder(h: VH, pos: Int) = h.bind(getItem(pos))

    inner class VH(private val b: ItemGoalBinding) : RecyclerView.ViewHolder(b.root) {
        fun bind(g: SavingsGoal) {
            b.tvGoalName.text   = g.name
            b.tvGoalSaved.text  = "Saved: ${CurrencyFormatter.format(g.savedAmount)}"
            b.tvGoalTarget.text = "Target: ${CurrencyFormatter.format(g.targetAmount)}"

            val pct = if (g.targetAmount > 0)
                ((g.savedAmount / g.targetAmount) * 100).roundToInt().coerceIn(0, 100)
            else 0
            b.pbGoal.progress    = pct
            b.tvGoalPercent.text = "$pct%"

            b.btnAddFunds.setOnClickListener  { onAddFunds(g) }
            b.btnDeleteGoal.setOnClickListener { onDelete(g) }
        }
    }

    class Diff : DiffUtil.ItemCallback<SavingsGoal>() {
        override fun areItemsTheSame(a: SavingsGoal, b: SavingsGoal) = a.id == b.id
        override fun areContentsTheSame(a: SavingsGoal, b: SavingsGoal) = a == b
    }
}

package com.clearcash.app.ui.expense

import android.view.*
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.clearcash.app.data.db.entities.Category
import com.clearcash.app.data.db.entities.Expense
import com.clearcash.app.databinding.ItemExpenseBinding
import com.clearcash.app.utils.CurrencyFormatter
import com.clearcash.app.utils.DateUtils

class ExpenseAdapter(
    private var categories: List<Category>,
    private val onReceipt: (Expense) -> Unit
) : ListAdapter<Expense, ExpenseAdapter.VH>(Diff()) {

    fun updateCategories(cats: List<Category>) { categories = cats; notifyDataSetChanged() }

    override fun onCreateViewHolder(p: ViewGroup, t: Int) =
        VH(ItemExpenseBinding.inflate(LayoutInflater.from(p.context), p, false))

    override fun onBindViewHolder(h: VH, pos: Int) = h.bind(getItem(pos))

    inner class VH(private val b: ItemExpenseBinding) : RecyclerView.ViewHolder(b.root) {
        fun bind(e: Expense) {
            b.tvAmount.text      = CurrencyFormatter.format(e.amount)
            b.tvDate.text        = DateUtils.formatForDisplay(e.date)
            b.tvDescription.text = e.description
            b.tvCategory.text    = categories.find { it.id == e.categoryId }?.name ?: "Uncategorized"
            if (e.startTime.isNotEmpty()) b.tvTime.text = "${e.startTime} – ${e.endTime}" else b.tvTime.text = ""
            b.btnViewReceipt.visibility =
                if (!e.receiptPath.isNullOrEmpty()) View.VISIBLE else View.GONE
            b.btnViewReceipt.setOnClickListener { onReceipt(e) }
        }
    }

    class Diff : DiffUtil.ItemCallback<Expense>() {
        override fun areItemsTheSame(a: Expense, b: Expense) = a.id == b.id
        override fun areContentsTheSame(a: Expense, b: Expense) = a == b
    }
}
package com.clearcash.app.ui.category

import android.view.*
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.clearcash.app.data.db.entities.Category
import com.clearcash.app.databinding.ItemCategoryBinding
import com.clearcash.app.utils.CurrencyFormatter

class CategoryAdapter(private val onDelete: (Category) -> Unit)
    : ListAdapter<Category, CategoryAdapter.VH>(Diff()) {

    override fun onCreateViewHolder(p: ViewGroup, t: Int) =
        VH(ItemCategoryBinding.inflate(LayoutInflater.from(p.context), p, false))

    override fun onBindViewHolder(h: VH, pos: Int) = h.bind(getItem(pos))

    inner class VH(private val b: ItemCategoryBinding) : RecyclerView.ViewHolder(b.root) {
        fun bind(cat: Category) {
            b.tvCategoryName.text  = cat.name
            b.tvCategoryLimit.text = if (cat.limit > 0) "Limit: ${CurrencyFormatter.format(cat.limit)}" else "No limit"
            b.btnDelete.setOnClickListener { onDelete(cat) }
        }
    }

    class Diff : DiffUtil.ItemCallback<Category>() {
        override fun areItemsTheSame(a: Category, b: Category) = a.id == b.id
        override fun areContentsTheSame(a: Category, b: Category) = a == b
    }
}
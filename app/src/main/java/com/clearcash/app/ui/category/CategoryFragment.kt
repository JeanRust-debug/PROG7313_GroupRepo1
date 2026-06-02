package com.clearcash.app.ui.category

import android.content.Intent
import android.os.Bundle
import android.view.*
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.clearcash.app.data.db.AppDatabase
import com.clearcash.app.data.repository.ClearCashRepository
import com.clearcash.app.databinding.FragmentCategoryBinding
import com.clearcash.app.utils.SessionManager
import kotlinx.coroutines.launch

class CategoryFragment : Fragment() {

    private var _b: FragmentCategoryBinding? = null
    private val b get() = _b!!
    private lateinit var vm: CategoryViewModel
    private lateinit var session: SessionManager

    override fun onCreateView(i: LayoutInflater, c: ViewGroup?, s: Bundle?) =
        FragmentCategoryBinding.inflate(i, c, false).also { _b = it }.root

    override fun onViewCreated(view: View, s: Bundle?) {
        super.onViewCreated(view, s)
        session = SessionManager(requireContext())
        val db = AppDatabase.getDatabase(requireContext())
        vm = ViewModelProvider(this, CategoryViewModel.Factory(
            ClearCashRepository(db)
        ))[CategoryViewModel::class.java]
        val adapter = CategoryAdapter { cat ->
            viewLifecycleOwner.lifecycleScope.launch {
                val count = db.expenseDao().getCountByCategory(cat.id)
                val message = if (count > 0)
                    "This will delete '${cat.name}'. Your $count expense(s) linked to it will become uncategorized."
                else
                    "Are you sure you want to delete '${cat.name}'?"
                AlertDialog.Builder(requireContext())
                    .setTitle("Delete category?")
                    .setMessage(message)
                    .setPositiveButton("Delete") { _, _ -> vm.delete(cat) }
                    .setNegativeButton("Cancel", null).show()
            }
        }
        b.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        b.recyclerView.adapter = adapter

        vm.getCategories(session.getUserId()).observe(viewLifecycleOwner) { list ->
            adapter.submitList(list)
            b.layoutEmpty.visibility = if (list.isEmpty()) View.VISIBLE else View.GONE
        }

        b.fabAdd.setOnClickListener {
            startActivity(Intent(requireContext(), AddCategoryActivity::class.java))
        }
    }

    override fun onDestroyView() { super.onDestroyView(); _b = null }
}
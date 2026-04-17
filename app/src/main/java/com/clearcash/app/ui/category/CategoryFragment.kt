package com.clearcash.app.ui.category

import android.content.Intent
import android.os.Bundle
import android.view.*
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.clearcash.app.data.db.AppDatabase
import com.clearcash.app.data.repository.ClearCashRepository
import com.clearcash.app.databinding.FragmentCategoryBinding
import com.clearcash.app.utils.SessionManager

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
        vm = ViewModelProvider(this, CategoryViewModel.Factory(
            ClearCashRepository(AppDatabase.getDatabase(requireContext()))
        ))[CategoryViewModel::class.java]

        val adapter = CategoryAdapter { cat ->
            AlertDialog.Builder(requireContext())
                .setTitle("Delete '${cat.name}'?")
                .setPositiveButton("Delete") { _, _ -> vm.delete(cat) }
                .setNegativeButton("Cancel", null).show()
        }
        b.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        b.recyclerView.adapter = adapter

        vm.getCategories(session.getUserId()).observe(viewLifecycleOwner) { list ->
            adapter.submitList(list)
            b.tvEmpty.visibility = if (list.isEmpty()) View.VISIBLE else View.GONE
        }

        b.fabAdd.setOnClickListener {
            startActivity(Intent(requireContext(), AddCategoryActivity::class.java))
        }
    }

    override fun onDestroyView() { super.onDestroyView(); _b = null }
}
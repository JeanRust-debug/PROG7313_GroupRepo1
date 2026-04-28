package com.clearcash.app.ui.expense

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.clearcash.app.data.db.AppDatabase
import com.clearcash.app.data.db.entities.Category
import com.clearcash.app.data.repository.ClearCashRepository
import com.clearcash.app.databinding.FragmentExpenseListBinding
import com.clearcash.app.utils.CurrencyFormatter
import com.clearcash.app.utils.DateUtils
import com.clearcash.app.utils.SessionManager
import java.util.Calendar

class ExpenseListFragment : Fragment() {

    private var _binding: FragmentExpenseListBinding? = null
    private val binding get() = _binding!!

    private lateinit var vm: ExpenseViewModel
    private lateinit var session: SessionManager
    private lateinit var adapter: ExpenseAdapter
    private var cats: List<Category> = emptyList()

    private var startDate = DateUtils.getStartOfMonth(
        DateUtils.getCurrentMonth(), DateUtils.getCurrentYear()
    )
    private var endDate = DateUtils.getEndOfMonth(
        DateUtils.getCurrentMonth(), DateUtils.getCurrentYear()
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentExpenseListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        session = SessionManager(requireContext())
        vm = ViewModelProvider(
            this,
            ExpenseViewModel.Factory(
                ClearCashRepository(AppDatabase.getDatabase(requireContext()))
            )
        )[ExpenseViewModel::class.java]

        adapter = ExpenseAdapter(cats) { expense ->
            // Intent: pass expense ID to ViewReceiptActivity
            val intent = Intent(requireContext(), ViewReceiptActivity::class.java)
            intent.putExtra("expense_id", expense.id)
            startActivity(intent)
        }

        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = adapter

        binding.btnStartDate.setOnClickListener { pickDate(true) }
        binding.btnEndDate.setOnClickListener { pickDate(false) }
        binding.fabAdd.setOnClickListener {
            startActivity(Intent(requireContext(), AddExpenseActivity::class.java))
        }

        vm.loadCategories(session.getUserId())
        vm.categories.observe(viewLifecycleOwner) { c ->
            cats = c
            adapter.updateCategories(c)
            loadList()
        }

        updateDateBtns()
    }

    private fun pickDate(isStart: Boolean) {
        val ts = if (isStart) startDate else endDate
        val c  = Calendar.getInstance().also { it.timeInMillis = ts }
        DatePickerDialog(requireContext(), { _, y, m, d ->
            val nc = Calendar.getInstance().also { it.set(y, m, d) }
            val newStart = if (isStart) DateUtils.getStartOfDay(nc.timeInMillis) else startDate
            val newEnd   = if (isStart) endDate else DateUtils.getEndOfDay(nc.timeInMillis)

            // Validate the new range
            if (newStart > newEnd) {
                Toast.makeText(requireContext(),
                    "Start date must be before end date", Toast.LENGTH_SHORT).show()
                return@DatePickerDialog
            }

            startDate = newStart
            endDate = newEnd
            updateDateBtns()
            loadList()
        }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show()
    }

    private fun updateDateBtns() {
        binding.btnStartDate.text = DateUtils.formatForDisplay(startDate)
        binding.btnEndDate.text   = DateUtils.formatForDisplay(endDate)
    }

    private fun loadList() {
        vm.getByPeriod(session.getUserId(), startDate, endDate)
            .observe(viewLifecycleOwner) { list ->
                adapter.submitList(list)
                binding.tvEmpty.visibility =
                    if (list.isEmpty()) View.VISIBLE else View.GONE
                binding.tvTotal.text =
                    "Total: ${CurrencyFormatter.format(list.sumOf { it.amount })}"
            }
    }

    override fun onResume() {
        super.onResume()
        loadList()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
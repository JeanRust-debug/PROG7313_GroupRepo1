package com.example.clearcash.ui.reports

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.clearcash.R
import com.example.clearcash.data.db.entities.Category
import com.example.clearcash.data.db.entities.Expense
import com.example.clearcash.databinding.FragmentReportsBinding
import com.example.clearcash.ui.category.CategoryViewModel
import com.example.clearcash.ui.expense.ExpenseAdapter
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale


class ReportsFragment : Fragment() {

    private val TAG = "ReportsFragment"

    private var _binding: FragmentReportsBinding? = null
    private val binding get() = _binding!!

    private val reportsViewModel: ReportsViewModel by viewModels()
    private val categoryViewModel: CategoryViewModel by viewModels()

    // Currently selected date range, stored as millis. Defaults to start-of-month -> today.
    private var fromMillis: Long = 0L
    private var toMillis: Long = 0L

    private val displayDateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())

    // Adapters
    private lateinit var expenseAdapter: ExpenseAdapter
    private val categoryTotalAdapter = CategoryTotalAdapter()

    // Cache of categories (id -> name) to join with CategoryTotal rows and feed ExpenseAdapter
    private var categoriesCache: List<Category> = emptyList()

    // Most recent totals from the DB, kept so we can re-join with category names
    private var currentTotals: List<Pair<Int, Double>> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentReportsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG, "ReportsFragment loaded")

        setupDefaultDateRange()
        setupDatePickers()
        setupRecyclerViews()
        setupApplyButton()
        observeData()

        // Commit the default range so data loads on first open without requiring a tap
        reportsViewModel.setDateRange(fromMillis, toMillis)
    }


    private fun setupDefaultDateRange() {
        val cal = Calendar.getInstance()

        // End = end of today (23:59:59.999)
        cal.set(Calendar.HOUR_OF_DAY, 23)
        cal.set(Calendar.MINUTE, 59)
        cal.set(Calendar.SECOND, 59)
        cal.set(Calendar.MILLISECOND, 999)
        toMillis = cal.timeInMillis

        // Start = first of this month at 00:00:00.000
        cal.set(Calendar.DAY_OF_MONTH, 1)
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        fromMillis = cal.timeInMillis

        binding.etFromDate.setText(displayDateFormat.format(Date(fromMillis)))
        binding.etToDate.setText(displayDateFormat.format(Date(toMillis)))
    }


    private fun setupDatePickers() {
        binding.etFromDate.setOnClickListener {
            showDatePicker(fromMillis) { picked ->
                fromMillis = startOfDay(picked)
                binding.etFromDate.setText(displayDateFormat.format(Date(fromMillis)))
                Log.d(TAG, "From date picked: ${displayDateFormat.format(Date(fromMillis))}")
            }
        }

        binding.etToDate.setOnClickListener {
            showDatePicker(toMillis) { picked ->
                toMillis = endOfDay(picked)
                binding.etToDate.setText(displayDateFormat.format(Date(toMillis)))
                Log.d(TAG, "To date picked: ${displayDateFormat.format(Date(toMillis))}")
            }
        }
    }


    private fun showDatePicker(initialMillis: Long, onPicked: (Long) -> Unit) {
        val cal = Calendar.getInstance().apply { timeInMillis = initialMillis }
        DatePickerDialog(
            requireContext(),
            { _, year, month, day ->
                val pickedCal = Calendar.getInstance()
                pickedCal.set(year, month, day)
                onPicked(pickedCal.timeInMillis)
            },
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH)
        ).show()
    }


    private fun startOfDay(millis: Long): Long {
        val c = Calendar.getInstance().apply { timeInMillis = millis }
        c.set(Calendar.HOUR_OF_DAY, 0)
        c.set(Calendar.MINUTE, 0)
        c.set(Calendar.SECOND, 0)
        c.set(Calendar.MILLISECOND, 0)
        return c.timeInMillis
    }


    private fun endOfDay(millis: Long): Long {
        val c = Calendar.getInstance().apply { timeInMillis = millis }
        c.set(Calendar.HOUR_OF_DAY, 23)
        c.set(Calendar.MINUTE, 59)
        c.set(Calendar.SECOND, 59)
        c.set(Calendar.MILLISECOND, 999)
        return c.timeInMillis
    }


    private fun setupRecyclerViews() {
        // Filtered expense list — clicking a row opens the receipt photo if one exists
        expenseAdapter = ExpenseAdapter(emptyMap()) { expense ->
            openReceiptPhoto(expense)
        }
        binding.rvFilteredExpenses.layoutManager = LinearLayoutManager(requireContext())
        binding.rvFilteredExpenses.adapter = expenseAdapter

        // Category totals list
        binding.rvCategoryTotals.layoutManager = LinearLayoutManager(requireContext())
        binding.rvCategoryTotals.adapter = categoryTotalAdapter
    }


    private fun setupApplyButton() {
        binding.btnApplyRange.setOnClickListener {
            if (fromMillis > toMillis) {
                Toast.makeText(
                    requireContext(),
                    getString(R.string.error_from_after_to),
                    Toast.LENGTH_SHORT
                ).show()
                Log.w(TAG, "Apply blocked — from ($fromMillis) > to ($toMillis)")
                return@setOnClickListener
            }
            reportsViewModel.setDateRange(fromMillis, toMillis)
            Log.d(TAG, "Range applied: $fromMillis -> $toMillis")
        }
    }


    private fun observeData() {
        // Cache categories and feed their names into the ExpenseAdapter whenever they change
        categoryViewModel.allCategories.observe(viewLifecycleOwner) { categories ->
            categoriesCache = categories
            val nameById = categories.associate { it.id to it.name }

            // Rebuild the ExpenseAdapter with the updated map and re-submit current list
            val currentList = expenseAdapter.currentList
            expenseAdapter = ExpenseAdapter(nameById) { expense ->
                openReceiptPhoto(expense)
            }
            binding.rvFilteredExpenses.adapter = expenseAdapter
            expenseAdapter.submitList(currentList)

            // Also refresh the category totals rows (names may have changed)
            rebuildTotalsFromCache()
        }

        // Filtered expense entries
        reportsViewModel.filteredExpenses.observe(viewLifecycleOwner) { expenses ->
            expenseAdapter.submitList(expenses)
            binding.tvNoEntries.visibility = if (expenses.isEmpty()) View.VISIBLE else View.GONE
            binding.rvFilteredExpenses.visibility =
                if (expenses.isEmpty()) View.GONE else View.VISIBLE
            Log.d(TAG, "Filtered expenses updated: ${expenses.size} items")
        }

        // Per-category totals
        reportsViewModel.categoryTotals.observe(viewLifecycleOwner) { totals ->
            currentTotals = totals.map { it.categoryId to it.total }
            rebuildTotalsFromCache()
            Log.d(TAG, "Category totals updated: ${totals.size} categories")
        }
    }


    private fun rebuildTotalsFromCache() {
        val nameById = categoriesCache.associate { it.id to it.name }

        val rows = currentTotals.map { (catId, total) ->
            CategoryTotalAdapter.Row(
                categoryId = catId,
                categoryName = nameById[catId] ?: getString(R.string.unknown_category),
                total = total
            )
        }.sortedByDescending { it.total } // Biggest spender first

        categoryTotalAdapter.submitList(rows)

        // Grand total across all categories for the selected period
        val grand = rows.sumOf { it.total }
        binding.tvGrandTotal.text = getString(R.string.grand_total_format, grand)

        // Empty state
        binding.tvNoTotals.visibility = if (rows.isEmpty()) View.VISIBLE else View.GONE
        binding.rvCategoryTotals.visibility = if (rows.isEmpty()) View.GONE else View.VISIBLE
    }


    private fun openReceiptPhoto(expense: Expense) {
        val path = expense.photoPath
        if (path.isNullOrEmpty()) {
            Toast.makeText(
                requireContext(),
                getString(R.string.no_receipt_attached),
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        // Inflate a simple ImageView-only layout in code (no separate XML file needed)
        val imageView = ImageView(requireContext()).apply {
            adjustViewBounds = true
            scaleType = ImageView.ScaleType.FIT_CENTER
        }

        // Load from content URI or local file as appropriate
        val loadModel: Any = when {
            path.startsWith("content://") || path.startsWith("file://") -> Uri.parse(path)
            else -> {
                val file = File(path)
                if (file.exists()) file else Uri.parse(path)
            }
        }

        Glide.with(this)
            .load(loadModel)
            .into(imageView)

        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.receipt_photo))
            .setView(imageView)
            .setPositiveButton(getString(R.string.close), null)
            .show()

        Log.d(TAG, "Opened receipt for expense id=${expense.id}")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
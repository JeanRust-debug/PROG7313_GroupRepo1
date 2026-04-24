package com.example.clearcash.ui.budget

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.clearcash.data.db.entities.BudgetGoal
import com.example.clearcash.databinding.FragmentBudgetGoalBinding
import com.example.clearcash.ui.expense.ExpenseViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import com.example.clearcash.util.DateUtils
/**
 * Fragment for setting and viewing monthly budget goals.
 * Displays current month's min/max goals and spending progress.
 * Author: Muaaz Abdool Gaffoor (ST10443760)
 */
class BudgetGoalFragment : Fragment() {

    private val TAG = "BudgetGoalFragment"
    // Stores the existing goal's ID so we can update instead of insert
    private var existingGoalId: Int = 0
    private var _binding: FragmentBudgetGoalBinding? = null
    private val binding get() = _binding!!

    private val budgetGoalViewModel: BudgetGoalViewModel by viewModels()
    private val expenseViewModel: ExpenseViewModel by viewModels()

    // Current month in format "YYYY-MM"
    private val currentMonth: String by lazy {
        SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(Date())
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBudgetGoalBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG, "BudgetGoalFragment loaded for month: $currentMonth")

        // Show current month label
        binding.tvCurrentMonth.text = "Setting goals for: $currentMonth"

        setupGoalsList()
        observeCurrentMonthGoal()
        observeSpendingProgress()

        // Save button click
        binding.btnSaveBudgetGoal.setOnClickListener {
            saveBudgetGoal()
        }
    }

    /**
     * Sets up the RecyclerView showing all past budget goals.
     */
    private fun setupGoalsList() {
        val adapter = BudgetGoalAdapter()
        binding.rvBudgetGoals.layoutManager = LinearLayoutManager(requireContext())
        binding.rvBudgetGoals.adapter = adapter

        budgetGoalViewModel.allBudgetGoals.observe(viewLifecycleOwner) { goals ->
            adapter.submitList(goals)
            Log.d(TAG, "Budget goals updated: ${goals.size} items")
        }
    }

    /**
     * Observes the current month's goal and displays it if it exists.
     */
    private fun observeCurrentMonthGoal() {
        budgetGoalViewModel.getBudgetGoalForMonth(currentMonth)
            .observe(viewLifecycleOwner) { goal ->
                if (goal != null) {
                    // Save the existing ID for updates
                    existingGoalId = goal.id

                    binding.cardCurrentGoal.visibility = View.VISIBLE
                    binding.tvMinGoalDisplay.text = "R%.2f".format(goal.minGoal)
                    binding.tvMaxGoalDisplay.text = "R%.2f".format(goal.maxGoal)

                    // Pre-fill inputs with existing values
                    binding.etMinGoal.setText(goal.minGoal.toString())
                    binding.etMaxGoal.setText(goal.maxGoal.toString())

                    Log.d(TAG, "Current goal loaded: id=$existingGoalId min=${goal.minGoal} max=${goal.maxGoal}")
                } else {
                    existingGoalId = 0
                    binding.cardCurrentGoal.visibility = View.GONE
                }
            }
    }

    /**
     * Observes total spending this month and updates the progress bar.
     */
    private fun observeSpendingProgress() {
        val startOfMonth = DateUtils.getStartOfCurrentMonth()
        val endOfMonth = DateUtils.getEndOfCurrentMonth()

        expenseViewModel.getCategoryTotals(startOfMonth, endOfMonth)
            .observe(viewLifecycleOwner) { totals ->
                val totalSpent = totals.sumOf { it.total }
                Log.d(TAG, "Total spent this month: R$totalSpent")

                // Always update the progress label
                budgetGoalViewModel.getBudgetGoalForMonth(currentMonth)
                    .removeObservers(viewLifecycleOwner)

                budgetGoalViewModel.getBudgetGoalForMonth(currentMonth)
                    .observe(viewLifecycleOwner) { goal ->
                        if (goal != null && binding.cardCurrentGoal.visibility == View.VISIBLE) {
                            val progress = if (goal.maxGoal > 0) {
                                ((totalSpent / goal.maxGoal) * 100).toInt().coerceIn(0, 100)
                            } else 0
                            binding.progressBudget.progress = progress
                            binding.tvProgressLabel.text =
                                "R%.2f spent of R%.2f max (%d%%)".format(
                                    totalSpent, goal.maxGoal, progress
                                )
                            Log.d(TAG, "Progress updated: $progress%")
                        }
                    }
            }
    }

    /**
     * Validates inputs and saves the budget goal for the current month.
     */
    private fun saveBudgetGoal() {
        val minStr = binding.etMinGoal.text.toString().trim()
        val maxStr = binding.etMaxGoal.text.toString().trim()

        if (minStr.isEmpty()) {
            binding.tilMinGoal.error = "Minimum goal is required"
            return
        } else {
            binding.tilMinGoal.error = null
        }

        if (maxStr.isEmpty()) {
            binding.tilMaxGoal.error = "Maximum goal is required"
            return
        } else {
            binding.tilMaxGoal.error = null
        }

        val minGoal = minStr.toDoubleOrNull()
        val maxGoal = maxStr.toDoubleOrNull()

        if (minGoal == null || minGoal < 0) {
            binding.tilMinGoal.error = "Enter a valid minimum amount"
            return
        }

        if (maxGoal == null || maxGoal <= 0) {
            binding.tilMaxGoal.error = "Enter a valid maximum amount"
            return
        }

        if (minGoal >= maxGoal) {
            binding.tilMaxGoal.error = "Maximum must be greater than minimum"
            return
        } else {
            binding.tilMaxGoal.error = null
        }

        // Use existingGoalId so Room UPDATES instead of inserting a duplicate
        val budgetGoal = BudgetGoal(
            id = existingGoalId,
            month = currentMonth,
            minGoal = minGoal,
            maxGoal = maxGoal
        )

        budgetGoalViewModel.insertBudgetGoal(budgetGoal)
        Toast.makeText(requireContext(), "Budget goal updated!", Toast.LENGTH_SHORT).show()
        Log.d(TAG, "Budget goal saved: id=$existingGoalId min=R$minGoal max=R$maxGoal")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
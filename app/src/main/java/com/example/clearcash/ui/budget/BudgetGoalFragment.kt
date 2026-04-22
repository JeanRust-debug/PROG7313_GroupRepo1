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

/**
 * Fragment for setting and viewing monthly budget goals.
 * Displays current month's min/max goals and spending progress.
 * Author: Muaaz Abdool Gaffoor (ST10443760)
 */
class BudgetGoalFragment : Fragment() {

    private val TAG = "BudgetGoalFragment"

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
                    // Show the current goal card
                    binding.cardCurrentGoal.visibility = View.VISIBLE
                    binding.tvMinGoalDisplay.text = "R%.2f".format(goal.minGoal)
                    binding.tvMaxGoalDisplay.text = "R%.2f".format(goal.maxGoal)

                    // Pre-fill inputs with existing values
                    binding.etMinGoal.setText(goal.minGoal.toString())
                    binding.etMaxGoal.setText(goal.maxGoal.toString())

                    Log.d(TAG, "Current goal loaded: min=${goal.minGoal} max=${goal.maxGoal}")
                } else {
                    binding.cardCurrentGoal.visibility = View.GONE
                }
            }
    }

    /**
     * Observes total spending this month and updates the progress bar.
     */
    private fun observeSpendingProgress() {
        // Get start and end of current month as timestamps
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        val startOfMonth = calendar.timeInMillis

        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH))
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        val endOfMonth = calendar.timeInMillis

        // Observe category totals to calculate total spending
        expenseViewModel.getCategoryTotals(startOfMonth, endOfMonth)
            .observe(viewLifecycleOwner) { totals ->
                val totalSpent = totals.sumOf { it.total }

                // Update progress bar against max goal
                budgetGoalViewModel.getBudgetGoalForMonth(currentMonth)
                    .observe(viewLifecycleOwner) { goal ->
                        if (goal != null && goal.maxGoal > 0) {
                            val progress = ((totalSpent / goal.maxGoal) * 100).toInt()
                                .coerceIn(0, 100)
                            binding.progressBudget.progress = progress
                            binding.tvProgressLabel.text =
                                "R%.2f spent of R%.2f max (${progress}%)"
                                    .format(totalSpent, goal.maxGoal)
                            Log.d(TAG, "Spending progress: $progress%")
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

        // Validate min goal
        if (minStr.isEmpty()) {
            binding.tilMinGoal.error = "Minimum goal is required"
            return
        } else {
            binding.tilMinGoal.error = null
        }

        // Validate max goal
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

        // Make sure min is less than max
        if (minGoal >= maxGoal) {
            binding.tilMaxGoal.error = "Maximum must be greater than minimum"
            return
        } else {
            binding.tilMaxGoal.error = null
        }

        // Save budget goal
        val budgetGoal = BudgetGoal(
            month = currentMonth,
            minGoal = minGoal,
            maxGoal = maxGoal
        )

        budgetGoalViewModel.insertBudgetGoal(budgetGoal)
        Toast.makeText(requireContext(), "Budget goal saved!", Toast.LENGTH_SHORT).show()
        Log.d(TAG, "Budget goal saved: min=R$minGoal max=R$maxGoal for $currentMonth")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
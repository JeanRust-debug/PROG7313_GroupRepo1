package com.example.clearcash.ui.budget

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.clearcash.data.db.entities.BudgetGoal
import com.example.clearcash.databinding.FragmentBudgetGoalBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class BudgetGoalFragment : Fragment() {

    private val TAG = "BudgetGoalFragment"

    // ViewBinding reference
    private var _binding: FragmentBudgetGoalBinding? = null
    private val binding get() = _binding!!

    // ViewModel scoped to this fragment
    private val viewModel: BudgetGoalViewModel by viewModels()

    // Current month key in YYYY-MM format (e.g. "2026-04")
    private val currentMonthKey: String by lazy {
        SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(Date())
    }

    // Pretty month label for the UI (e.g. "April 2026")
    private val currentMonthLabel: String by lazy {
        SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(Date())
    }

    // The existing goal for this month, if any — kept so we preserve the primary key on update
    private var existingGoal: BudgetGoal? = null

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
        Log.d(TAG, "BudgetGoalFragment loaded for month $currentMonthKey")

        // Header shows which month these goals apply to
        binding.tvMonthLabel.text = getString(
            com.example.clearcash.R.string.goals_for_month, currentMonthLabel
        )

        // Pre-fill fields if a goal already exists for this month
        viewModel.getBudgetGoalForMonth(currentMonthKey).observe(viewLifecycleOwner) { goal ->
            existingGoal = goal
            if (goal != null) {
                binding.etMinGoal.setText(goal.minGoal.toString())
                binding.etMaxGoal.setText(goal.maxGoal.toString())
                binding.tvCurrentGoalSummary.visibility = View.VISIBLE
                binding.tvCurrentGoalSummary.text = getString(
                    com.example.clearcash.R.string.current_goal_summary,
                    goal.minGoal, goal.maxGoal
                )
                Log.d(TAG, "Existing goal loaded: min=${goal.minGoal} max=${goal.maxGoal}")
            } else {
                binding.tvCurrentGoalSummary.visibility = View.GONE
                Log.d(TAG, "No existing goal for this month yet")
            }
        }

        // Save button handler
        binding.btnSaveGoal.setOnClickListener {
            saveBudgetGoal()
        }
    }


    private fun saveBudgetGoal() {
        val minStr = binding.etMinGoal.text.toString().trim()
        val maxStr = binding.etMaxGoal.text.toString().trim()

        // Validate minimum goal field
        if (minStr.isEmpty()) {
            binding.tilMinGoal.error = "Minimum goal is required"
            return
        } else {
            binding.tilMinGoal.error = null
        }

        val minGoal = minStr.toDoubleOrNull()
        if (minGoal == null || minGoal < 0) {
            binding.tilMinGoal.error = "Enter a valid amount"
            return
        } else {
            binding.tilMinGoal.error = null
        }

        // Validate maximum goal field
        if (maxStr.isEmpty()) {
            binding.tilMaxGoal.error = "Maximum goal is required"
            return
        } else {
            binding.tilMaxGoal.error = null
        }

        val maxGoal = maxStr.toDoubleOrNull()
        if (maxGoal == null || maxGoal <= 0) {
            binding.tilMaxGoal.error = "Enter a valid amount"
            return
        } else {
            binding.tilMaxGoal.error = null
        }

        // Enforce min is not greater than max
        if (minGoal > maxGoal) {
            binding.tilMaxGoal.error = "Maximum must be greater than minimum"
            Log.w(TAG, "Save blocked — min ($minGoal) > max ($maxGoal)")
            return
        }

        // Reuse existing primary key on update, otherwise autoGenerate handles insert
        val goalToSave = BudgetGoal(
            id = existingGoal?.id ?: 0,
            month = currentMonthKey,
            minGoal = minGoal,
            maxGoal = maxGoal
        )

        viewModel.insertBudgetGoal(goalToSave)

        Toast.makeText(
            requireContext(),
            "Budget goals saved for $currentMonthLabel",
            Toast.LENGTH_SHORT
        ).show()
        Log.d(TAG, "Goal saved: min=$minGoal max=$maxGoal for $currentMonthKey")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
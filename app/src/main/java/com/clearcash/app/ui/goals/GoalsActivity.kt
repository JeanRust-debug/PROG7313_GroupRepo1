package com.clearcash.app.ui.goals

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.clearcash.app.data.db.AppDatabase
import com.clearcash.app.data.repository.ClearCashRepository
import com.clearcash.app.databinding.ActivityGoalsBinding
import com.clearcash.app.utils.SessionManager

class GoalsActivity : AppCompatActivity() {

    private lateinit var b: ActivityGoalsBinding
    private lateinit var vm: GoalsViewModel
    private lateinit var session: SessionManager
    private lateinit var adapter: GoalAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityGoalsBinding.inflate(layoutInflater)
        setContentView(b.root)

        setSupportActionBar(b.toolbar)
        supportActionBar?.run { title = "Savings Goals"; setDisplayHomeAsUpEnabled(true) }

        session = SessionManager(this)
        vm = ViewModelProvider(this, GoalsViewModel.Factory(
            ClearCashRepository(AppDatabase.getDatabase(this))
        ))[GoalsViewModel::class.java]

        adapter = GoalAdapter(
            onAddFunds = { goal -> showAddFundsDialog(goal.id.toString()) { amount ->
                vm.addFunds(goal, amount)
            }},
            onDelete = { goal ->
                AlertDialog.Builder(this)
                    .setTitle("Delete Goal")
                    .setMessage("Delete \"${goal.name}\"?")
                    .setPositiveButton("Delete") { _, _ -> vm.deleteGoal(goal) }
                    .setNegativeButton("Cancel", null)
                    .show()
            }
        )

        b.rvGoals.layoutManager = LinearLayoutManager(this)
        b.rvGoals.adapter = adapter

        vm.getGoals(session.getUserId()).observe(this) { adapter.submitList(it) }

        b.fabAddGoal.setOnClickListener {
            startActivity(Intent(this, AddGoalActivity::class.java))
        }
    }

    private fun showAddFundsDialog(goalName: String, onConfirm: (Double) -> Unit) {
        val input = EditText(this).also {
            it.hint = "Amount (R)"
            it.inputType = android.text.InputType.TYPE_CLASS_NUMBER or
                    android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL
        }
        AlertDialog.Builder(this)
            .setTitle("Add Funds")
            .setView(input)
            .setPositiveButton("Add") { _, _ ->
                val amount = input.text.toString().toDoubleOrNull()
                if (amount != null && amount > 0) onConfirm(amount)
                else Toast.makeText(this, "Enter a valid amount", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun onSupportNavigateUp(): Boolean { finish(); return true }
}

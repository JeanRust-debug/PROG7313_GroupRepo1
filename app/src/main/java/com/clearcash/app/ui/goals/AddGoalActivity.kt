package com.clearcash.app.ui.goals

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.clearcash.app.data.db.AppDatabase
import com.clearcash.app.data.repository.ClearCashRepository
import com.clearcash.app.databinding.ActivityAddGoalBinding
import com.clearcash.app.utils.SessionManager

class AddGoalActivity : AppCompatActivity() {

    private lateinit var b: ActivityAddGoalBinding
    private lateinit var vm: GoalsViewModel
    private lateinit var session: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityAddGoalBinding.inflate(layoutInflater)
        setContentView(b.root)

        setSupportActionBar(b.toolbar)
        supportActionBar?.run { title = "New Goal"; setDisplayHomeAsUpEnabled(true) }

        session = SessionManager(this)
        vm = ViewModelProvider(this, GoalsViewModel.Factory(
            ClearCashRepository(AppDatabase.getDatabase(this))
        ))[GoalsViewModel::class.java]

        vm.saveResult.observe(this) { result ->
            result.onSuccess { finish() }
                .onFailure { Toast.makeText(this, it.message, Toast.LENGTH_SHORT).show() }
        }

        b.btnSaveGoal.setOnClickListener { save() }
    }

    private fun save() {
        val name = b.etGoalName.text.toString().trim()
        val amountStr = b.etTargetAmount.text.toString().trim()

        if (name.isEmpty()) { b.tilGoalName.error = "Required"; return }
        val amount = amountStr.toDoubleOrNull()
        if (amount == null || amount <= 0) { b.tilTargetAmount.error = "Enter valid amount"; return }
        b.tilGoalName.error = null; b.tilTargetAmount.error = null

        vm.addGoal(name, amount, session.getUserId())
    }

    private fun confirmDiscard() {
        val hasInput = b.etGoalName.text.toString().isNotEmpty() ||
                b.etTargetAmount.text.toString().isNotEmpty()
        if (hasInput) {
            AlertDialog.Builder(this)
                .setTitle("Discard changes?")
                .setMessage("Your unsaved goal will be lost.")
                .setPositiveButton("Discard") { _, _ -> finish() }
                .setNegativeButton("Keep editing", null)
                .show()
        } else {
            finish()
        }
    }

    override fun onSupportNavigateUp(): Boolean { confirmDiscard(); return true }

    @Suppress("DEPRECATION")
    override fun onBackPressed() { confirmDiscard() }
}

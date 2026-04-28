package com.clearcash.app.ui.budget

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.SeekBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.clearcash.app.data.db.AppDatabase
import com.clearcash.app.data.repository.ClearCashRepository
import com.clearcash.app.databinding.ActivityBudgetBinding
import com.clearcash.app.utils.DateUtils
import com.clearcash.app.utils.SessionManager
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

// screen where the user sets their monthly mnimum and maximum spending goals
class BudgetActivity : AppCompatActivity() {

    private lateinit var b: ActivityBudgetBinding
    private lateinit var repo: ClearCashRepository
    private lateinit var session: SessionManager


    // formats numbers as South African Rand currency (e.g. R 1 500,00)
    private val numFormat = NumberFormat.getCurrencyInstance(Locale("en", "ZA"))


    // wont trigger each other in loop
    private var syncingMin = false
    private var syncingMax = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityBudgetBinding.inflate(layoutInflater)
        setContentView(b.root)



        // toolbar with back button
        setSupportActionBar(b.toolbar)
        supportActionBar?.run { title = "Monthly Budget Goals"; setDisplayHomeAsUpEnabled(true) }


        session = SessionManager(this)
        repo    = ClearCashRepository(AppDatabase.getDatabase(this))


        // min spending goal seekbar
        b.seekBarMin.max = 100000
        b.seekBarMin.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(sb: SeekBar?, p: Int, u: Boolean) {
                // update format display
                b.tvMinValue.text = numFormat.format(p)
                // sync text
                if (!syncingMin) {
                    syncingMin = true
                    b.etMinInput.setText(if (p == 0) "" else p.toString())
                    syncingMin = false
                }
                Log.d("BudgetActivity", "Min goal: $p")
            }
            override fun onStartTrackingTouch(sb: SeekBar?) {}
            override fun onStopTrackingTouch(sb: SeekBar?) {}
        })



        // when  user types a number manually. move the min seekbar to match
        b.etMinInput.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if (!syncingMin) {
                    syncingMin = true
                    val value = s.toString().toDoubleOrNull() ?: 0.0
                    // clamp the value so it never exceeds the seekbar maximum
                    b.seekBarMin.progress = value.coerceAtMost(100000.0).toInt()
                    syncingMin = false
                }


            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })



        // ── Maximum spending limit seekbar ────────────────────────────────────
        b.seekBarMax.max = 100000
        b.seekBarMax.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(sb: SeekBar?, p: Int, u: Boolean) {
                b.tvMaxValue.text = numFormat.format(p)
                if (!syncingMax) {
                    syncingMax = true
                    b.etMaxInput.setText(if (p == 0) "" else p.toString())
                    syncingMax = false
                }
                Log.d("BudgetActivity", "Max goal: $p")
            }
            override fun onStartTrackingTouch(sb: SeekBar?) {}
            override fun onStopTrackingTouch(sb: SeekBar?) {}
        })


        // When the user types a number manually, move the max seekbar to match
        b.etMaxInput.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if (!syncingMax) {
                    syncingMax = true
                    val value = s.toString().toDoubleOrNull() ?: 0.0
                    b.seekBarMax.progress = value.coerceAtMost(100000.0).toInt()
                    syncingMax = false
                }
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })



        // load any previously saved budget for this month
        loadExisting()

        b.btnSave.setOnClickListener {
            val min = b.seekBarMin.progress.toDouble()
            val max = b.seekBarMax.progress.toDouble()
            // validate before saving
            if (max <= 0) { Toast.makeText(this, "Set a maximum budget", Toast.LENGTH_SHORT).show(); return@setOnClickListener }
            if (min > max) { Toast.makeText(this, "Min cannot exceed Max", Toast.LENGTH_SHORT).show(); return@setOnClickListener }
            save(min, max)
        }
    }

    // fetch the existing budget from the database and populates the UI
    private fun loadExisting() = lifecycleScope.launch {
        val budget = repo.getBudgetByMonth(session.getUserId(), DateUtils.getCurrentMonth(), DateUtils.getCurrentYear())
        runOnUiThread {
            if (budget != null) {
                // restre  saved slider positions and text inputs
                b.seekBarMin.progress = budget.minGoal.toInt()
                b.seekBarMax.progress = budget.maxGoal.toInt()
                b.tvMinValue.text     = numFormat.format(budget.minGoal)
                b.tvMaxValue.text     = numFormat.format(budget.maxGoal)
                b.etMinInput.setText(budget.minGoal.toInt().toString())
                b.etMaxInput.setText(budget.maxGoal.toInt().toString())
            } else {
                // shows 0
                b.tvMinValue.text = numFormat.format(0)
                b.tvMaxValue.text = numFormat.format(0)
            }
        }
    }

    // saves budget for month
    private fun save(min: Double, max: Double) {
        b.progressBar.visibility = View.VISIBLE; b.btnSave.isEnabled = false
        lifecycleScope.launch {
            repo.saveBudget(session.getUserId(), min, max, DateUtils.getCurrentMonth(), DateUtils.getCurrentYear())
            runOnUiThread {
                b.progressBar.visibility = View.GONE; b.btnSave.isEnabled = true
                Toast.makeText(this@BudgetActivity, "Budget saved!", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    // back arrow
    override fun onSupportNavigateUp(): Boolean { finish(); return true }
}

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

class BudgetActivity : AppCompatActivity() {

    private lateinit var b: ActivityBudgetBinding
    private lateinit var repo: ClearCashRepository
    private lateinit var session: SessionManager
    private val numFormat = NumberFormat.getCurrencyInstance(Locale("en", "ZA"))

    private var syncingMin = false
    private var syncingMax = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityBudgetBinding.inflate(layoutInflater)
        setContentView(b.root)
        setSupportActionBar(b.toolbar)
        supportActionBar?.run { title = "Monthly Budget Goals"; setDisplayHomeAsUpEnabled(true) }

        session = SessionManager(this)
        repo    = ClearCashRepository(AppDatabase.getDatabase(this))

        // ── SeekBar: minimum goal ─────────────────────────────────────────────
        b.seekBarMin.max = 100000
        b.seekBarMin.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(sb: SeekBar?, p: Int, u: Boolean) {
                b.tvMinValue.text = numFormat.format(p)
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

        b.etMinInput.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if (!syncingMin) {
                    syncingMin = true
                    val value = s.toString().toDoubleOrNull() ?: 0.0
                    b.seekBarMin.progress = value.coerceAtMost(100000.0).toInt()
                    syncingMin = false
                }
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        // ── SeekBar: maximum limit ────────────────────────────────────────────
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

        loadExisting()

        b.btnSave.setOnClickListener {
            val min = b.seekBarMin.progress.toDouble()
            val max = b.seekBarMax.progress.toDouble()
            if (max <= 0) { Toast.makeText(this, "Set a maximum budget", Toast.LENGTH_SHORT).show(); return@setOnClickListener }
            if (min > max) { Toast.makeText(this, "Min cannot exceed Max", Toast.LENGTH_SHORT).show(); return@setOnClickListener }
            save(min, max)
        }
    }

    private fun loadExisting() = lifecycleScope.launch {
        val budget = repo.getBudgetByMonth(session.getUserId(), DateUtils.getCurrentMonth(), DateUtils.getCurrentYear())
        runOnUiThread {
            if (budget != null) {
                b.seekBarMin.progress = budget.minGoal.toInt()
                b.seekBarMax.progress = budget.maxGoal.toInt()
                b.tvMinValue.text     = numFormat.format(budget.minGoal)
                b.tvMaxValue.text     = numFormat.format(budget.maxGoal)
                b.etMinInput.setText(budget.minGoal.toInt().toString())
                b.etMaxInput.setText(budget.maxGoal.toInt().toString())
            } else {
                b.tvMinValue.text = numFormat.format(0)
                b.tvMaxValue.text = numFormat.format(0)
            }
        }
    }

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

    override fun onSupportNavigateUp(): Boolean { finish(); return true }
}

package com.clearcash.app.ui.category

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.SeekBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.clearcash.app.data.db.AppDatabase
import com.clearcash.app.data.repository.ClearCashRepository
import com.clearcash.app.databinding.ActivityAddCategoryBinding
import com.clearcash.app.utils.SessionManager
import java.text.NumberFormat
import java.util.Locale

// screen where the user creates a new expense category with an optional spending limit
class AddCategoryActivity : AppCompatActivity() {

    private lateinit var b: ActivityAddCategoryBinding
    private lateinit var vm: CategoryViewModel
    private lateinit var session: SessionManager

    // this formats the limit value as South African Rand currency
    private val numFormat: NumberFormat = NumberFormat.getCurrencyInstance(Locale("en", "ZA"))

    // this flag to prevent the seekbar and EditText from updating each other in a loop
    private var syncing = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityAddCategoryBinding.inflate(layoutInflater)
        setContentView(b.root)

        // set up the toolbar with a back button
        setSupportActionBar(b.toolbar)
        supportActionBar?.run { title = "Add Category"; setDisplayHomeAsUpEnabled(true) }

        session = SessionManager(this)
        vm = ViewModelProvider(this, CategoryViewModel.Factory(
            ClearCashRepository(AppDatabase.getDatabase(this))
        ))[CategoryViewModel::class.java]

        // seekbar max is 50k
        b.seekBarLimit.max = 50000
        b.tvLimitValue.text = "No limit"

        // update display limit
        b.seekBarLimit.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(sb: SeekBar?, progress: Int, fromUser: Boolean) {
                b.tvLimitValue.text = if (progress == 0) "No limit" else numFormat.format(progress)


                if (!syncing) {
                    syncing = true
                    b.etLimitInput.setText(if (progress == 0) "" else progress.toString())
                    syncing = false
                }
                Log.d("AddCategory", "SeekBar progress: $progress")
            }
            override fun onStartTrackingTouch(sb: SeekBar?) {}
            override fun onStopTrackingTouch(sb: SeekBar?) {}
        })



        // when the user types a number. move the seekbar to match
        b.etLimitInput.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if (!syncing) {
                    syncing = true
                    val value = s.toString().toDoubleOrNull() ?: 0.0
                    // Clamp to the seekbar maximum so the slider doesn't go out of range
                    b.seekBarLimit.progress = value.coerceAtMost(50000.0).toInt()
                    syncing = false
                }
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        // observe the save result and show feedback to the user
        vm.saveResult.observe(this) { result ->
            b.progressBar.visibility = View.GONE; b.btnSave.isEnabled = true
            result.onSuccess { Toast.makeText(this, "Category saved!", Toast.LENGTH_SHORT).show(); finish() }
                .onFailure { Toast.makeText(this, it.message, Toast.LENGTH_SHORT).show() }
        }

        b.btnSave.setOnClickListener {
            val name = b.etName.text.toString().trim()
            // validate that a name has been entered
            if (name.isEmpty()) { b.tilName.error = "Name required"; return@setOnClickListener }
            b.tilName.error = null
            b.progressBar.visibility = View.VISIBLE; b.btnSave.isEnabled = false
            // save the category with the current slider value as the spending limit
            vm.add(session.getUserId(), name, b.seekBarLimit.progress.toDouble())
        }
    }

    // handle the toolbar back arrow
    override fun onSupportNavigateUp(): Boolean { finish(); return true }
}

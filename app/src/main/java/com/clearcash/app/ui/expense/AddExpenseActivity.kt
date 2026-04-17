package com.clearcash.app.ui.expense

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.net.Uri
import android.os.*
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModelProvider
import com.clearcash.app.data.db.AppDatabase
import com.clearcash.app.data.db.entities.Expense
import com.clearcash.app.data.repository.ClearCashRepository
import com.clearcash.app.databinding.ActivityAddExpenseBinding
import com.clearcash.app.utils.DateUtils
import com.clearcash.app.utils.SessionManager
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class AddExpenseActivity : AppCompatActivity() {

    private lateinit var b: ActivityAddExpenseBinding
    private lateinit var vm: ExpenseViewModel
    private lateinit var session: SessionManager

    private var selCategoryId: Long? = null
    private var selDate = System.currentTimeMillis()
    private var startTime = ""
    private var endTime   = ""
    private var receiptPath: String? = null
    private var photoUri: Uri? = null

    // Camera launcher
    private val camera = registerForActivityResult(ActivityResultContracts.TakePicture()) { ok ->
        if (ok) { receiptPath = photoUri?.path; b.tvReceiptStatus.text = "✓ Receipt attached"; b.tvReceiptStatus.visibility = View.VISIBLE }
    }
    // Gallery launcher
    private val gallery = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { receiptPath = it.toString(); b.tvReceiptStatus.text = "✓ Receipt attached"; b.tvReceiptStatus.visibility = View.VISIBLE }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityAddExpenseBinding.inflate(layoutInflater)
        setContentView(b.root)
        supportActionBar?.run { title = "Add Expense"; setDisplayHomeAsUpEnabled(true) }

        session = SessionManager(this)
        vm = ViewModelProvider(this, ExpenseViewModel.Factory(
            ClearCashRepository(AppDatabase.getDatabase(this))
        ))[ExpenseViewModel::class.java]

        vm.loadCategories(session.getUserId())

        // Populate category spinner
        vm.categories.observe(this) { cats ->
            val names = cats.map { it.name }
            b.spinnerCategory.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, names)
                .also { it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }
            b.spinnerCategory.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(p: AdapterView<*>?, v: View?, pos: Int, id: Long) { selCategoryId = cats[pos].id }
                override fun onNothingSelected(p: AdapterView<*>?) {}
            }
        }

        vm.saveResult.observe(this) { result ->
            b.progressBar.visibility = View.GONE; b.btnSave.isEnabled = true
            result.onSuccess { finish() }
                .onFailure { Toast.makeText(this, it.message, Toast.LENGTH_SHORT).show() }
        }

        updateDateBtn()

        // ── Event handling ────────────────────────────────────────────────────
        b.btnSelectDate.setOnClickListener { pickDate() }
        b.btnStartTime.setOnClickListener  { pickTime(true) }
        b.btnEndTime.setOnClickListener    { pickTime(false) }
        b.btnTakePhoto.setOnClickListener  { takePhoto() }
        b.btnGallery.setOnClickListener    { gallery.launch("image/*") }
        b.btnSave.setOnClickListener       { save() }
    }

    private fun pickDate() {
        val c = Calendar.getInstance().also { it.timeInMillis = selDate }
        DatePickerDialog(this, { _, y, m, d ->
            Calendar.getInstance().also { it.set(y, m, d); selDate = it.timeInMillis }
            updateDateBtn()
        }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show()
    }

    private fun pickTime(isStart: Boolean) {
        val c = Calendar.getInstance()
        TimePickerDialog(this, { _, h, min ->
            val t = "%02d:%02d".format(h, min)
            if (isStart) { startTime = t; b.btnStartTime.text = "Start: $t" }
            else         { endTime   = t; b.btnEndTime.text   = "End: $t" }
        }, c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), true).show()
    }

    private fun updateDateBtn() { b.btnSelectDate.text = "Date: ${DateUtils.formatForDisplay(selDate)}" }

    private fun takePhoto() {
        val ts   = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val file = File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), "REC_${ts}.jpg")
        photoUri = FileProvider.getUriForFile(this, "${packageName}.provider", file)
        receiptPath = file.absolutePath
        camera.launch(photoUri)
    }

    private fun save() {
        val amountStr = b.etAmount.text.toString().trim()
        val desc      = b.etDescription.text.toString().trim()
        if (amountStr.isEmpty()) { b.tilAmount.error = "Required"; return }
        val amount = amountStr.toDoubleOrNull()
        if (amount == null || amount <= 0) { b.tilAmount.error = "Enter valid amount"; return }
        if (desc.isEmpty()) { b.tilDescription.error = "Required"; return }
        b.tilAmount.error = null; b.tilDescription.error = null

        b.progressBar.visibility = View.VISIBLE; b.btnSave.isEnabled = false
        Log.d("AddExpense", "Saving expense amount=$amount category=$selCategoryId")
        vm.save(Expense(userId = session.getUserId(), categoryId = selCategoryId,
            amount = amount, date = selDate, startTime = startTime, endTime = endTime,
            description = desc, receiptPath = receiptPath))
    }

    override fun onSupportNavigateUp(): Boolean { finish(); return true }
}
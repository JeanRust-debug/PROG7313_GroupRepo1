package com.clearcash.app.ui.expense

import android.Manifest
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.*
import android.util.Log
import android.view.View
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModelProvider
import com.clearcash.app.R
import com.clearcash.app.data.db.AppDatabase
import com.clearcash.app.data.db.entities.Expense
import com.clearcash.app.data.repository.ClearCashRepository
import com.clearcash.app.databinding.ActivityAddExpenseBinding
import com.clearcash.app.ui.category.AddCategoryActivity
import com.clearcash.app.utils.DateUtils
import com.clearcash.app.utils.SessionManager
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

// Screen where the user logs a new expense with amount, category, date, time and optional receipt photo
class AddExpenseActivity : AppCompatActivity() {

    private lateinit var b: ActivityAddExpenseBinding
    private lateinit var vm: ExpenseViewModel
    private lateinit var session: SessionManager

    private var selCategoryId: Long? = null          // ID of the category chosen in the spinner
    private var selDate = System.currentTimeMillis() // Default date is today
    private var startTime = ""
    private var endTime   = ""
    private var receiptPath: String? = null          // File path or URI of the attached receipt
    private var photoUri: Uri? = null
    private var hasCategories = false

    // Launched after the camera finishes — checks if the photo was taken successfully
    private val camera = registerForActivityResult(ActivityResultContracts.TakePicture()) { ok ->
        if (ok) {
            b.tvReceiptStatus.text = "✓ Receipt attached"
            b.tvReceiptStatus.visibility = View.VISIBLE
        } else {
            receiptPath = null
        }
    }

    // Requests the CAMERA permission at runtime (required on Android 6 and above)
    private val cameraPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) launchCamera()
        else Toast.makeText(this,
            "Camera permission is required to take receipt photos",
            Toast.LENGTH_LONG).show()
    }

    // Launched when the user picks an image from the gallery
    private val gallery = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            receiptPath = it.toString()
            b.tvReceiptStatus.text = "✓ Receipt attached"
            b.tvReceiptStatus.visibility = View.VISIBLE
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityAddExpenseBinding.inflate(layoutInflater)
        setContentView(b.root)

        // Set up toolbar with back button
        setSupportActionBar(b.toolbar)
        supportActionBar?.run { title = "Add Expense"; setDisplayHomeAsUpEnabled(true) }

        session = SessionManager(this)
        vm = ViewModelProvider(this, ExpenseViewModel.Factory(
            ClearCashRepository(AppDatabase.getDatabase(this))
        ))[ExpenseViewModel::class.java]

        // Load the user's categories to populate the spinner
        vm.loadCategories(session.getUserId())

        // Populate the category dropdown; prompt to create one if none exist
        vm.categories.observe(this) { cats ->
            hasCategories = cats.isNotEmpty()
            if (!hasCategories) {
                promptToCreateCategory()
                return@observe
            }
            val names = cats.map { it.name }
            b.spinnerCategory.adapter = ArrayAdapter(this, R.layout.spinner_item, names)
                .also { it.setDropDownViewResource(R.layout.spinner_dropdown_item) }
            b.spinnerCategory.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(p: AdapterView<*>?, v: View?, pos: Int, id: Long) {
                    selCategoryId = cats[pos].id
                }
                override fun onNothingSelected(p: AdapterView<*>?) {}
            }
        }

        // Close the screen when the expense is saved successfully
        vm.saveResult.observe(this) { result ->
            b.progressBar.visibility = View.GONE; b.btnSave.isEnabled = true
            result.onSuccess { finish() }
                .onFailure { Toast.makeText(this, it.message, Toast.LENGTH_SHORT).show() }
        }

        updateDateBtn()

        // Wire up all the button click listeners
        b.btnSelectDate.setOnClickListener { pickDate() }
        b.btnStartTime.setOnClickListener  { pickTime(true) }
        b.btnEndTime.setOnClickListener    { pickTime(false) }
        b.btnTakePhoto.setOnClickListener  { takePhoto() }
        b.btnGallery.setOnClickListener    { gallery.launch("image/*") }
        b.btnSave.setOnClickListener       { save() }
    }

    // If the user has no categories, prompt them to create one before continuing
    private fun promptToCreateCategory() {
        AlertDialog.Builder(this)
            .setTitle("No Categories")
            .setMessage("You need to create at least one category before adding expenses.")
            .setPositiveButton("Create Category") { _, _ ->
                startActivity(Intent(this, AddCategoryActivity::class.java))
                finish()
            }
            .setNegativeButton("Cancel") { _, _ -> finish() }
            .setCancelable(false)
            .show()
    }

    // Opens a date picker and stores the selected date as a timestamp
    private fun pickDate() {
        val c = Calendar.getInstance().also { it.timeInMillis = selDate }
        DatePickerDialog(this, { _, y, m, d ->
            Calendar.getInstance().also { it.set(y, m, d); selDate = it.timeInMillis }
            updateDateBtn()
        }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show()
    }

    // Opens a time picker; isStart=true sets the start time, false sets the end time
    private fun pickTime(isStart: Boolean) {
        val c = Calendar.getInstance()
        TimePickerDialog(this, { _, h, min ->
            val t = "%02d:%02d".format(h, min)
            if (isStart) { startTime = t; b.btnStartTime.text = "Start: $t" }
            else         { endTime   = t; b.btnEndTime.text   = "End: $t" }
        }, c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), true).show()
    }

    // Updates the date button label to show the currently selected date
    private fun updateDateBtn() {
        b.btnSelectDate.text = "Date: ${DateUtils.formatForDisplay(selDate)}"
    }

    // Checks for camera permission before launching the camera
    private fun takePhoto() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            == PackageManager.PERMISSION_GRANTED) {
            launchCamera()
        } else {
            cameraPermission.launch(Manifest.permission.CAMERA)
        }
    }

    // Creates a temporary image file and launches the camera to capture a receipt photo
    private fun launchCamera() {
        try {
            val ts = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val dir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
            dir?.mkdirs()
            val file = File(dir, "REC_${ts}.jpg")
            // FileProvider is required to share the file URI with the camera app securely
            photoUri = FileProvider.getUriForFile(this, "${packageName}.provider", file)
            receiptPath = file.absolutePath
            camera.launch(photoUri)
        } catch (e: Exception) {
            Log.e("AddExpense", "Camera launch failed", e)
            Toast.makeText(this, "Could not open camera: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    // Validates all fields and saves the expense to the database
    private fun save() {
        if (!hasCategories) {
            Toast.makeText(this, "Please create a category first", Toast.LENGTH_SHORT).show()
            return
        }

        val amountStr = b.etAmount.text.toString().trim()
        val desc      = b.etDescription.text.toString().trim()
        if (amountStr.isEmpty()) { b.tilAmount.error = "Required"; return }
        val amount = amountStr.toDoubleOrNull()
        if (amount == null || amount <= 0) { b.tilAmount.error = "Enter valid amount"; return }
        if (desc.isEmpty()) { b.tilDescription.error = "Required"; return }
        b.tilAmount.error = null; b.tilDescription.error = null

        // Ensure end time is not before start time
        if (startTime.isNotEmpty() && endTime.isNotEmpty() && endTime < startTime) {
            Toast.makeText(this, "End time must be after start time", Toast.LENGTH_SHORT).show()
            return
        }

        b.progressBar.visibility = View.VISIBLE; b.btnSave.isEnabled = false
        Log.d("AddExpense", "Saving expense amount=$amount category=$selCategoryId")
        vm.save(Expense(userId = session.getUserId(), categoryId = selCategoryId,
            amount = amount, date = selDate, startTime = startTime, endTime = endTime,
            description = desc, receiptPath = receiptPath))
    }

    // Handle the toolbar back arrow
    override fun onSupportNavigateUp(): Boolean { finish(); return true }
}

package com.example.clearcash.ui.expense

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.clearcash.R
import com.example.clearcash.data.db.entities.Expense
import com.example.clearcash.databinding.FragmentExpenseBinding
import com.example.clearcash.ui.category.CategoryViewModel
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale


class ExpenseFragment : Fragment() {

    private val TAG = "ExpenseFragment"

    private var _binding: FragmentExpenseBinding? = null
    private val binding get() = _binding!!

    private val expenseViewModel: ExpenseViewModel by viewModels()
    private val categoryViewModel: CategoryViewModel by viewModels()

    // Holds the selected date as a timestamp
    private var selectedDate: Long = System.currentTimeMillis()

    // Holds the selected category ID
    private var selectedCategoryId: Int = -1

    // Holds the path of the captured/selected photo
    private var photoPath: String? = null

    // URI for the camera photo file
    private var cameraPhotoUri: Uri? = null

    // Map of category name to ID for dropdown
    private var categoryMap: Map<String, Int> = emptyMap()

    // Camera launcher
    private val cameraLauncher = registerForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            // Show preview of captured photo
            photoPath = cameraPhotoUri?.path
            Glide.with(this).load(cameraPhotoUri).into(binding.ivReceiptPreview)
            binding.ivReceiptPreview.visibility = View.VISIBLE
            Log.d(TAG, "Photo captured: $photoPath")
        }
    }

    // Gallery launcher
    private val galleryLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            photoPath = it.toString()
            Glide.with(this).load(it).into(binding.ivReceiptPreview)
            binding.ivReceiptPreview.visibility = View.VISIBLE
            Log.d(TAG, "Photo selected from gallery: $photoPath")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentExpenseBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG, "ExpenseFragment loaded")

        setupDatePicker()
        setupCategoryDropdown()
        setupExpenseList()
        setupButtons()
    }

    /**
     * Sets up the date picker dialog when date field is clicked.
     */
    private fun setupDatePicker() {
        val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        binding.etDate.setText(dateFormat.format(Date(selectedDate)))

        binding.etDate.setOnClickListener {
            val calendar = Calendar.getInstance()
            DatePickerDialog(
                requireContext(),
                { _, year, month, day ->
                    calendar.set(year, month, day)
                    selectedDate = calendar.timeInMillis
                    binding.etDate.setText(dateFormat.format(Date(selectedDate)))
                    Log.d(TAG, "Date selected: ${dateFormat.format(Date(selectedDate))}")
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }
    }

    /**
     * Populates the category dropdown from the database.
     */
    private fun setupCategoryDropdown() {
        categoryViewModel.allCategories.observe(viewLifecycleOwner) { categories ->
            categoryMap = categories.associate { it.name to it.id }
            val categoryNames = categories.map { it.name }

            val adapter = ArrayAdapter(
                requireContext(),
                android.R.layout.simple_dropdown_item_1line,
                categoryNames
            )
            binding.actvCategory.setAdapter(adapter)

            binding.actvCategory.setOnItemClickListener { _, _, position, _ ->
                val selectedName = categoryNames[position]
                selectedCategoryId = categoryMap[selectedName] ?: -1
                Log.d(TAG, "Category selected: $selectedName (ID: $selectedCategoryId)")
            }
        }
    }

    /**
     * Sets up the RecyclerView to show recent expenses.
     * Clicking an item opens the receipt photo viewer.
     */
    private fun setupExpenseList() {
        categoryViewModel.allCategories.observe(viewLifecycleOwner) { categories ->
            val categoryNamesById = categories.associate { it.id to it.name }

            val adapter = ExpenseAdapter(categoryNamesById) { expense ->
                // Open the attached receipt photo in a viewer dialog (or show a message if none)
                openReceiptPhoto(expense)
            }

            binding.rvExpenses.layoutManager = LinearLayoutManager(requireContext())
            binding.rvExpenses.adapter = adapter

            expenseViewModel.allExpenses.observe(viewLifecycleOwner) { expenses ->
                adapter.submitList(expenses)
                if (expenses.isEmpty()) {
                    binding.tvNoExpenses.visibility = View.VISIBLE
                    binding.rvExpenses.visibility = View.GONE
                } else {
                    binding.tvNoExpenses.visibility = View.GONE
                    binding.rvExpenses.visibility = View.VISIBLE
                }
                Log.d(TAG, "Expenses updated: ${expenses.size} items")
            }
        }
    }

    /**
     * Sets up save and photo attachment buttons.
     */
    private fun setupButtons() {
        // Photo attachment — show camera or gallery choice
        binding.btnAttachPhoto.setOnClickListener {
            showPhotoOptions()
        }

        // Save expense
        binding.btnSaveExpense.setOnClickListener {
            saveExpense()
        }
    }

    /**
     * Shows options to take a photo or pick from gallery.
     */
    private fun showPhotoOptions() {
        val options = arrayOf("Take Photo", "Choose from Gallery")
        AlertDialog.Builder(requireContext())
            .setTitle("Attach Receipt")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> launchCamera()
                    1 -> galleryLauncher.launch("image/*")
                }
            }.show()
    }

    /**
     * Creates a temporary file and launches the camera.
     */
    private fun launchCamera() {
        val photoFile = File.createTempFile(
            "receipt_${System.currentTimeMillis()}",
            ".jpg",
            requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        )
        cameraPhotoUri = FileProvider.getUriForFile(
            requireContext(),
            "${requireContext().packageName}.fileprovider",
            photoFile
        )
        cameraLauncher.launch(cameraPhotoUri)
    }

    /**
     * Opens the receipt photo attached to an expense inside a full-size AlertDialog.
     * Handles both local File paths (from camera) and content:// URIs (from gallery).
     */
    private fun openReceiptPhoto(expense: Expense) {
        val path = expense.photoPath
        if (path.isNullOrEmpty()) {
            Toast.makeText(
                requireContext(),
                getString(R.string.no_receipt_attached),
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        val imageView = ImageView(requireContext()).apply {
            adjustViewBounds = true
            scaleType = ImageView.ScaleType.FIT_CENTER
        }

        val loadModel: Any = when {
            path.startsWith("content://") || path.startsWith("file://") -> Uri.parse(path)
            else -> {
                val file = File(path)
                if (file.exists()) file else Uri.parse(path)
            }
        }

        Glide.with(this)
            .load(loadModel)
            .into(imageView)

        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.receipt_photo))
            .setView(imageView)
            .setPositiveButton(getString(R.string.close), null)
            .show()

        Log.d(TAG, "Opened receipt for expense id=${expense.id}")
    }

    /**
     * Validates inputs and saves the expense to the database.
     */
    private fun saveExpense() {
        val amountStr = binding.etAmount.text.toString().trim()
        val description = binding.etDescription.text.toString().trim()

        // Validate amount
        if (amountStr.isEmpty()) {
            binding.tilAmount.error = "Amount is required"
            return
        } else {
            binding.tilAmount.error = null
        }

        val amount = amountStr.toDoubleOrNull()
        if (amount == null || amount <= 0) {
            binding.tilAmount.error = "Enter a valid amount"
            return
        } else {
            binding.tilAmount.error = null
        }

        // Validate description
        if (description.isEmpty()) {
            binding.tilDescription.error = "Description is required"
            return
        } else {
            binding.tilDescription.error = null
        }

        // Validate category selection
        if (selectedCategoryId == -1) {
            Toast.makeText(requireContext(), "Please select a category", Toast.LENGTH_SHORT).show()
            return
        }

        // Create and save expense
        val expense = Expense(
            amount = amount,
            date = selectedDate,
            description = description,
            categoryId = selectedCategoryId,
            photoPath = photoPath
        )

        expenseViewModel.insertExpense(expense)

        // Clear inputs
        binding.etAmount.text?.clear()
        binding.etDescription.text?.clear()
        binding.actvCategory.text?.clear()
        binding.ivReceiptPreview.visibility = View.GONE
        photoPath = null
        selectedCategoryId = -1
        selectedDate = System.currentTimeMillis()

        Toast.makeText(requireContext(), "Expense saved!", Toast.LENGTH_SHORT).show()
        Log.d(TAG, "Expense saved: R$amount - $description")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
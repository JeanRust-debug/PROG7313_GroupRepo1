package com.example.clearcash.ui.expense
import com.example.clearcash.util.ValidationUtils
import android.app.DatePickerDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.clearcash.data.db.entities.Expense
import com.example.clearcash.databinding.FragmentExpenseBinding
import com.example.clearcash.ui.category.CategoryViewModel
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import androidx.navigation.fragment.findNavController
import com.example.clearcash.R


/**
 * Fragment for adding and viewing expense entries.
 * Supports date picking, category selection, and receipt photo attachment.
 * Author: Muaaz Abdool Gaffoor (ST10443760)
 */
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
            cameraPhotoUri?.let { uri ->
                try {
                    // Copy camera photo to permanent location just like gallery
                    val permanentFile = File.createTempFile(
                        "receipt_saved_${System.currentTimeMillis()}",
                        ".jpg",
                        requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES)
                    )
                    requireContext().contentResolver.openInputStream(uri)?.use { input ->
                        permanentFile.outputStream().use { output ->
                            input.copyTo(output)
                        }
                    }
                    photoPath = permanentFile.absolutePath
                    Glide.with(this).load(permanentFile).into(binding.ivReceiptPreview)
                    binding.ivReceiptPreview.visibility = View.VISIBLE
                    Log.d(TAG, "Camera photo saved permanently: $photoPath")
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to save camera photo: ${e.message}")
                    Toast.makeText(requireContext(), "Failed to save photo", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // Gallery launcher
    private val galleryLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            // Copy to local file so we can load it later
            val photoFile = File.createTempFile(
                "receipt_${System.currentTimeMillis()}",
                ".jpg",
                requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES)
            )
            requireContext().contentResolver.openInputStream(it)?.use { input ->
                photoFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
            photoPath = photoFile.absolutePath
            Glide.with(this).load(photoFile).into(binding.ivReceiptPreview)
            binding.ivReceiptPreview.visibility = View.VISIBLE
            Log.d(TAG, "Photo copied from gallery: $photoPath")
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
        // Request camera permission at runtime
        if (requireContext().checkSelfPermission(android.Manifest.permission.CAMERA)
            != android.content.pm.PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(android.Manifest.permission.CAMERA), 100)
        }
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
     */
    private fun setupExpenseList() {
        categoryViewModel.allCategories.observe(viewLifecycleOwner) { categories ->
            val categoryNamesById = categories.associate { it.id to it.name }

            val adapter = ExpenseAdapter(
                categoryNames = categoryNamesById,
                onItemClick = { expense ->
                    val bundle = Bundle().apply {
                        putFloat("amount", expense.amount.toFloat())
                        putLong("date", expense.date)
                        putString("description", expense.description)
                        putInt("categoryId", expense.categoryId)
                        putString("photoPath", expense.photoPath)
                    }
                    findNavController().navigate(
                        R.id.action_expenseFragment_to_expenseDetailFragment,
                        bundle
                    )
                },
                onDeleteClick = { expense ->
                    expenseViewModel.deleteExpense(expense)
                    Toast.makeText(
                        requireContext(),
                        "${expense.description} deleted",
                        Toast.LENGTH_SHORT
                    ).show()
                    Log.d(TAG, "Deleted expense: ${expense.description}")
                }
            )

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
        android.app.AlertDialog.Builder(requireContext())
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
        try {
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
            photoPath = photoFile.absolutePath
            cameraLauncher.launch(cameraPhotoUri)
        } catch (e: Exception) {
            Log.e(TAG, "Camera launch failed: ${e.message}")
            Toast.makeText(requireContext(), "Camera unavailable", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Validates inputs and saves the expense to the database.
     */
    private fun saveExpense() {
        val amountStr = binding.etAmount.text.toString().trim()
        val description = binding.etDescription.text.toString().trim()

        // Validate amount using ValidationUtils
        val amountError = ValidationUtils.validateAmount(amountStr)
        if (amountError != null) {
            binding.tilAmount.error = amountError
            return
        } else {
            binding.tilAmount.error = null
        }

        // Validate description
        val descError = ValidationUtils.validateRequired(description, "Description")
        if (descError != null) {
            binding.tilDescription.error = descError
            return
        } else {
            binding.tilDescription.error = null
        }

        // Validate category
        if (selectedCategoryId == -1) {
            Toast.makeText(requireContext(), "Please select a category", Toast.LENGTH_SHORT).show()
            return
        }

        val expense = Expense(
            amount = amountStr.toDouble(),
            date = selectedDate,
            description = description,
            categoryId = selectedCategoryId,
            photoPath = photoPath
        )

        expenseViewModel.insertExpense(expense)

        // Clear all inputs after save
        binding.etAmount.text?.clear()
        binding.etDescription.text?.clear()
        binding.actvCategory.text?.clear()
        binding.ivReceiptPreview.visibility = View.GONE
        photoPath = null
        selectedCategoryId = -1
        selectedDate = System.currentTimeMillis()

        // Reset date display
        val dateFormat = java.text.SimpleDateFormat("dd MMM yyyy", java.util.Locale.getDefault())
        binding.etDate.setText(dateFormat.format(java.util.Date(selectedDate)))

        Toast.makeText(requireContext(), "Expense saved!", Toast.LENGTH_SHORT).show()
        Log.d(TAG, "Expense saved: R${amountStr.toDouble()} - $description")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
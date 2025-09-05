package vcmsa.projects.budgetingbestie

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.lifecycle.lifecycleScope
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AddExpenses : AppCompatActivity() {
    private lateinit var categorySpinner: Spinner
    private lateinit var etDescription: EditText
    private lateinit var etAmount: EditText
    private lateinit var etDate: EditText
    private lateinit var etReceipt: EditText
    private lateinit var btnUpload: Button
    private lateinit var btnAddExpense: Button
    private var receiptUri: Uri? = null

    private val PICK_IMAGE_REQUEST = 101
    private val CAPTURE_IMAGE_REQUEST = 102
    private val CAMERA_PERMISSION_REQUEST_CODE = 1001
    private var imageUri: Uri? = null
    private lateinit var expenseRepository: ExpenseRepository
    private val firestore = com.google.firebase.firestore.FirebaseFirestore.getInstance()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_expenses)

        // Initialize views
        etDescription = findViewById(R.id.etDescription)
        etAmount = findViewById(R.id.etAmount)
        etDate = findViewById(R.id.etDate)
        etReceipt = findViewById(R.id.etReceipt)
        btnUpload = findViewById(R.id.btnUpload)
        btnAddExpense = findViewById(R.id.btnAddExpense)
        categorySpinner = findViewById(R.id.categorySpinner)

        expenseRepository = ExpenseRepository() // Initialize the repository

        setupCategorySpinner()
        setupDatePicker()
        setupUploadButton()
        setupAddExpenseButton()
    }

    private fun getCurrentUserId(): String {
        return FirebaseAuth.getInstance().currentUser?.uid ?: ""
    }


    private fun setupCategorySpinner() {
        val userId = getCurrentUserId()
        if (userId.isEmpty()) {
            Toast.makeText(this, "No user signed in.", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            val categories = getCategoriesFromFirestore(userId)
            val adapter = ArrayAdapter(
                this@AddExpenses,
                android.R.layout.simple_spinner_dropdown_item,
                categories
            )
            categorySpinner.adapter = adapter
        }
    }


    private suspend fun getCategoriesFromFirestore(userId: String): List<String> {
        return try {
            val snapshot = firestore.collection("categories")
                .whereEqualTo("userId", userId)
                .get()
                .await()

            snapshot.documents.mapNotNull { it.getString("name") }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    private fun setupDatePicker() {
        etDate.isFocusable = false
        etDate.isClickable = true

        etDate.setOnClickListener {
            val datePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText("Select Expense Date")
                .build()

            datePicker.show(supportFragmentManager, "date_picker")

            datePicker.addOnPositiveButtonClickListener { selection ->
                val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                etDate.setText(dateFormat.format(Date(selection)))
            }
        }
    }

    private fun setupUploadButton() {
        btnUpload.setOnClickListener {
            val options = arrayOf("Take Photo", "Choose from Gallery", "Cancel")
            val builder = android.app.AlertDialog.Builder(this)
            builder.setTitle("Add Receipt")

            builder.setItems(options) { dialog, which ->
                when (options[which]) {
                    "Take Photo" -> openCamera()
                    "Choose from Gallery" -> openGallery()
                    "Cancel" -> dialog.dismiss()
                }
            }

            builder.show()
        }
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/*"
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }

    private fun openCamera() {
        if (checkSelfPermission(android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(android.Manifest.permission.CAMERA), CAMERA_PERMISSION_REQUEST_CODE)
            return
        }

        try {
            val imageFile = createImageFile()
            if (imageFile == null) {
                Log.e("AddExpensesActivity", "Image file creation failed")
                Toast.makeText(this, "Unable to create image file", Toast.LENGTH_SHORT).show()
                return
            }

            imageUri = FileProvider.getUriForFile(this, "${packageName}.provider", imageFile)

            val intent = Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE).apply {
                putExtra(android.provider.MediaStore.EXTRA_OUTPUT, imageUri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            startActivityForResult(intent, CAPTURE_IMAGE_REQUEST)
        } catch (e: Exception) {
            Log.e("AddExpensesActivity", "Failed to open camera: ${e.message}", e)
            Toast.makeText(this, "Error launching camera", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openCamera() // Retry now that permission is granted
            } else {
                Toast.makeText(this, "Camera permission denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun createImageFile(): File? {
        return try {
            val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val storageDir: File = getExternalFilesDir(Environment.DIRECTORY_PICTURES)!!
            File.createTempFile("JPEG_${timeStamp}_", ".jpg", storageDir)
        } catch (ex: IOException) {
            ex.printStackTrace()
            null
        }
    }

    private fun setupAddExpenseButton() {
        btnAddExpense.setOnClickListener {
            val category = categorySpinner.selectedItem?.toString()
            val description = etDescription.text.toString().trim()
            val amountText = etAmount.text.toString().trim()
            val date = etDate.text.toString().trim()
            val receipt = receiptUri?.toString() ?: ""

            if (category.isNullOrBlank() || description.isBlank() || amountText.isBlank() || date.isBlank()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val amount = try {
                amountText.toDouble()
            } catch (e: NumberFormatException) {
                Toast.makeText(this, "Invalid amount", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            lifecycleScope.launch {
                val currentUser  = FirebaseAuth.getInstance().currentUser
                if (currentUser  != null) {
                    val expense = Expense(
                        userId = currentUser .uid, // Use Firebase user ID
                        category = category,
                        description = description,
                        date = date,
                        amount = amount.toString(),
                        receiptPhotoUri = receipt
                    )

                    try {
                        expenseRepository.insertExpense(expense) // Use the repository to insert the expense
                        Toast.makeText(this@AddExpenses, "Expense added successfully!", Toast.LENGTH_SHORT).show()
                        finish()
                    } catch (e: Exception) {
                        Toast.makeText(this@AddExpenses, "Error adding expense: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this@AddExpenses, "No signed-in user found!", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                PICK_IMAGE_REQUEST -> {
                    receiptUri = data?.data
                    etReceipt.setText(receiptUri?.lastPathSegment ?: "Image Selected")
                }
                CAPTURE_IMAGE_REQUEST -> {
                    receiptUri = imageUri
                    etReceipt.setText("Photo Captured")
                }
            }
        }
    }
}
package vcmsa.projects.budgetingbestie


import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class EditExpense : AppCompatActivity() {
    private lateinit var categorySpinner: Spinner
    private lateinit var etDescription: EditText
    private lateinit var etAmount: EditText
    private lateinit var etDate: EditText
    private lateinit var etReceipt: EditText
    private lateinit var btnUpload: Button
    private lateinit var btnSave: Button
    private lateinit var btnDelete: Button
    private var receiptUri: Uri? = null
    private var expenseId: String = ""
    private var currentExpense: Expense? = null
    private lateinit var expenseRepository: ExpenseRepository
    private var categories: List<String> = emptyList()
    private val firestore = com.google.firebase.firestore.FirebaseFirestore.getInstance()

    companion object {
        private const val PICK_IMAGE_REQUEST = 102
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_expense)

        initializeViews()
        expenseRepository = ExpenseRepository()
        loadCategories()
        loadExpenseData()
        setupDatePicker()
        setupImageUpload()
        setupSaveButton()
        setupDeleteButton()
    }

    private fun initializeViews() {
        categorySpinner = findViewById(R.id.editCategorySpinner)
        etDescription = findViewById(R.id.editDescription)
        etAmount = findViewById(R.id.editAmount)
        etDate = findViewById(R.id.editDate)
        etReceipt = findViewById(R.id.editReceipt)
        btnUpload = findViewById(R.id.btnUploadEdit)
        btnSave = findViewById(R.id.btnSaveExpense)
        btnDelete = findViewById(R.id.btnDeleteExpense)
    }




    private fun loadCategories() {
        lifecycleScope.launch {
            try {
                // Get the current user ID
                val userId = FirebaseAuth.getInstance().currentUser?.uid
                if (userId == null) {
                    Toast.makeText(this@EditExpense, "User not authenticated", Toast.LENGTH_SHORT).show()
                    return@launch
                }
                categories = getCategoriesFromFirestore(userId)
                val adapter = ArrayAdapter(
                    this@EditExpense,
                    android.R.layout.simple_spinner_dropdown_item,
                    categories
                )
                categorySpinner.adapter = adapter
            } catch (e: Exception) {
                Toast.makeText(this@EditExpense, "Error loading categories: ${e.message}", Toast.LENGTH_SHORT).show()
            }
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
        etDate.setOnClickListener {
            // Implement date picker logic here
        }
    }

    private fun setupImageUpload() {
        btnUpload.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "image/*"
            startActivityForResult(intent, PICK_IMAGE_REQUEST)
        }
    }

    private fun loadExpenseData() {
        expenseId = intent.getStringExtra("expenseId") ?: ""
        if (expenseId.isEmpty()) {
            Toast.makeText(this, "Invalid expense ID", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        lifecycleScope.launch {
            try {
                currentExpense = expenseRepository.getExpenseById(expenseId) // Use repository to get expense
                currentExpense?.let { expense ->
                    etDescription.setText(expense.description)
                    etAmount.setText(expense.amount)
                    etDate.setText(expense.date)

                    if (expense.receiptPhotoUri.isNotEmpty()) {
                        receiptUri = Uri.parse(expense.receiptPhotoUri)
                        etReceipt.setText(receiptUri?.lastPathSegment ?: "Image Selected")
                    }

                    // Set spinner selection
                    val spinnerIndex = categories.indexOf(expense.category)
                    if (spinnerIndex != -1) {
                        categorySpinner.setSelection(spinnerIndex)
                    }
                } ?: run {
                    Toast.makeText(this@EditExpense, "Expense not found", Toast.LENGTH_SHORT).show()
                    finish()
                }
            } catch (e: Exception) {
                Toast.makeText(this@EditExpense, "Error loading expense: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupSaveButton() {
        btnSave.setOnClickListener {
            lifecycleScope.launch {
                try {
                    val expense = currentExpense ?: run {
                        Toast.makeText(this@EditExpense, "No expense data available", Toast.LENGTH_SHORT).show()
                        return@launch
                    }

                    // Validate input
                    val description = etDescription.text.toString().trim()
                    val amount = etAmount.text.toString().trim()
                    val date = etDate.text.toString().trim()
                    val category = categorySpinner.selectedItem?.toString() ?: run {
                        Toast.makeText(this@EditExpense, "Please select a category", Toast.LENGTH_SHORT).show()
                        return@launch
                    }

                    if (description.isBlank() || amount.isBlank() || date.isBlank()) {
                        Toast.makeText(this@EditExpense, "Please fill in all required fields", Toast.LENGTH_SHORT).show()
                        return@launch
                    }

                    // Update expense
                    expense.apply {
                        this.description = description
                        this.amount = amount
                        this.date = date
                        this.category = category
                        receiptPhotoUri = receiptUri?.toString() ?: receiptPhotoUri
                    }

                    expenseRepository.updateExpense(expense) // Use repository to update expense
                    Toast.makeText(this@EditExpense, "Expense updated successfully!", Toast.LENGTH_SHORT).show()
                    finish()
                } catch (e: Exception) {
                    Toast.makeText(this@EditExpense, "Error updating expense: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun setupDeleteButton() {
        btnDelete.setOnClickListener {
            lifecycleScope.launch {
                try {
                    val expense = currentExpense ?: run {
                        Toast.makeText(this@EditExpense, "No expense data available", Toast.LENGTH_SHORT).show()
                        return@launch
                    }

                    expenseRepository.deleteExpense(expense) // Use repository to delete expense
                    Toast.makeText(this@EditExpense, "Expense deleted successfully!", Toast.LENGTH_SHORT).show()
                    finish()
                } catch (e: Exception) {
                    Toast.makeText(this@EditExpense, "Error deleting expense: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK) {
            data?.data?.let { uri ->
                receiptUri = uri
                etReceipt.setText(uri.lastPathSegment ?: "Image Selected")
            }
        }
    }
}

package vcmsa.projects.budgetingbestie

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AllExpenses : AppCompatActivity() {
    private lateinit var addButton: Button
    private lateinit var dateRangePickerButton: Button
    private lateinit var expenseRecyclerView: RecyclerView
    private lateinit var totalTextView: TextView
    private var selectedStartDate: String? = null
    private var selectedEndDate: String? = null
    private lateinit var adapter: ExpenseAdapter
    private val allExpenses = mutableListOf<Expense>()
    private val filteredExpenses = mutableListOf<Expense>()
    private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    private lateinit var expenseRepository: ExpenseRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_all_expenses)

        expenseRepository = ExpenseRepository()
        initializeViews()
        setupRecyclerView()
        setupDateRangePicker()
        setupAddButton()
        loadExpensesFromFirestore()
    }

    private fun initializeViews() {
        expenseRecyclerView = findViewById(R.id.expensesRecyclerView)
        addButton = findViewById(R.id.btnAddExpense)
        dateRangePickerButton = findViewById(R.id.dateRangePickerButton)
        totalTextView = findViewById(R.id.tvTotalExpenses)
    }

    private fun setupRecyclerView() {
        adapter = ExpenseAdapter(this, filteredExpenses)
        expenseRecyclerView.layoutManager = LinearLayoutManager(this)
        expenseRecyclerView.adapter = adapter
    }
    private fun setupAddButton() {
        addButton.setOnClickListener {
            val intent = Intent(this, AddExpenses::class.java)
            startActivity(intent)
        }
    }

    private fun setupDateRangePicker() {
        dateRangePickerButton.setOnClickListener {
            val dateRangePicker = MaterialDatePicker.Builder.dateRangePicker()
                .setTitleText("Select Expense Date Range")
                .build()

            dateRangePicker.addOnPositiveButtonClickListener { selection ->
                val startDateMillis = selection.first
                val endDateMillis = selection.second

                selectedStartDate = dateFormat.format(Date(startDateMillis))
                selectedEndDate = dateFormat.format(Date(endDateMillis))

                dateRangePickerButton.text = "$selectedStartDate to $selectedEndDate"
                filterExpenses()
            }

            dateRangePicker.show(supportFragmentManager, "date_range_picker")
        }
    }

    private fun loadExpensesFromFirestore() {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser == null) {
            totalTextView.text = "User not logged in"
            return
        }

        FirebaseFirestore.getInstance()
            .collection("expenses")
            .whereEqualTo("userId", currentUser.uid)
            .get()
            .addOnSuccessListener { result ->
                allExpenses.clear()
                for (doc in result) {
                    val expense = doc.toObject(Expense::class.java).copy(id = doc.id)
                    allExpenses.add(expense)
                }
                filterExpenses()
            }
            .addOnFailureListener {
                totalTextView.text = "Error loading expenses"
            }
    }

    private fun filterExpenses() {
        CoroutineScope(Dispatchers.Default).launch {
            try {
                val filtered = allExpenses.filter { expense ->
                    if (selectedStartDate == null || selectedEndDate == null) {
                        true
                    } else {
                        try {
                            val expenseDate = dateFormat.parse(expense.date)
                            val startDate = dateFormat.parse(selectedStartDate!!)
                            val endDate = dateFormat.parse(selectedEndDate!!)

                            expenseDate != null &&
                                    !expenseDate.before(startDate) &&
                                    !expenseDate.after(endDate)
                        } catch (e: Exception) {
                            true
                        }
                    }
                }

                withContext(Dispatchers.Main) {
                    filteredExpenses.clear()
                    filteredExpenses.addAll(filtered)
                    adapter.updateData(filteredExpenses)
                    calculateTotalExpenses(filteredExpenses)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    totalTextView.text = "Error filtering expenses"
                }
            }
        }
    }

    private fun calculateTotalExpenses(expenses: List<Expense>) {
        var total = 0.0
        for (expense in expenses) {
            try {
                total += expense.amount.toDouble()
            } catch (e: NumberFormatException) {
                // Optionally log the error or ignore silently
            }
        }
        totalTextView.text = "Total: R%.2f".format(total)
    }

    override fun onResume() {
        super.onResume()
        loadExpensesFromFirestore()
    }
}
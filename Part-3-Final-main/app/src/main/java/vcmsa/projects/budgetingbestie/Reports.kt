package vcmsa.projects.budgetingbestie

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.io.File
import java.text.SimpleDateFormat
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.LinearLayoutManager
import android.util.Log
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.util.*

class Reports : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var budgetAdapter: BudgetAdapter
    private val budgetList = mutableListOf<Budget>()
    private val expenseList = mutableListOf<Expense>()

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reports)

        recyclerView = findViewById(R.id.recyclerViewBudgets)
        recyclerView.layoutManager = LinearLayoutManager(this)
        budgetAdapter = BudgetAdapter(budgetList)
        recyclerView.adapter = budgetAdapter

        val btnDownload: Button = findViewById(R.id.btnDownload)
        btnDownload.setOnClickListener {
            generateAndSaveReport()
        }
        loadBudgets()
        loadExpenses()

        // Navigation FABs
        findViewById<FloatingActionButton>(R.id.fabDashboard).setOnClickListener {
            startActivity(Intent(this, Dashboard::class.java))
        }
        findViewById<FloatingActionButton>(R.id.fabBudget).setOnClickListener {
            startActivity(Intent(this, BudgetMain::class.java))
        }
        findViewById<FloatingActionButton>(R.id.fabExpenses).setOnClickListener {
            startActivity(Intent(this, ExpensesMain::class.java))
        }
    }

    private fun loadBudgets() {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
            return
        }

        db.collection("budgets")
            .whereEqualTo("userId", currentUser.uid)
            .get()
            .addOnSuccessListener { querySnapshot ->
                budgetList.clear()
                for (document in querySnapshot.documents) {
                    val budget = document.toObject(Budget::class.java)
                    budget?.id = document.id
                    if (budget != null) {
                        budgetList.add(budget)
                    }
                }
                budgetAdapter.notifyDataSetChanged()
            }
            .addOnFailureListener { exception ->
                Log.e("Reports", "Error loading budgets", exception)
                Toast.makeText(this, "Failed to load budgets.", Toast.LENGTH_SHORT).show()
            }

    }

    private fun loadExpenses() {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
            return
        }

        db.collection("expenses")
            .whereEqualTo("userId", currentUser.uid)
            .get()
            .addOnSuccessListener { querySnapshot ->
                expenseList.clear()
                for (document in querySnapshot.documents) {
                    val expense = document.toObject(Expense::class.java)
                    val expenseWithId = expense?.copy(id = document.id)
                    if (expenseWithId != null) {
                        expenseList.add(expenseWithId)
                    }
                }
            }
            .addOnFailureListener { exception ->
                Log.e("Reports", "Error loading expenses", exception)
                Toast.makeText(this, "Failed to load expenses.", Toast.LENGTH_SHORT).show()
            }
    }
    private fun generateAndSaveReport() {
        if (budgetList.isEmpty() && expenseList.isEmpty()) {
            Toast.makeText(this, "No data to save.", Toast.LENGTH_SHORT).show()
            return
        }

        val fileName = "Budget_Report_${
            SimpleDateFormat(
                "yyyyMMdd_HHmmss",
                Locale.getDefault()
            ).format(Date())
        }.txt"
        val fileContents = StringBuilder()
        fileContents.append("Your Monthly Budget and Expense Report\n")
        fileContents.append("Generated on: ${Date()}\n\n")

        if (budgetList.isNotEmpty()) {
            fileContents.append("=== Budgets ===\n")
            for (budget in budgetList) {
                fileContents.append("Name: ${budget.name}\n")
                fileContents.append("Amount: R${budget.amount}\n\n")
            }
        }

        if (expenseList.isNotEmpty()) {
            fileContents.append("=== Expenses ===\n")
            for (expense in expenseList) {
                fileContents.append("Category: ${expense.category}\n")
                fileContents.append("Description: ${expense.description}\n")
                fileContents.append("Amount: R${expense.amount}\n")
                val formattedDate = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(expense.createdAt.toDate())
                fileContents.append("Date: $formattedDate\n\n")

            }
        }

        try {
            val downloadsDir =
                getExternalFilesDir(null)  // You can replace with Environment.DIRECTORY_DOWNLOADS if you want it in Downloads
            val file = File(downloadsDir, fileName)
            file.writeText(fileContents.toString())

            Toast.makeText(this, "Report saved: ${file.absolutePath}", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Failed to save report", Toast.LENGTH_SHORT).show()
        }
    }

}
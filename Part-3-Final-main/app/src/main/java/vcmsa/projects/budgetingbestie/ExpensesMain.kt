package vcmsa.projects.budgetingbestie

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import vcmsa.projects.budgetingbestie.databinding.ActivityExpensesMainBinding

class ExpensesMain : AppCompatActivity() {
    private lateinit var binding: ActivityExpensesMainBinding
    private lateinit var expenseRepository: ExpenseRepository
    private var currentUserId: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityExpensesMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        expenseRepository = ExpenseRepository()


        lifecycleScope.launch {

            val user = getCurrentUser()
            if (user == null) {
                startActivity(Intent(this@ExpensesMain, LoginActivity::class.java))
                finish()
                return@launch
            } else {
                currentUserId = user.uid
                loadLatestTransactions()
                loadLatestCategories()
            }
        }

        binding.tvViewAllTransactions.setOnClickListener {
            startActivity(Intent(this, AllExpenses::class.java))
        }

        binding.tvViewAllCategories.setOnClickListener {
            startActivity(Intent(this, CategoryMain::class.java))
        }

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

    override fun onResume() {
        super.onResume()
        if (currentUserId != "") {
            loadLatestTransactions()
            loadLatestCategories()
        }
    }

    private fun getCurrentUser(): FirebaseUser? {
        return FirebaseAuth.getInstance().currentUser
    }

    private fun loadLatestTransactions() {
        lifecycleScope.launch {
            try {
                val latestExpenses = expenseRepository.getLatestThreeExpenses(currentUserId)
                withContext(Dispatchers.Main) {
                    binding.transactionsContainer.removeAllViews()
                    if (latestExpenses.isEmpty()) {
                        val noTransactions = TextView(this@ExpensesMain).apply {
                            text = "No transactions yet"
                            setTextColor(ContextCompat.getColor(this@ExpensesMain, android.R.color.darker_gray))
                            textSize = 14f
                        }
                        binding.transactionsContainer.addView(noTransactions)
                    } else {
                        for (expense in latestExpenses) {
                            val transactionView = LayoutInflater.from(this@ExpensesMain)
                                .inflate(R.layout.item_expense_mini, binding.transactionsContainer, false)

                            transactionView.findViewById<TextView>(R.id.tvMiniDescription).text = expense.description
                            transactionView.findViewById<TextView>(R.id.tvMiniAmount).text = "R${expense.amount}"
                            transactionView.findViewById<TextView>(R.id.tvMiniCategory).text = expense.category
                            transactionView.findViewById<TextView>(R.id.tvMiniDate).text = expense.date

                            binding.transactionsContainer.addView(transactionView)
                        }
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {

                }
            }
        }
    }


    private fun loadLatestCategories() {
        lifecycleScope.launch {
            try {

                val latestCategories = loadLatestCategoriesFromFirestore(currentUserId)
                withContext(Dispatchers.Main) {
                    binding.categoriesContainer.removeAllViews()
                    if (latestCategories.isEmpty()) {
                        val noCategories = TextView(this@ExpensesMain).apply {
                            text = "No categories yet"
                            setTextColor(ContextCompat.getColor(this@ExpensesMain, android.R.color.darker_gray))
                            textSize = 14f
                        }
                        binding.categoriesContainer.addView(noCategories)
                    } else {
                        for (category in latestCategories) {
                            val categoryView = LayoutInflater.from(this@ExpensesMain)
                                .inflate(R.layout.item_category_mini, binding.categoriesContainer, false)

                            categoryView.findViewById<TextView>(R.id.tvMiniCategoryName).text = category.name
                            binding.categoriesContainer.addView(categoryView)
                        }
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {

                }
            }
        }
    }

    private suspend fun loadLatestCategoriesFromFirestore(userId: String): List<Category> {
        val categoryRepository = CategoryRepository()
        return categoryRepository.getLatestThreeCategories(userId)
    }

}


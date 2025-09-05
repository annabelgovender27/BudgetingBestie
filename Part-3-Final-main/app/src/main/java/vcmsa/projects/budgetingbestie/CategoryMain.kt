package vcmsa.projects.budgetingbestie

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.launch
import vcmsa.projects.budgetingbestie.databinding.ActivityCategoryMainBinding

class CategoryMain : AppCompatActivity() {

    private lateinit var binding: ActivityCategoryMainBinding
    private lateinit var allCategoriesAdapter: CategoryAdapter
    private val categoryRepository = CategoryRepository()
    private var currentUserId: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCategoryMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Get signed-in user
        val currentUser = getCurrentUser()
        if (currentUser == null) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        currentUserId = currentUser.uid

        setupUI()
        loadCategories()
    }

    private fun getCurrentUser(): FirebaseUser? {
        return FirebaseAuth.getInstance().currentUser
    }

    private fun setupUI() {
        allCategoriesAdapter = CategoryAdapter(emptyList())
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = allCategoriesAdapter

        binding.btnCreate.setOnClickListener {
            startActivity(Intent(this, AddCategory::class.java))
        }

        binding.btnFilter.setOnClickListener {
            startActivity(Intent(this, CategoryTotals::class.java))
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

    private fun loadCategories() {
        lifecycleScope.launch {
            try {
                val categories = categoryRepository.getCategoriesForUser(currentUserId)
                allCategoriesAdapter.updateList(categories)
            } catch (e: Exception) {
                Toast.makeText(this@CategoryMain, "Failed to load categories: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (currentUserId.isNotEmpty()) {
            loadCategories()
        }
    }
}
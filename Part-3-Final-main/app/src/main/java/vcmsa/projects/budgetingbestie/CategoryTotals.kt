package vcmsa.projects.budgetingbestie

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.firebase.Timestamp
import kotlinx.coroutines.launch
import vcmsa.projects.budgetingbestie.databinding.ActivityCategoryTotalsBinding
import java.util.*

class CategoryTotals : AppCompatActivity() {

    private lateinit var binding: ActivityCategoryTotalsBinding
    private lateinit var totalsAdapter: CategoryTotalAdapter
    private val expenseRepository = ExpenseRepository()
    private var currentUserId: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCategoryTotalsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        currentUserId = getCurrentUserId()
        if (currentUserId.isEmpty()) {
            Toast.makeText(this, "No signed-in user found", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        totalsAdapter = CategoryTotalAdapter(emptyList())
        binding.rvCategoryTotals.layoutManager = LinearLayoutManager(this)
        binding.rvCategoryTotals.adapter = totalsAdapter

        // Load all-time totals on startup
        val defaultStart = Timestamp(Date(0)) // From Unix epoch
        val defaultEnd = Timestamp(Date())    // To now
        loadCategoryTotals(defaultStart, defaultEnd)

        binding.btnPickDates.setOnClickListener {
            showDateRangePicker()
        }
    }

    private fun showDateRangePicker() {
        val dateRangePicker = MaterialDatePicker.Builder.dateRangePicker()
            .setTitleText("Select Category Totals Date Range")
            .build()

        dateRangePicker.show(supportFragmentManager, "date_range_picker")

        dateRangePicker.addOnPositiveButtonClickListener { selection ->
            val startDateMillis = selection.first ?: return@addOnPositiveButtonClickListener
            val endDateMillis = selection.second ?: return@addOnPositiveButtonClickListener

            val startTimestamp = Timestamp(Date(startDateMillis))
            val endTimestamp = Timestamp(Date(endDateMillis))

            val dateFormat = java.text.SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val formattedStart = dateFormat.format(Date(startDateMillis))
            val formattedEnd = dateFormat.format(Date(endDateMillis))

            binding.tvSelectedDates.text = "From: $formattedStart To: $formattedEnd"

            loadCategoryTotals(startTimestamp, endTimestamp)
        }
    }

    private fun loadCategoryTotals(start: Timestamp, end: Timestamp) {
        lifecycleScope.launch {
            try {
                val totals = expenseRepository.getCategoryTotalsBetween(currentUserId, start, end)
                if (totals.isEmpty()) {
                    Toast.makeText(this@CategoryTotals, "No expenses found in this range.", Toast.LENGTH_SHORT).show()
                }
                totalsAdapter.updateData(totals)
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(this@CategoryTotals, "Error loading totals: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun getCurrentUserId(): String {
        val user = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser
        return user?.uid ?: ""
    }

}

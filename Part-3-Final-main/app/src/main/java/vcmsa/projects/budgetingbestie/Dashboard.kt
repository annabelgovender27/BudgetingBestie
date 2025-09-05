//?package vcmsa.projects.budgetingbestie
//
//import android.content.Intent
//import android.graphics.Color
//import android.os.Bundle
//import android.widget.Button
//import android.widget.Toast
//import androidx.appcompat.app.AppCompatActivity
//import com.github.mikephil.charting.charts.BarChart
//import com.github.mikephil.charting.charts.PieChart
//import com.github.mikephil.charting.data.PieData
//import com.github.mikephil.charting.data.PieDataSet
//import com.github.mikephil.charting.data.PieEntry
//import com.github.mikephil.charting.utils.ColorTemplate
//import com.google.firebase.auth.FirebaseAuth
//import com.google.firebase.firestore.FirebaseFirestore
//
//class Dashboard : AppCompatActivity() {
//
//    private lateinit var db: FirebaseFirestore
//    private var userId: String? = null
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_dashboard)
//
//        db = FirebaseFirestore.getInstance()
//        val currentUser = FirebaseAuth.getInstance().currentUser
//        userId = currentUser?.uid
//
//        if (userId == null) {
//            Toast.makeText(this, "Unauthorized access. Please log in.", Toast.LENGTH_LONG).show()
//            startActivity(Intent(this, LoginActivity::class.java).apply {
//                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
//            })
//            finish()
//            return
//        }
//
//        val pieChart = findViewById<PieChart>(R.id.pieChart)
//        val barChart = findViewById<BarChart>(R.id.barChart)
//
//        loadExpensesData(pieChart)
//        loadMonthlySpendingData(barChart)
//
//        findViewById<Button>(R.id.btnExpenses).setOnClickListener {
//            startActivity(Intent(this, ExpensesMain::class.java))
//        }
//
//        findViewById<Button>(R.id.btnProfile).setOnClickListener {
//            val intent = Intent(this, ProfileActivity::class.java)
//            intent.putExtra("USER_ID", userId)
//            startActivity(intent)
//        }
//
//        findViewById<Button>(R.id.btnSignOut).setOnClickListener {
//            FirebaseAuth.getInstance().signOut()
//            startActivity(Intent(this, LoginActivity::class.java).apply {
//                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
//            })
//            finish()
//        }
//
//        // Uncomment if these buttons/activities are implemented
//        /*
//        findViewById<Button>(R.id.btnBudgets).setOnClickListener {
//            startActivity(Intent(this, BudgetMain::class.java))
//        }
//
//        findViewById<Button>(R.id.btnReports).setOnClickListener {
//            startActivity(Intent(this, Reports::class.java))
//        }
//
//        findViewById<Button>(R.id.btnSavings).setOnClickListener {
//            startActivity(Intent(this, SavingsMain::class.java))
//        }
//        */
//    }
//
//    private fun loadExpensesData(pieChart: PieChart) {
//        userId?.let { uid ->
//            db.collection("expenses")
//                .whereEqualTo("userId", uid)
//                .get()
//                .addOnSuccessListener { documents ->
//                    val categoryTotals = mutableMapOf<String, Float>()
//
//                    for (doc in documents) {
//                        val category = doc.getString("category") ?: "Unknown"
//
//                        // Safely handle the 'amount' field
//                        val amountField = doc.get("amount")
//                        val amount = when (amountField) {
//                            is Number -> amountField.toFloat()
//                            is String -> amountField.toFloatOrNull() ?: 0f
//                            else -> 0f
//                        }
//
//                        categoryTotals[category] = categoryTotals.getOrDefault(category, 0f) + amount
//                    }
//
//                    if (categoryTotals.isEmpty()) {
//                        Toast.makeText(this, "No expenses found.", Toast.LENGTH_SHORT).show()
//                        pieChart.clear()
//                        return@addOnSuccessListener
//                    }
//
//                    // Prepare PieChart entries
//                    val entries = categoryTotals.map { (category, total) ->
//                        PieEntry(total, category)
//                    }
//
//                    val dataSet = PieDataSet(entries, "Expenses by Category").apply {
//                        colors = ColorTemplate.MATERIAL_COLORS.toList() + ColorTemplate.COLORFUL_COLORS.toList()
//                        valueTextSize = 14f
//                        valueTextColor = Color.WHITE
//                    }
//
//                    val pieData = PieData(dataSet)
//
//                    pieChart.apply {
//                        data = pieData
//                        description.isEnabled = false
//                        setUsePercentValues(true)
//                        legend.isEnabled = true
//                        animateY(1000)
//                        invalidate()
//                    }
//                }
//                .addOnFailureListener { e ->
//                    e.printStackTrace()
//                    Toast.makeText(this, "Failed to load expenses data.", Toast.LENGTH_SHORT).show()
//                }
//        }
//    }
//}
//

package vcmsa.projects.budgetingbestie

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.PercentFormatter
import com.github.mikephil.charting.utils.ColorTemplate
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class Dashboard : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private var userId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        db = FirebaseFirestore.getInstance()
        val currentUser = FirebaseAuth.getInstance().currentUser
        userId = currentUser?.uid

        if (userId == null) {
            Toast.makeText(this, "Unauthorized access. Please log in.", Toast.LENGTH_LONG).show()
            startActivity(Intent(this, LoginActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            })
            finish()
            return
        }

        val pieChart = findViewById<PieChart>(R.id.pieChart)

        loadExpensesData(pieChart)

        findViewById<Button>(R.id.btnProfile).setOnClickListener {
            val intent = Intent(this, ProfileActivity::class.java)
            intent.putExtra("USER_ID", userId)
            startActivity(intent)
        }

        findViewById<Button>(R.id.btnBudgets).setOnClickListener {
            val intent = Intent(this, BudgetMain::class.java)
            intent.putExtra("USER_ID", userId)
            startActivity(intent)
        }

        findViewById<Button>(R.id.btnExpenses).setOnClickListener {
            val intent = Intent(this, ExpensesMain::class.java)
            intent.putExtra("USER_ID", userId)
            startActivity(intent)
        }

        findViewById<Button>(R.id.btnReports).setOnClickListener {
            val intent = Intent(this, Reports::class.java)
            intent.putExtra("USER_ID", userId)
            startActivity(intent)
        }

        findViewById<Button>(R.id.btnTrophyCase).setOnClickListener {
            val intent = Intent(this, TrophyCase::class.java)
            intent.putExtra("USER_ID", userId)
            startActivity(intent)
        }

        findViewById<Button>(R.id.btnSignOut).setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            startActivity(Intent(this, LoginActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            })
            finish()
        }
    }

    private fun loadExpensesData(pieChart: PieChart) {
        userId?.let { uid ->
            db.collection("expenses")
                .whereEqualTo("userId", uid)
                .get()
                .addOnSuccessListener { documents ->
                    val categoryTotals = mutableMapOf<String, Float>()

                    for (doc in documents) {
                        val category = doc.getString("category") ?: "Unknown"
                        val amountField = doc.get("amount")
                        val amount = when (amountField) {
                            is Number -> amountField.toFloat()
                            is String -> amountField.toFloatOrNull() ?: 0f
                            else -> 0f
                        }
                        categoryTotals[category] = categoryTotals.getOrDefault(category, 0f) + amount
                    }

                    if (categoryTotals.isEmpty()) {
                        Toast.makeText(this, "No expenses found.", Toast.LENGTH_SHORT).show()
                        pieChart.clear()
                        return@addOnSuccessListener
                    }

                    val entries = categoryTotals.map { (category, total) ->
                        PieEntry(total, category) // Keep category for legend
                    }

                    val dataSet = PieDataSet(entries, "").apply {
                        colors = ColorTemplate.MATERIAL_COLORS.toList() + ColorTemplate.COLORFUL_COLORS.toList()
                        valueTextSize = 10f
                        valueTextColor = Color.BLACK
                    }

                    // Format the values to show percentages with "%" symbol
                    val percentFormatter = PercentFormatter(pieChart)
                    val pieData = PieData(dataSet).apply {
                        setValueFormatter(percentFormatter)
                    }

                    pieChart.apply {
                        data = pieData
                        description.isEnabled = false
                        setUsePercentValues(true)
                        legend.isEnabled = true
                        animateY(1000)
                        setEntryLabelColor(Color.TRANSPARENT) // Hide labels on slices
                        invalidate()
                    }
                }
                .addOnFailureListener {
                    it.printStackTrace()
                    Toast.makeText(this, "Failed to load expenses data.", Toast.LENGTH_SHORT).show()
                }
        }
    }

}
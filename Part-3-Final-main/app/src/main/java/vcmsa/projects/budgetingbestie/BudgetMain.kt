package vcmsa.projects.budgetingbestie

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.utils.ColorTemplate
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import vcmsa.projects.budgetingbestie.databinding.ActivityBudgetMainBinding

class BudgetMain : AppCompatActivity() {
    private lateinit var binding: ActivityBudgetMainBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private var userId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBudgetMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize Firebase Auth and Firestore
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // Get current user
        val currentUser = auth.currentUser
        userId = currentUser?.uid

        if (currentUser == null) {
            // User not logged in, redirect to Login
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        } else {
            // User logged in, proceed with setup
            setupUI()
            // Load the budget spending data into the chart
            loadBudgetSpendingData(binding.barChart)
        }
    }

    private fun setupUI() {
        // Button navigation
        binding.btnSetBudget.setOnClickListener {
            startActivity(Intent(this, SetBudget::class.java))
        }

        binding.btnGoals.setOnClickListener {
            startActivity(Intent(this, SetGoals::class.java))
        }

        // Load Budgets into RecyclerView
        val budgetAdapter = BudgetAdapter(emptyList())
        binding.recyclerView.adapter = budgetAdapter
        binding.recyclerView.layoutManager = LinearLayoutManager(this)

        db.collection("budgets")
            .whereEqualTo("userId", userId)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Toast.makeText(this, "Error loading budgets", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }
                val budgets = snapshot?.toObjects(Budget::class.java) ?: emptyList()
                budgetAdapter.updateList(budgets)
            }

        // Load Goals into RecyclerView
        val goalsAdapter = GoalsAdapter(emptyList())
        binding.recyclerViewG.adapter = goalsAdapter
        binding.recyclerViewG.layoutManager = LinearLayoutManager(this)

        db.collection("goals")
            .whereEqualTo("userId", userId)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Toast.makeText(this, "Error loading goals", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }
                val goals = snapshot?.toObjects(Goals::class.java) ?: emptyList()
                goalsAdapter.updateGoalsList(goals)
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

    private fun loadBudgetSpendingData(barChart: BarChart) {
        userId?.let { uid ->

            // Step 1: Fetch user goals (min, max)
            db.collection("goals")
                .whereEqualTo("userId", uid)
                .limit(1)
                .get()
                .addOnSuccessListener { goalsDocs ->

                    if (goalsDocs.isEmpty) {
                        Toast.makeText(this, "No goals found for user.", Toast.LENGTH_SHORT).show()
                        barChart.clear()
                        return@addOnSuccessListener
                    }

                    val goalDoc = goalsDocs.documents[0]
                    val minGoal = goalDoc.getDouble("min")?.toFloat() ?: 0f
                    val maxGoal = goalDoc.getDouble("max")?.toFloat() ?: 0f

                    // Step 2: Fetch budgets (to get labels)
                    db.collection("budgets")
                        .whereEqualTo("userId", uid)
                        .get()
                        .addOnSuccessListener { budgetDocs ->

                            val budgetNames = budgetDocs.mapNotNull { it.getString("name") }

                            if (budgetNames.isEmpty()) {
                                Toast.makeText(this, "No budgets found.", Toast.LENGTH_SHORT).show()
                                barChart.clear()
                                return@addOnSuccessListener
                            }

                            // Step 3: Fetch all expenses for user and sum amounts
                            db.collection("expenses")
                                .whereEqualTo("userId", uid)
                                .get()
                                .addOnSuccessListener { expenseDocs ->

                                    var totalExpenses = 0f
                                    for (doc in expenseDocs) {
                                        val amountField = doc.get("amount")
                                        val amount = when (amountField) {
                                            is Number -> amountField.toFloat()
                                            is String -> amountField.toFloatOrNull() ?: 0f
                                            else -> 0f
                                        }
                                        totalExpenses += amount
                                    }

                                    // Step 4: Prepare bar entries: one bar per dataset per budget (grouped bars)
                                    val minGoalEntries = mutableListOf<BarEntry>()
                                    val expensesEntries = mutableListOf<BarEntry>()
                                    val maxGoalEntries = mutableListOf<BarEntry>()
                                    val labels = mutableListOf<String>()

                                    budgetNames.forEachIndexed { index, name ->
                                        // x = index for all datasets
                                        minGoalEntries.add(BarEntry(index.toFloat(), minGoal))
                                        expensesEntries.add(BarEntry(index.toFloat(), totalExpenses))
                                        maxGoalEntries.add(BarEntry(index.toFloat(), maxGoal))
                                        labels.add(name)
                                    }

                                    // Create BarDataSets for each
                                    val minGoalDataSet = BarDataSet(minGoalEntries, "Minimum Goal").apply {
                                        color = ColorTemplate.COLORFUL_COLORS[0]
                                    }
                                    val expensesDataSet = BarDataSet(expensesEntries, "Total Expenses").apply {
                                        color = ColorTemplate.COLORFUL_COLORS[1]
                                    }
                                    val maxGoalDataSet = BarDataSet(maxGoalEntries, "Maximum Goal").apply {
                                        color = ColorTemplate.COLORFUL_COLORS[2]
                                    }

                                    val barData = BarData(minGoalDataSet, expensesDataSet, maxGoalDataSet)

                                    val groupSpace = 0.3f
                                    val barSpace = 0.05f
                                    val barWidth = (1f - groupSpace) / 3f - barSpace

                                    barData.barWidth = barWidth

                                    barChart.apply {
                                        data = barData
                                        description.isEnabled = false
                                        legend.isEnabled = true

                                        xAxis.apply {
                                            valueFormatter = IndexAxisValueFormatter(labels)
                                            granularity = 1f
                                            position = com.github.mikephil.charting.components.XAxis.XAxisPosition.BOTTOM
                                            setCenterAxisLabels(true)
                                            axisMinimum = 0f
                                            axisMaximum = budgetNames.size.toFloat()
                                            isGranularityEnabled = true
                                        }

                                        axisLeft.axisMinimum = 0f
                                        axisRight.isEnabled = false

                                        // IMPORTANT: group the bars so they show side by side
                                        barData.groupBars(0f, groupSpace, barSpace)

                                        animateY(1000)
                                        invalidate()
                                    }

                                }
                                .addOnFailureListener { e ->
                                    e.printStackTrace()
                                    Toast.makeText(this, "Failed to load expenses.", Toast.LENGTH_SHORT).show()
                                }

                        }
                        .addOnFailureListener { e ->
                            e.printStackTrace()
                            Toast.makeText(this, "Failed to load budgets.", Toast.LENGTH_SHORT).show()
                        }

                }
                .addOnFailureListener { e ->
                    e.printStackTrace()
                    Toast.makeText(this, "Failed to load goals.", Toast.LENGTH_SHORT).show()
                }
        }
    }

}


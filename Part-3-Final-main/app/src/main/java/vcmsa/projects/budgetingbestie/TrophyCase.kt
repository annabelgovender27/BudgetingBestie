package vcmsa.projects.budgetingbestie

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class TrophyCase : AppCompatActivity() {
    private lateinit var adapter: TrophyAdapter
    private val db = Firebase.firestore
    private val userId = FirebaseAuth.getInstance().currentUser?.uid.orEmpty()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_trophy_case)

        adapter = TrophyAdapter()
        val recyclerView = findViewById<RecyclerView>(R.id.trophyRecyclerView)
        recyclerView.layoutManager = GridLayoutManager(this, 2)
        recyclerView.adapter = adapter

        fetchTrophiesAndUserData()
    }

    private fun fetchTrophiesAndUserData() {
        if (userId.isBlank()) return

        fetchUserData { userData ->
            // Step 1: Get current earned trophies
            db.collection("users").document(userId).collection("trophies")
                .whereEqualTo("earned", true)
                .get()
                .addOnSuccessListener { trophySnapshot ->
                    val currentTrophies = trophySnapshot.map { it.toObject(Trophy::class.java) }

                    // Step 2: Check and award new trophies based on fresh data
                    TrophyManager.checkAndAwardTrophies(
                        userId, userData, currentTrophies, this
                    ) {
                        // Optional: Toast or log
                    }

                    // Step 3: Always reload trophies after checking
                    fetchAndDisplayTrophies()
                }
        }
    }

    private fun fetchAndDisplayTrophies() {
        db.collection("users").document(userId).collection("trophies")
            .whereEqualTo("earned", true)
            .get()
            .addOnSuccessListener { trophySnapshot ->
                val trophies = trophySnapshot.map { it.toObject(Trophy::class.java) }
                adapter.submitList(trophies)
            }
    }

    private fun fetchUserData(onComplete: (TrophyManager.UserData) -> Unit) {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val todayStr = sdf.format(Date())

        db.collection("users").document(userId).collection("budgets").get()
            .addOnSuccessListener { budgetsSnap ->
                val budgets = budgetsSnap.mapNotNull { doc ->
                    val start = doc.getString("startDate") ?: return@mapNotNull null
                    val end = doc.getString("endDate") ?: return@mapNotNull null
                    val amount = doc.getDouble("amount") ?: return@mapNotNull null
                    TrophyManager.Budget(doc.id, start, end, amount)
                }

                db.collection("users").document(userId).collection("expenses").get()
                    .addOnSuccessListener { expensesSnap ->
                        val expenses = expensesSnap.mapNotNull { doc ->
                            val date = doc.getString("date") ?: return@mapNotNull null
                            val amount = doc.getDouble("amount") ?: return@mapNotNull null
                            val category = doc.getString("category") ?: ""
                            TrophyManager.Expense(doc.id, date, amount, category)
                        }

                        val totalTakeout = expenses.filter {
                            it.category.lowercase() == "takeout"
                        }.sumOf { it.amount }

                        val categoriesUsed = expenses.map { it.category }.toSet()

                        // --- FIXED CATEGORY FETCH HERE ---
                        db.collection("categories")
                            .whereEqualTo("userId", userId)
                            .get()
                            .addOnSuccessListener { catSnap ->
                                val categoriesCreated = catSnap.size()

                                db.collection("users").document(userId).get()
                                    .addOnSuccessListener { userSnap ->
                                        val firstLoginDate = userSnap.getString("firstLoginDate")
                                        val firstLogin = firstLoginDate == todayStr

                                        val userData = TrophyManager.UserData(
                                            budgets = budgets,
                                            expenses = expenses,
                                            budgetsCreated = budgets.size,
                                            expensesLogged = expenses.size,
                                            totalTakeoutSpent = totalTakeout,
                                            categoriesCreated = categoriesCreated,
                                            categoriesUsed = categoriesUsed,
                                            firstLogin = firstLogin,
                                            firstLoginDate = firstLoginDate
                                        )
                                        onComplete(userData)
                                    }
                            }
                    }
            }
    }
}
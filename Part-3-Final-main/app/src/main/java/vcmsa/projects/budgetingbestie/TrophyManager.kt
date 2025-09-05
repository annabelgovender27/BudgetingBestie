package vcmsa.projects.budgetingbestie

import android.content.Context
import android.graphics.Color
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import nl.dionsegijn.konfetti.core.Party
import nl.dionsegijn.konfetti.core.Position
import nl.dionsegijn.konfetti.core.emitter.Emitter
import nl.dionsegijn.konfetti.xml.KonfettiView
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

object TrophyManager {

    data class TrophyDefinition(
        val id: String,
        val title: String,
        val description: String,
        val imageRes: Int,
        val requirementMet: (userData: UserData) -> Boolean
    )
    data class Budget(
        val id: String = "",
        val startDate: String = "",
        val endDate: String = "",
        val amount: Double = 0.0
    )
    data class Expense(
        val id: String = "",
        val date: String = "",
        val amount: Double = 0.0,
        val category: String = ""
    )

    data class UserData(
        val budgets: List<Budget> = emptyList(),
        val expenses: List<Expense> = emptyList(),
        val budgetsCreated: Int = 0,
        val totalTakeoutSpent: Double = 0.0,
        val expensesLogged: Int = 0,
        val categoriesCreated: Int = 0,
        val categoriesUsed: Set<String> = emptySet(),
        val firstLogin: Boolean = false,
        val firstLoginDate: String? = null
        // add more fields if needed
    )

    private val trophyDefinitions = listOf(


    // 1. First Budget
    TrophyDefinition(
    id = "first_budget",
    title = "First Budget",
    description = "Set your very first budget!",
    imageRes = R.drawable.trophy,
    requirementMet = { data -> data.budgetsCreated >= 1 }
    ),
    // 2. First Category
    TrophyDefinition(
    id = "first_category",
    title = "First Category",
    description = "Created your first category.",
    imageRes = R.drawable.trophy,
    requirementMet = { data -> data.categoriesCreated >= 1 }
    ),
    // 3. Small Savings
    TrophyDefinition(
    id = "small_savings",
    title = "Small Savings",
    description = "Stayed under budget for a 7-day period.",
    imageRes = R.drawable.trophy,
    requirementMet = { data ->
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        data.budgets.any { budget ->
            try {
                val start = sdf.parse(budget.startDate)
                val end = sdf.parse(budget.endDate)
                if (start != null && end != null) {
                    val diff = ((end.time - start.time) / (1000 * 60 * 60 * 24)).toInt() + 1
                    if (diff <= 7) {
                        val expensesInPeriod = data.expenses.filter { expense ->
                            val expDate = sdf.parse(expense.date)
                            expDate != null && !expDate.before(start) && !expDate.after(end)
                        }
                        val totalSpent = expensesInPeriod.sumOf { it.amount }
                        return@any totalSpent <= budget.amount
                    }
                }
                false
            } catch (e: Exception) {
                false
            }
        }
    }
    ),
    // 4. 5 categories
    TrophyDefinition(
    id = "5_categories",
    title = "Organizer",
    description = "Added 5 different categories.",
    imageRes = R.drawable.trophy,
    requirementMet = { data -> data.categoriesCreated >= 5 }
    ),
    // 5. 10 categories
    TrophyDefinition(
    id = "10_categories",
    title = "Organizer",
    description = "Added 10 different categories.",
    imageRes = R.drawable.trophy,
    requirementMet = { data -> data.categoriesCreated >= 10 }
    ),
    // 6. First expense
    TrophyDefinition(
    id = "first_expense",
    title = "First Expense",
    description = "Logged your very first expense.",
    imageRes = R.drawable.trophy,
    requirementMet = { data -> data.expensesLogged >= 1 }
    ),
    // 7. Takeout trophy: spent less than R700 (and > 0)
    TrophyDefinition(
    id = "takeout_saver",
    title = "Takeout Saver",
    description = "Spent less than R700 on takeout!",
    imageRes = R.drawable.trophy,
    requirementMet = { data -> data.totalTakeoutSpent > 0 && data.totalTakeoutSpent < 700 }
    ),
    // 8. Zero Takeout This Month
    TrophyDefinition(
    id = "zero_takeout_month",
    title = "Zero Takeout Month",
    description = "No takeout purchases for the current month!",
    imageRes = R.drawable.trophy,
    requirementMet = { data ->
        val now = Calendar.getInstance()
        val thisMonth = now.get(Calendar.MONTH)
        val thisYear = now.get(Calendar.YEAR)
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val takeoutExpensesThisMonth = data.expenses.filter { expense ->
            expense.category.equals("takeout", ignoreCase = true) &&
                    try {
                        val expDate = sdf.parse(expense.date)
                        if (expDate != null) {
                            val cal = Calendar.getInstance()
                            cal.time = expDate
                            cal.get(Calendar.MONTH) == thisMonth && cal.get(Calendar.YEAR) == thisYear
                        } else false
                    } catch (e: Exception) {
                        false
                    }
        }
        takeoutExpensesThisMonth.isEmpty()
    }
    )
    )

    fun checkAndAwardTrophies(
        userId: String,
        userData: UserData,
        currentTrophies: List<Trophy>,
        context: Context,
        onTrophyAwarded: (Trophy) -> Unit
    ) {
        val db = Firebase.firestore

        for (def in trophyDefinitions) {
            if (currentTrophies.none { it.id == def.id } && def.requirementMet(userData)) {
                val trophy = Trophy(
                    id = def.id,
                    title = def.title,
                    description = def.description,
                    dateAchieved = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()),
                    imageRes = def.imageRes,
                    earned = true
                )
                db.collection("users")
                    .document(userId)
                    .collection("trophies")
                    .document(trophy.id)
                    .set(trophy)
                    .addOnSuccessListener {
                        onTrophyAwarded(trophy)
                        showTrophyPopup(context, trophy)
                    }
            }
        }
    }

    fun showTrophyPopup(context: Context, trophy: Trophy) {
        val builder = AlertDialog.Builder(context)
        builder.setTitle("🏆 Trophy Unlocked!")
        builder.setMessage("${trophy.title}\n\n${trophy.description}")
        builder.setPositiveButton("Awesome!") { dialog, _ -> dialog.dismiss() }

        val dialog = builder.create()
        dialog.show()

        val activity = context as? AppCompatActivity ?: return

        val konfettiView = activity.findViewById<KonfettiView>(R.id.konfettiView)
        konfettiView?.start(
            Party(
                speed = 30f,
                maxSpeed = 30f,
                damping = 0.9f,
                spread = 360,
                colors = listOf(Color.BLUE, Color.GREEN, Color.YELLOW),
                emitter = Emitter(duration = 3, TimeUnit.SECONDS).perSecond(100),
                position = Position.Relative(0.5, 0.3)
            )
        )
    }
}
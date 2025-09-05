package vcmsa.projects.budgetingbestie

import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Locale

class ExpenseRepository {

    private val firestore = FirebaseFirestore.getInstance()
    private val collection = firestore.collection("expenses")
    private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    // Insert expense, generating an ID if needed
    suspend fun insertExpense(expense: Expense) {
        val docId = if (expense.id.isEmpty()) {
            collection.document().id
        } else {
            expense.id
        }

        val expenseWithId = expense.copy(
            id = docId,
            createdAt = expense.createdAt
        )
        collection.document(docId).set(expenseWithId).await()
    }


    // Get latest three expenses ordered by createdAt DESC for a user
    suspend fun getLatestThreeExpenses(userId: String): List<Expense> {
        val snapshot = collection.whereEqualTo("userId", userId)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .limit(3)
            .get()
            .await()
        return snapshot.toObjects(Expense::class.java)
    }

    // Get expense by ID
    suspend fun getExpenseById(id: String): Expense? {
        val doc = collection.document(id).get().await()
        return if (doc.exists()) doc.toObject(Expense::class.java) else null
    }

    // Update expense (replace document)
    suspend fun updateExpense(expense: Expense) {
        if (expense.id.isNotEmpty()) {
            collection.document(expense.id).set(expense).await()
        } else {
            throw IllegalArgumentException("Expense ID is empty, cannot update")
        }
    }

    // Delete expense
    suspend fun deleteExpense(expense: Expense) {
        if (expense.id.isNotEmpty()) {
            collection.document(expense.id).delete().await()
        } else {
            throw IllegalArgumentException("Expense ID is empty, cannot delete")
        }
    }


    suspend fun getCategoryTotalsBetween(userId: String, start: Timestamp, end: Timestamp): List<CategoryTotal> {
        val expenses = collection.whereEqualTo("userId", userId).get().await().toObjects(Expense::class.java)

        val startDate = start.toDate()
        val endDate = end.toDate()

        val filtered = expenses.filter {
            val expenseDate = try {
                dateFormat.parse(it.date)
            } catch (e: Exception) {
                null
            }
            expenseDate != null && !expenseDate.before(startDate) && !expenseDate.after(endDate)
        }

        val categoryTotalsMap = mutableMapOf<String, Double>()
        for (expense in filtered) {
            val amount = expense.amount.toDoubleOrNull() ?: 0.0
            categoryTotalsMap[expense.category] = (categoryTotalsMap[expense.category] ?: 0.0) + amount
        }

        return categoryTotalsMap.map { (category, total) -> CategoryTotal(category, total) }
    }


}


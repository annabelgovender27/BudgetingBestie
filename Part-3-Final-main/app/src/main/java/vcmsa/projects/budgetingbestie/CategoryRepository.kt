package vcmsa.projects.budgetingbestie

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await

class CategoryRepository {

    private val collection = FirebaseFirestore.getInstance().collection("categories")

    suspend fun addCategory(category: Category) {
        if (category.id.isEmpty()) {
            val newDoc = collection.document()
            category.id = newDoc.id
            newDoc.set(category).await()
        } else {
            collection.document(category.id).set(category).await()
        }
    }

    suspend fun getCategoriesForUser(userId: String): List<Category> {
        val snapshot = collection.whereEqualTo("userId", userId)
            .orderBy("name", Query.Direction.ASCENDING)
            .get()
            .await()
        return snapshot.toObjects(Category::class.java)
    }

    suspend fun getCategoryByName(userId: String, name: String): Category? {
        val snapshot = collection
            .whereEqualTo("userId", userId)
            .whereEqualTo("name", name)
            .limit(1)
            .get()
            .await()
        return if (snapshot.isEmpty) null else snapshot.toObjects(Category::class.java)[0]
    }

    suspend fun getLatestThreeCategories(userId: String): List<Category> {
        val snapshot = collection.whereEqualTo("userId", userId)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .limit(3)
            .get()
            .await()
        return snapshot.toObjects(Category::class.java)
    }
}

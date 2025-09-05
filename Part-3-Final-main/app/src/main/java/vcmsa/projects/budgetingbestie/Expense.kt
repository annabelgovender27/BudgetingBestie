package vcmsa.projects.budgetingbestie
import com.google.firebase.Timestamp


data class Expense(
    val id: String = "",
    var category: String = "",
    var description: String = "",
    var date: String = "",
    var amount: String = "",
    var receiptPhotoUri: String = "",
    var userId: String = "",
    var createdAt: Timestamp = Timestamp.now()
)

package vcmsa.projects.budgetingbestie

import com.google.firebase.Timestamp

data class Category(
    var id: String = "",
    var name: String = "",
    var description: String = "",
    var notes: String = "",
    var userId: String = "",
    var createdAt: Timestamp = Timestamp.now()
)

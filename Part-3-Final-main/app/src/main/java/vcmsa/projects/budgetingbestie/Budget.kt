package vcmsa.projects.budgetingbestie

data class
Budget(
    var id: String? = null, // Firestore document ID
    var name: String = "",
    var amount: Double = 0.0,
    var userId: String = ""
)

package vcmsa.projects.budgetingbestie

data class Trophy(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val dateAchieved: String = "",
    val imageRes: Int = R.drawable.trophy,
    val earned: Boolean = true
)

package app.unpolished.inksober.domain.model

data class Milestone(
    val title: String,
    val targetDays: Int,
    val isAchieved: Boolean,
    val achievedDateMillis: Long? = null,
    val progressPercentage: Float = 0f,
    val description: String = ""
)

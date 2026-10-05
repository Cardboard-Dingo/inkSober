package app.unpolished.inksober.domain.model

data class SobrietyDuration(
    val days: Long = 0,
    val hours: Long = 0,
    val minutes: Long = 0,
    val seconds: Long = 0,
    val totalHours: Long = 0,
    val totalDays: Long = 0
) {
    fun formatFormattedString(): String {
        return when {
            days > 0 -> "$days d, $hours h, $minutes m"
            hours > 0 -> "$hours h, $minutes m, $seconds s"
            else -> "$minutes m, $seconds s"
        }
    }
}

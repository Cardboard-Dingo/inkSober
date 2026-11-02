package app.unpolished.inksober.domain.model

data class SobrietyDuration(
    val days: Long = 0,
    val months: Long = 0,
    val years: Long = 0,
    val totalDays: Long = 0
) {
    fun formatFormattedString(): String {
        val parts = mutableListOf<String>()
        if (years > 0) parts.add("$years yr${if (years > 1) "s" else ""}")
        if (months > 0) parts.add("$months mo${if (months > 1) "s" else ""}")
        if (days > 0 || parts.isEmpty()) parts.add("$days day${if (days != 1L) "s" else ""}")
        return parts.joinToString(", ")
    }
}

package app.unpolished.inksober.domain.model

data class UserPreferences(
    val activeSubstanceId: Long = 1L,
    val isEInkModeEnabled: Boolean = false,
    val isDarkModeEnabled: Boolean = false
)

package app.unpolished.inksober.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
data object DashboardRoute : NavKey

@Serializable
data object SubstancesRoute : NavKey

@Serializable
data class JournalRoute(val preselectedSubstanceId: Long? = null) : NavKey

@Serializable
data object QuotesRoute : NavKey

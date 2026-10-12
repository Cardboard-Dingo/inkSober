package app.unpolished.inksober.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.unpolished.inksober.data.local.entity.RecoveryQuoteEntity
import app.unpolished.inksober.data.local.entity.SubstanceEntity
import app.unpolished.inksober.data.preferences.UserPreferencesRepository
import app.unpolished.inksober.domain.model.Milestone
import app.unpolished.inksober.domain.model.SobrietyDuration
import app.unpolished.inksober.repository.JournalRepository
import app.unpolished.inksober.repository.QuoteRepository
import app.unpolished.inksober.repository.SubstanceRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class DashboardUiState(
    val activeSubstance: SubstanceEntity? = null,
    val sobrietyDuration: SobrietyDuration = SobrietyDuration(),
    val milestones: List<Milestone> = emptyList(),
    val quoteOfTheDay: RecoveryQuoteEntity? = null,
    val allSubstances: List<SubstanceEntity> = emptyList(),
    val isLoading: Boolean = true
)

class DashboardViewModel(
    private val substanceRepository: SubstanceRepository,
    private val quoteRepository: QuoteRepository,
    private val journalRepository: JournalRepository,
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    private val _ticker = MutableStateFlow(System.currentTimeMillis())

    init {
        viewModelScope.launch {
            while (true) {
                delay(1000L)
                _ticker.value = System.currentTimeMillis()
            }
        }

        observeData()
    }

    private fun observeData() {
        viewModelScope.launch {
            combine(
                userPreferencesRepository.userPreferencesFlow,
                substanceRepository.allSubstances,
                quoteRepository.getQuoteOfTheDay(),
                _ticker
            ) { prefs, substances, quote, now ->
                val active = substances.find { it.id == prefs.activeSubstanceId }
                    ?: substances.firstOrNull()

                val duration = if (active != null) {
                    substanceRepository.calculateSobrietyDuration(active.startDateMillis, now)
                } else {
                    SobrietyDuration()
                }

                val milestones = if (active != null) {
                    substanceRepository.calculateMilestones(active.startDateMillis, now)
                } else {
                    emptyList()
                }

                DashboardUiState(
                    activeSubstance = active,
                    sobrietyDuration = duration,
                    milestones = milestones,
                    quoteOfTheDay = quote ?: _uiState.value.quoteOfTheDay,
                    allSubstances = substances,
                    isLoading = false
                )
            }.collect { newState ->
                _uiState.value = newState
            }
        }
    }

    fun cycleQuote() {
        viewModelScope.launch {
            quoteRepository.getRandomQuote().take(1).collect { randomQuote ->
                if (randomQuote != null) {
                    _uiState.update { it.copy(quoteOfTheDay = randomQuote) }
                }
            }
        }
    }

    fun resetSobrietyDate(substanceId: Long, customStartDateMillis: Long = System.currentTimeMillis()) {
        viewModelScope.launch {
            val substance = substanceRepository.getSubstanceByIdOneShot(substanceId)
            if (substance != null) {
                val updated = substance.copy(startDateMillis = customStartDateMillis)
                substanceRepository.updateSubstance(updated)
            }
        }
    }

    fun setActiveSubstance(substanceId: Long) {
        viewModelScope.launch {
            userPreferencesRepository.setActiveSubstanceId(substanceId)
        }
    }

    fun addSubstance(
        name: String,
        category: String,
        icon: String,
        startDateMillis: Long,
        initialNote: String
    ) {
        viewModelScope.launch {
            val newSubstance = SubstanceEntity(
                name = name,
                startDateMillis = startDateMillis,
                category = category,
                icon = icon,
                initialNote = initialNote
            )
            val id = substanceRepository.insertSubstance(newSubstance)
            userPreferencesRepository.setActiveSubstanceId(id)
        }
    }
}

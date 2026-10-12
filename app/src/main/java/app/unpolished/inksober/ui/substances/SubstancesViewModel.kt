package app.unpolished.inksober.ui.substances

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.unpolished.inksober.data.local.entity.SubstanceEntity
import app.unpolished.inksober.data.preferences.UserPreferencesRepository
import app.unpolished.inksober.domain.model.SobrietyDuration
import app.unpolished.inksober.repository.SubstanceRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class SubstanceItem(
    val entity: SubstanceEntity,
    val duration: SobrietyDuration,
    val isPrimary: Boolean
)

data class SubstancesUiState(
    val substances: List<SubstanceItem> = emptyList(),
    val primarySubstanceId: Long = 1L,
    val isLoading: Boolean = true
)

class SubstancesViewModel(
    private val substanceRepository: SubstanceRepository,
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SubstancesUiState())
    val uiState: StateFlow<SubstancesUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                substanceRepository.allSubstances,
                userPreferencesRepository.userPreferencesFlow
            ) { substances, prefs ->
                val now = System.currentTimeMillis()
                val items = substances.map { substance ->
                    SubstanceItem(
                        entity = substance,
                        duration = substanceRepository.calculateSobrietyDuration(substance.startDateMillis, now),
                        isPrimary = substance.id == prefs.activeSubstanceId
                    )
                }
                SubstancesUiState(
                    substances = items,
                    primarySubstanceId = prefs.activeSubstanceId,
                    isLoading = false
                )
            }.collect { newState ->
                _uiState.value = newState
            }
        }
    }

    fun addSubstance(
        name: String,
        startDateMillis: Long,
        category: String,
        icon: String = "tune",
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
            if (_uiState.value.substances.size == 1) {
                userPreferencesRepository.setActiveSubstanceId(id)
            }
        }
    }

    fun updateSubstance(substance: SubstanceEntity) {
        viewModelScope.launch {
            substanceRepository.updateSubstance(substance)
        }
    }

    fun deleteSubstance(substance: SubstanceEntity) {
        viewModelScope.launch {
            substanceRepository.deleteSubstance(substance)
            if (substance.id == _uiState.value.primarySubstanceId) {
                val remaining = _uiState.value.substances.filter { it.entity.id != substance.id }
                if (remaining.isNotEmpty()) {
                    userPreferencesRepository.setActiveSubstanceId(remaining.first().entity.id)
                }
            }
        }
    }

    fun setPrimarySubstance(id: Long) {
        viewModelScope.launch {
            userPreferencesRepository.setActiveSubstanceId(id)
        }
    }
}

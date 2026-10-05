package app.unpolished.inksober.ui.journal

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.unpolished.inksober.data.local.entity.JournalEntryEntity
import app.unpolished.inksober.data.local.entity.SubstanceEntity
import app.unpolished.inksober.repository.JournalRepository
import app.unpolished.inksober.repository.SubstanceRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate

data class JournalUiState(
    val entries: List<JournalEntryEntity> = emptyList(),
    val filteredEntries: List<JournalEntryEntity> = emptyList(),
    val substances: List<SubstanceEntity> = emptyList(),
    val searchQuery: String = "",
    val selectedMoodFilter: String? = null,
    val isLoading: Boolean = true
)

class JournalViewModel(
    private val journalRepository: JournalRepository,
    private val substanceRepository: SubstanceRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    private val _selectedMoodFilter = MutableStateFlow<String?>(null)
    private val _uiState = MutableStateFlow(JournalUiState())
    val uiState: StateFlow<JournalUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                journalRepository.allJournalEntries,
                substanceRepository.allSubstances,
                _searchQuery,
                _selectedMoodFilter
            ) { entries, substances, query, moodFilter ->
                val filtered = entries.filter { entry ->
                    val matchesQuery = query.isBlank() ||
                            entry.title.contains(query, ignoreCase = true) ||
                            entry.body.contains(query, ignoreCase = true)
                    val matchesMood = moodFilter == null || entry.mood.equals(moodFilter, ignoreCase = true)
                    matchesQuery && matchesMood
                }

                JournalUiState(
                    entries = entries,
                    filteredEntries = filtered,
                    substances = substances,
                    searchQuery = query,
                    selectedMoodFilter = moodFilter,
                    isLoading = false
                )
            }.collect { newState ->
                _uiState.value = newState
            }
        }
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setMoodFilter(mood: String?) {
        _selectedMoodFilter.value = mood
    }

    fun addJournalEntry(title: String, body: String, mood: String, substanceId: Long) {
        viewModelScope.launch {
            val epochDay = LocalDate.now().toEpochDay()
            val entry = JournalEntryEntity(
                dateEpochDay = epochDay,
                substanceId = substanceId,
                title = title,
                body = body,
                mood = mood,
                timestampMillis = System.currentTimeMillis()
            )
            journalRepository.insertJournalEntry(entry)
        }
    }

    fun updateJournalEntry(entry: JournalEntryEntity) {
        viewModelScope.launch {
            journalRepository.updateJournalEntry(entry)
        }
    }

    fun deleteJournalEntry(entry: JournalEntryEntity) {
        viewModelScope.launch {
            journalRepository.deleteJournalEntry(entry)
        }
    }
}

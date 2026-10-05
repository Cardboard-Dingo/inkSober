package app.unpolished.inksober.ui.quotes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.unpolished.inksober.data.local.entity.RecoveryQuoteEntity
import app.unpolished.inksober.repository.QuoteRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class QuotesUiState(
    val quotes: List<RecoveryQuoteEntity> = emptyList(),
    val searchQuery: String = "",
    val isLoading: Boolean = true
)

class QuotesViewModel(
    private val quoteRepository: QuoteRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    private val _uiState = MutableStateFlow(QuotesUiState())
    val uiState: StateFlow<QuotesUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            quoteRepository.seedQuotesIfEmpty()
            combine(
                quoteRepository.allQuotes,
                _searchQuery
            ) { quotes, query ->
                val filtered = if (query.isBlank()) {
                    quotes
                } else {
                    quotes.filter {
                        it.quoteText.contains(query, ignoreCase = true) ||
                                it.author.contains(query, ignoreCase = true)
                    }
                }
                QuotesUiState(
                    quotes = filtered,
                    searchQuery = query,
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

    fun addCustomQuote(text: String, author: String) {
        viewModelScope.launch {
            quoteRepository.insertQuote(
                RecoveryQuoteEntity(
                    quoteText = text,
                    author = author
                )
            )
        }
    }
}

package app.unpolished.inksober.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import app.unpolished.inksober.InkSoberApplication
import app.unpolished.inksober.ui.dashboard.DashboardViewModel
import app.unpolished.inksober.ui.journal.JournalViewModel
import app.unpolished.inksober.ui.quotes.QuotesViewModel
import app.unpolished.inksober.ui.substances.SubstancesViewModel

object AppViewModelFactory {

    fun createDashboardViewModelFactory(app: InkSoberApplication): ViewModelProvider.Factory =
        object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
                return DashboardViewModel(
                    substanceRepository = app.substanceRepository,
                    quoteRepository = app.quoteRepository,
                    journalRepository = app.journalRepository,
                    userPreferencesRepository = app.userPreferencesRepository
                ) as T
            }
        }

    fun createSubstancesViewModelFactory(app: InkSoberApplication): ViewModelProvider.Factory =
        object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
                return SubstancesViewModel(
                    substanceRepository = app.substanceRepository,
                    userPreferencesRepository = app.userPreferencesRepository
                ) as T
            }
        }

    fun createJournalViewModelFactory(app: InkSoberApplication): ViewModelProvider.Factory =
        object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
                return JournalViewModel(
                    journalRepository = app.journalRepository,
                    substanceRepository = app.substanceRepository
                ) as T
            }
        }

    fun createQuotesViewModelFactory(app: InkSoberApplication): ViewModelProvider.Factory =
        object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
                return QuotesViewModel(
                    quoteRepository = app.quoteRepository
                ) as T
            }
        }
}

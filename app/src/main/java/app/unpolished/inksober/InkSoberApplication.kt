package app.unpolished.inksober

import android.app.Application
import app.unpolished.inksober.data.local.AppDatabase
import app.unpolished.inksober.data.local.entity.SubstanceEntity
import app.unpolished.inksober.data.preferences.UserPreferencesRepository
import app.unpolished.inksober.repository.JournalRepository
import app.unpolished.inksober.repository.QuoteRepository
import app.unpolished.inksober.repository.SubstanceRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

class InkSoberApplication : Application() {

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    val database: AppDatabase by lazy { AppDatabase.getInstance(this, applicationScope) }
    val substanceRepository: SubstanceRepository by lazy { SubstanceRepository(database.substanceDao()) }
    val journalRepository: JournalRepository by lazy { JournalRepository(database.journalEntryDao()) }
    val quoteRepository: QuoteRepository by lazy { QuoteRepository(database.recoveryQuoteDao()) }
    val userPreferencesRepository: UserPreferencesRepository by lazy { UserPreferencesRepository(this) }

    override fun onCreate() {
        super.onCreate()
        applicationScope.launch {
            quoteRepository.seedQuotesIfEmpty()
            seedDefaultSubstanceIfEmpty()
        }
    }

    private suspend fun seedDefaultSubstanceIfEmpty() {
        if (database.substanceDao().getSubstanceCount() == 0) {
            val defaultStart = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(1)
            val defaultSubstance = SubstanceEntity(
                name = "Alcohol",
                startDateMillis = defaultStart,
                category = "General",
                icon = "local_bar",
                initialNote = "Started my sobriety journey."
            )
            val id = substanceRepository.insertSubstance(defaultSubstance)
            userPreferencesRepository.setActiveSubstanceId(id)
        }
    }
}

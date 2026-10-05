package app.unpolished.inksober.repository

import app.unpolished.inksober.data.local.InitialQuoteSeed
import app.unpolished.inksober.data.local.dao.RecoveryQuoteDao
import app.unpolished.inksober.data.local.entity.RecoveryQuoteEntity
import kotlinx.coroutines.flow.Flow
import java.util.Calendar

class QuoteRepository(private val recoveryQuoteDao: RecoveryQuoteDao) {

    val allQuotes: Flow<List<RecoveryQuoteEntity>> = recoveryQuoteDao.getAllQuotes()

    fun getQuoteOfTheDay(dayOfYear: Int = Calendar.getInstance().get(Calendar.DAY_OF_YEAR)): Flow<RecoveryQuoteEntity?> {
        val mappedDay = ((dayOfYear - 1) % InitialQuoteSeed.quotes.size) + 1
        return recoveryQuoteDao.getQuoteByDayOfYear(mappedDay)
    }

    fun getRandomQuote(): Flow<RecoveryQuoteEntity?> = recoveryQuoteDao.getRandomQuote()

    suspend fun seedQuotesIfEmpty() {
        if (recoveryQuoteDao.getQuoteCount() == 0) {
            recoveryQuoteDao.insertQuotes(InitialQuoteSeed.quotes)
        }
    }

    suspend fun insertQuote(quote: RecoveryQuoteEntity): Long = recoveryQuoteDao.insertQuote(quote)
}

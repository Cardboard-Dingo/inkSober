package app.unpolished.inksober

import app.unpolished.inksober.data.local.InitialQuoteSeed
import app.unpolished.inksober.data.local.dao.RecoveryQuoteDao
import app.unpolished.inksober.data.local.entity.RecoveryQuoteEntity
import app.unpolished.inksober.repository.QuoteRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test

class QuoteRepositoryTest {

    private lateinit var fakeDao: FakeQuoteDao
    private lateinit var repository: QuoteRepository

    @Before
    fun setup() {
        fakeDao = FakeQuoteDao()
        repository = QuoteRepository(fakeDao)
    }

    @Test
    fun seedQuotesIfEmpty_populatesAllQuotes() = runTest {
        assertEquals(0, fakeDao.getQuoteCount())

        repository.seedQuotesIfEmpty()

        assertEquals(InitialQuoteSeed.quotes.size, fakeDao.getQuoteCount())
    }

    @Test
    fun getQuoteOfTheDay_mapsDayOfYearToQuotes() = runTest {
        repository.seedQuotesIfEmpty()

        val quoteDay1 = repository.getQuoteOfTheDay(1).first()
        val quoteDay26 = repository.getQuoteOfTheDay(26).first() // 26 % 25 = 1

        assertNotNull(quoteDay1)
        assertEquals(1, quoteDay1?.dayOfYear)
        assertEquals(1, quoteDay26?.dayOfYear)
    }

    private class FakeQuoteDao : RecoveryQuoteDao {
        private val list = mutableListOf<RecoveryQuoteEntity>()
        private val flow = MutableStateFlow<List<RecoveryQuoteEntity>>(emptyList())

        private fun notifyChanges() {
            flow.value = list.toList()
        }

        override fun getAllQuotes(): Flow<List<RecoveryQuoteEntity>> = flow

        override fun getQuoteByDayOfYear(dayOfYear: Int): Flow<RecoveryQuoteEntity?> =
            flow.map { items -> items.find { it.dayOfYear == dayOfYear } }

        override suspend fun getQuoteByDayOfYearOneShot(dayOfYear: Int): RecoveryQuoteEntity? =
            list.find { it.dayOfYear == dayOfYear }

        override fun getRandomQuote(): Flow<RecoveryQuoteEntity?> =
            flow.map { items -> items.shuffled().firstOrNull() }

        override suspend fun getQuoteCount(): Int = list.size

        override suspend fun insertQuotes(quotes: List<RecoveryQuoteEntity>) {
            list.addAll(quotes)
            notifyChanges()
        }

        override suspend fun insertQuote(quote: RecoveryQuoteEntity): Long {
            list.add(quote)
            notifyChanges()
            return quote.id
        }
    }
}

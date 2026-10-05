package app.unpolished.inksober

import app.unpolished.inksober.data.local.dao.JournalEntryDao
import app.unpolished.inksober.data.local.entity.JournalEntryEntity
import app.unpolished.inksober.repository.JournalRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test

class JournalRepositoryTest {

    private lateinit var fakeDao: FakeJournalEntryDao
    private lateinit var repository: JournalRepository

    @Before
    fun setup() {
        fakeDao = FakeJournalEntryDao()
        repository = JournalRepository(fakeDao)
    }

    @Test
    fun insertAndGetJournalEntry_worksCorrectly() = runTest {
        val entry = JournalEntryEntity(
            id = 1,
            dateEpochDay = 19500,
            substanceId = 1,
            title = "First sober week",
            body = "Feeling great!",
            mood = "Peaceful"
        )

        repository.insertJournalEntry(entry)

        val retrieved = repository.getJournalEntryByIdOneShot(1)
        assertNotNull(retrieved)
        assertEquals("First sober week", retrieved?.title)
        assertEquals("Peaceful", retrieved?.mood)
    }

    @Test
    fun deleteJournalEntry_removesEntry() = runTest {
        val entry = JournalEntryEntity(
            id = 2,
            dateEpochDay = 19501,
            substanceId = 1,
            title = "Reflections",
            body = "Taking it step by step.",
            mood = "Strong"
        )

        repository.insertJournalEntry(entry)
        val allEntriesBefore = repository.allJournalEntries.first()
        assertEquals(1, allEntriesBefore.size)

        repository.deleteJournalEntry(entry)
        val allEntriesAfter = repository.allJournalEntries.first()
        assertEquals(0, allEntriesAfter.size)
    }

    private class FakeJournalEntryDao : JournalEntryDao {
        private val list = mutableListOf<JournalEntryEntity>()
        private val flow = MutableStateFlow<List<JournalEntryEntity>>(emptyList())

        private fun notifyChanges() {
            flow.value = list.toList()
        }

        override fun getAllJournalEntries(): Flow<List<JournalEntryEntity>> = flow

        override fun getJournalEntriesBySubstance(substanceId: Long): Flow<List<JournalEntryEntity>> =
            flow.map { entries -> entries.filter { it.substanceId == substanceId } }

        override fun getJournalEntriesByDate(dateEpochDay: Long): Flow<List<JournalEntryEntity>> =
            flow.map { entries -> entries.filter { it.dateEpochDay == dateEpochDay } }

        override fun getJournalEntryById(id: Long): Flow<JournalEntryEntity?> =
            flow.map { entries -> entries.find { it.id == id } }

        override suspend fun getJournalEntryByIdOneShot(id: Long): JournalEntryEntity? =
            list.find { it.id == id }

        override suspend fun insertJournalEntry(entry: JournalEntryEntity): Long {
            list.removeIf { it.id == entry.id }
            list.add(entry)
            notifyChanges()
            return entry.id
        }

        override suspend fun updateJournalEntry(entry: JournalEntryEntity) {
            insertJournalEntry(entry)
        }

        override suspend fun deleteJournalEntry(entry: JournalEntryEntity) {
            list.removeIf { it.id == entry.id }
            notifyChanges()
        }

        override suspend fun deleteJournalEntryById(id: Long) {
            list.removeIf { it.id == id }
            notifyChanges()
        }
    }
}

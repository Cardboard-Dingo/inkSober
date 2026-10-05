package app.unpolished.inksober.repository

import app.unpolished.inksober.data.local.dao.JournalEntryDao
import app.unpolished.inksober.data.local.entity.JournalEntryEntity
import kotlinx.coroutines.flow.Flow

class JournalRepository(private val journalEntryDao: JournalEntryDao) {

    val allJournalEntries: Flow<List<JournalEntryEntity>> = journalEntryDao.getAllJournalEntries()

    fun getJournalEntriesBySubstance(substanceId: Long): Flow<List<JournalEntryEntity>> =
        journalEntryDao.getJournalEntriesBySubstance(substanceId)

    fun getJournalEntriesByDate(dateEpochDay: Long): Flow<List<JournalEntryEntity>> =
        journalEntryDao.getJournalEntriesByDate(dateEpochDay)

    fun getJournalEntryById(id: Long): Flow<JournalEntryEntity?> =
        journalEntryDao.getJournalEntryById(id)

    suspend fun getJournalEntryByIdOneShot(id: Long): JournalEntryEntity? =
        journalEntryDao.getJournalEntryByIdOneShot(id)

    suspend fun insertJournalEntry(entry: JournalEntryEntity): Long =
        journalEntryDao.insertJournalEntry(entry)

    suspend fun updateJournalEntry(entry: JournalEntryEntity) =
        journalEntryDao.updateJournalEntry(entry)

    suspend fun deleteJournalEntry(entry: JournalEntryEntity) =
        journalEntryDao.deleteJournalEntry(entry)

    suspend fun deleteJournalEntryById(id: Long) =
        journalEntryDao.deleteJournalEntryById(id)
}

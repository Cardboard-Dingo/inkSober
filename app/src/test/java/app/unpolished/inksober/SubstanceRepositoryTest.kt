package app.unpolished.inksober

import app.unpolished.inksober.data.local.dao.SubstanceDao
import app.unpolished.inksober.data.local.entity.SubstanceEntity
import app.unpolished.inksober.repository.SubstanceRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.util.concurrent.TimeUnit

class SubstanceRepositoryTest {

    private lateinit var fakeDao: FakeSubstanceDao
    private lateinit var repository: SubstanceRepository

    @Before
    fun setup() {
        fakeDao = FakeSubstanceDao()
        repository = SubstanceRepository(fakeDao)
    }

    @Test
    fun calculateSobrietyDuration_correctlyCalculatesTime() {
        val now = System.currentTimeMillis()
        // 5 days, 3 hours, 20 minutes ago
        val fiveDaysAgo = now - (TimeUnit.DAYS.toMillis(5) + TimeUnit.HOURS.toMillis(3) + TimeUnit.MINUTES.toMillis(20))

        val duration = repository.calculateSobrietyDuration(fiveDaysAgo, now)

        assertEquals(5L, duration.days)
        assertEquals(3L, duration.hours)
        assertEquals(20L, duration.minutes)
        assertEquals(5L, duration.totalDays)
        assertEquals(5 * 24 + 3L, duration.totalHours)
    }

    @Test
    fun calculateMilestones_correctlyIdentifiesAchieved() {
        val now = System.currentTimeMillis()
        // 10 days sober
        val tenDaysAgo = now - TimeUnit.DAYS.toMillis(10)

        val milestones = repository.calculateMilestones(tenDaysAgo, now)

        val oneDayMilestone = milestones.first { it.targetDays == 1 }
        val oneWeekMilestone = milestones.first { it.targetDays == 7 }
        val oneMonthMilestone = milestones.first { it.targetDays == 30 }

        assertTrue(oneDayMilestone.isAchieved)
        assertTrue(oneWeekMilestone.isAchieved)
        assertFalse(oneMonthMilestone.isAchieved)
        assertEquals(10f / 30f, oneMonthMilestone.progressPercentage, 0.01f)
    }

    @Test
    fun crudOperations_workWithDao() = runTest {
        val substance = SubstanceEntity(
            id = 1,
            name = "Alcohol",
            startDateMillis = System.currentTimeMillis(),
            category = "General",
            icon = "local_bar",
            initialNote = "Fresh start"
        )

        repository.insertSubstance(substance)
        val retrieved = repository.getSubstanceByIdOneShot(1)

        assertEquals("Alcohol", retrieved?.name)

        repository.deleteSubstanceById(1)
        val afterDelete = repository.getSubstanceByIdOneShot(1)

        assertEquals(null, afterDelete)
    }

    private class FakeSubstanceDao : SubstanceDao {
        private val list = mutableListOf<SubstanceEntity>()
        private val flow = MutableStateFlow<List<SubstanceEntity>>(emptyList())

        private fun notifyChanges() {
            flow.value = list.toList()
        }

        override fun getAllSubstances(): Flow<List<SubstanceEntity>> = flow

        override fun getSubstanceById(id: Long): Flow<SubstanceEntity?> =
            flow.map { items -> items.find { it.id == id } }

        override suspend fun getSubstanceByIdOneShot(id: Long): SubstanceEntity? =
            list.find { it.id == id }

        override suspend fun getSubstanceCount(): Int = list.size

        override suspend fun insertSubstance(substance: SubstanceEntity): Long {
            list.removeIf { it.id == substance.id }
            list.add(substance)
            notifyChanges()
            return substance.id
        }

        override suspend fun updateSubstance(substance: SubstanceEntity) {
            insertSubstance(substance)
        }

        override suspend fun deleteSubstance(substance: SubstanceEntity) {
            list.removeIf { it.id == substance.id }
            notifyChanges()
        }

        override suspend fun deleteSubstanceById(id: Long) {
            list.removeIf { it.id == id }
            notifyChanges()
        }
    }
}

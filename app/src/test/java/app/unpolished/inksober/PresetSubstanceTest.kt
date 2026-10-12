package app.unpolished.inksober

import app.unpolished.inksober.domain.model.PresetSubstance
import app.unpolished.inksober.repository.SubstanceRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import java.util.Calendar

class PresetSubstanceTest {

    @Test
    fun presetSubstances_containsAllFourteenChoices() {
        val presets = PresetSubstance.ALL_PRESETS
        assertEquals(14, presets.size)

        val expectedNames = listOf(
            "Alcohol",
            "Tobacco / Smoking",
            "Vaping / E-Cigarettes",
            "Cannabis / Marijuana",
            "Narcotics / Opioids",
            "Gambling",
            "Caffeine / Energy Drinks",
            "Sugar / Junk Food",
            "Social Media / Screen Time",
            "Pornography / Adult Content",
            "Shopping / Compulsive Buying",
            "Prescription Medications",
            "Video Games / Gaming",
            "Custom Substance / Habit"
        )

        val actualNames = presets.map { it.displayName }
        assertEquals(expectedNames, actualNames)
    }

    @Test
    fun presetSubstance_iconsResolveProperly() {
        PresetSubstance.ALL_PRESETS.forEach { preset ->
            val icon = PresetSubstance.getIconForSubstance(preset.iconName, preset.category)
            assertNotNull(icon)
        }
    }

    @Test
    fun exactTimestampSobrietyCalculation_calculatesPreciseDuration() {
        val calendar = Calendar.getInstance().apply {
            set(2024, Calendar.JANUARY, 1, 12, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val pastStartMillis = calendar.timeInMillis

        val nowCalendar = Calendar.getInstance().apply {
            set(2024, Calendar.JANUARY, 10, 15, 30, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val nowMillis = nowCalendar.timeInMillis

        val dummyDao = FakeSubstanceDao()
        val repository = SubstanceRepository(dummyDao)

        val duration = repository.calculateSobrietyDuration(pastStartMillis, nowMillis)

        assertEquals(9L, duration.days)
        assertEquals(3L, duration.hours)
        assertEquals(30L, duration.minutes)
    }

    private class FakeSubstanceDao : app.unpolished.inksober.data.local.dao.SubstanceDao {
        override fun getAllSubstances() = kotlinx.coroutines.flow.MutableStateFlow(emptyList<app.unpolished.inksober.data.local.entity.SubstanceEntity>())
        override fun getSubstanceById(id: Long) = kotlinx.coroutines.flow.MutableStateFlow(null)
        override suspend fun getSubstanceByIdOneShot(id: Long) = null
        override suspend fun getSubstanceCount() = 0
        override suspend fun insertSubstance(substance: app.unpolished.inksober.data.local.entity.SubstanceEntity) = substance.id
        override suspend fun updateSubstance(substance: app.unpolished.inksober.data.local.entity.SubstanceEntity) {}
        override suspend fun deleteSubstance(substance: app.unpolished.inksober.data.local.entity.SubstanceEntity) {}
        override suspend fun deleteSubstanceById(id: Long) {}
    }
}

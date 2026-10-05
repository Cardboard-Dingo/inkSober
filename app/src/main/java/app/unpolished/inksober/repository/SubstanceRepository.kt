package app.unpolished.inksober.repository

import app.unpolished.inksober.data.local.dao.SubstanceDao
import app.unpolished.inksober.data.local.entity.SubstanceEntity
import app.unpolished.inksober.domain.model.Milestone
import app.unpolished.inksober.domain.model.SobrietyDuration
import kotlinx.coroutines.flow.Flow
import java.util.concurrent.TimeUnit

class SubstanceRepository(private val substanceDao: SubstanceDao) {

    val allSubstances: Flow<List<SubstanceEntity>> = substanceDao.getAllSubstances()

    fun getSubstanceById(id: Long): Flow<SubstanceEntity?> = substanceDao.getSubstanceById(id)

    suspend fun getSubstanceByIdOneShot(id: Long): SubstanceEntity? = substanceDao.getSubstanceByIdOneShot(id)

    suspend fun insertSubstance(substance: SubstanceEntity): Long = substanceDao.insertSubstance(substance)

    suspend fun updateSubstance(substance: SubstanceEntity) = substanceDao.updateSubstance(substance)

    suspend fun deleteSubstance(substance: SubstanceEntity) = substanceDao.deleteSubstance(substance)

    suspend fun deleteSubstanceById(id: Long) = substanceDao.deleteSubstanceById(id)

    fun calculateSobrietyDuration(
        startDateMillis: Long,
        currentMillis: Long = System.currentTimeMillis()
    ): SobrietyDuration {
        if (startDateMillis > currentMillis) {
            return SobrietyDuration()
        }
        val diffMillis = currentMillis - startDateMillis

        val seconds = TimeUnit.MILLISECONDS.toSeconds(diffMillis) % 60
        val minutes = TimeUnit.MILLISECONDS.toMinutes(diffMillis) % 60
        val hours = TimeUnit.MILLISECONDS.toHours(diffMillis) % 24
        val days = TimeUnit.MILLISECONDS.toDays(diffMillis)
        val totalHours = TimeUnit.MILLISECONDS.toHours(diffMillis)
        val totalDays = days

        return SobrietyDuration(
            days = days,
            hours = hours,
            minutes = minutes,
            seconds = seconds,
            totalHours = totalHours,
            totalDays = totalDays
        )
    }

    fun calculateMilestones(
        startDateMillis: Long,
        currentMillis: Long = System.currentTimeMillis()
    ): List<Milestone> {
        val totalDays = if (currentMillis >= startDateMillis) {
            TimeUnit.MILLISECONDS.toDays(currentMillis - startDateMillis).toInt()
        } else 0

        val standardMilestones = listOf(
            Pair(1, "24 Hours"),
            Pair(3, "3 Days"),
            Pair(7, "1 Week"),
            Pair(14, "2 Weeks"),
            Pair(30, "1 Month"),
            Pair(60, "60 Days"),
            Pair(90, "90 Days"),
            Pair(180, "6 Months"),
            Pair(270, "9 Months"),
            Pair(365, "1 Year")
        )

        return standardMilestones.map { (targetDays, title) ->
            val isAchieved = totalDays >= targetDays
            val achievedDate = if (isAchieved) startDateMillis + TimeUnit.DAYS.toMillis(targetDays.toLong()) else null
            val progress = if (isAchieved) 1.0f else (totalDays.toFloat() / targetDays.toFloat()).coerceIn(0.0f, 1.0f)

            Milestone(
                title = title,
                targetDays = targetDays,
                isAchieved = isAchieved,
                achievedDateMillis = achievedDate,
                progressPercentage = progress,
                description = if (isAchieved) "Achieved $title sober" else "$totalDays / $targetDays days completed"
            )
        }
    }
}

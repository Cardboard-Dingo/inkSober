package app.unpolished.inksober.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import app.unpolished.inksober.data.local.entity.RecoveryQuoteEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RecoveryQuoteDao {
    @Query("SELECT * FROM recovery_quotes")
    fun getAllQuotes(): Flow<List<RecoveryQuoteEntity>>

    @Query("SELECT * FROM recovery_quotes WHERE dayOfYear = :dayOfYear LIMIT 1")
    fun getQuoteByDayOfYear(dayOfYear: Int): Flow<RecoveryQuoteEntity?>

    @Query("SELECT * FROM recovery_quotes WHERE dayOfYear = :dayOfYear LIMIT 1")
    suspend fun getQuoteByDayOfYearOneShot(dayOfYear: Int): RecoveryQuoteEntity?

    @Query("SELECT * FROM recovery_quotes ORDER BY RANDOM() LIMIT 1")
    fun getRandomQuote(): Flow<RecoveryQuoteEntity?>

    @Query("SELECT COUNT(*) FROM recovery_quotes")
    suspend fun getQuoteCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuotes(quotes: List<RecoveryQuoteEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuote(quote: RecoveryQuoteEntity): Long
}

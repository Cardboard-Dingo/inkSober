package app.unpolished.inksober.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import app.unpolished.inksober.data.local.entity.SubstanceEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SubstanceDao {
    @Query("SELECT * FROM substances ORDER BY startDateMillis DESC")
    fun getAllSubstances(): Flow<List<SubstanceEntity>>

    @Query("SELECT * FROM substances WHERE id = :id")
    fun getSubstanceById(id: Long): Flow<SubstanceEntity?>

    @Query("SELECT * FROM substances WHERE id = :id")
    suspend fun getSubstanceByIdOneShot(id: Long): SubstanceEntity?

    @Query("SELECT COUNT(*) FROM substances")
    suspend fun getSubstanceCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubstance(substance: SubstanceEntity): Long

    @Update
    suspend fun updateSubstance(substance: SubstanceEntity)

    @Delete
    suspend fun deleteSubstance(substance: SubstanceEntity)

    @Query("DELETE FROM substances WHERE id = :id")
    suspend fun deleteSubstanceById(id: Long)
}

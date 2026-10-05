package app.unpolished.inksober.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "journal_entries")
data class JournalEntryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val dateEpochDay: Long,
    val substanceId: Long = 0L,
    val title: String,
    val body: String,
    val mood: String = "Neutral",
    val timestampMillis: Long = System.currentTimeMillis()
)

package app.unpolished.inksober.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "recovery_quotes")
data class RecoveryQuoteEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val quoteText: String,
    val author: String,
    val dayOfYear: Int? = null
)

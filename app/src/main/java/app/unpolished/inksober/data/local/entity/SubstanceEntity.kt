package app.unpolished.inksober.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "substances")
data class SubstanceEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val startDateMillis: Long,
    val category: String = "General",
    val icon: String = "default",
    val initialNote: String = ""
)

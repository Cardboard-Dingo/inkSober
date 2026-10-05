package app.unpolished.inksober.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import app.unpolished.inksober.data.local.dao.JournalEntryDao
import app.unpolished.inksober.data.local.dao.RecoveryQuoteDao
import app.unpolished.inksober.data.local.dao.SubstanceDao
import app.unpolished.inksober.data.local.entity.JournalEntryEntity
import app.unpolished.inksober.data.local.entity.RecoveryQuoteEntity
import app.unpolished.inksober.data.local.entity.SubstanceEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        SubstanceEntity::class,
        JournalEntryEntity::class,
        RecoveryQuoteEntity::class,
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun substanceDao(): SubstanceDao
    abstract fun journalEntryDao(): JournalEntryDao
    abstract fun recoveryQuoteDao(): RecoveryQuoteDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context, scope: CoroutineScope = CoroutineScope(Dispatchers.IO)): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "inksober_database"
                )
                .addCallback(DatabaseCallback(scope))
                .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback(
            private val scope: CoroutineScope
        ) : Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch {
                        database.recoveryQuoteDao().insertQuotes(InitialQuoteSeed.quotes)
                    }
                }
            }
        }
    }
}

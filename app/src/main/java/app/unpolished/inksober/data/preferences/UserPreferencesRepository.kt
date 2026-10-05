package app.unpolished.inksober.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import app.unpolished.inksober.domain.model.UserPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

class UserPreferencesRepository(private val context: Context) {

    private object PreferencesKeys {
        val ACTIVE_SUBSTANCE_ID = longPreferencesKey("active_substance_id")
        val E_INK_MODE_ENABLED = booleanPreferencesKey("e_ink_mode_enabled")
        val DARK_MODE_ENABLED = booleanPreferencesKey("dark_mode_enabled")
    }

    val userPreferencesFlow: Flow<UserPreferences> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            val activeSubstanceId = preferences[PreferencesKeys.ACTIVE_SUBSTANCE_ID] ?: 1L
            val isEInkModeEnabled = preferences[PreferencesKeys.E_INK_MODE_ENABLED] ?: false
            val isDarkModeEnabled = preferences[PreferencesKeys.DARK_MODE_ENABLED] ?: false
            UserPreferences(
                activeSubstanceId = activeSubstanceId,
                isEInkModeEnabled = isEInkModeEnabled,
                isDarkModeEnabled = isDarkModeEnabled
            )
        }

    suspend fun setActiveSubstanceId(id: Long) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.ACTIVE_SUBSTANCE_ID] = id
        }
    }

    suspend fun setEInkModeEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.E_INK_MODE_ENABLED] = enabled
        }
    }

    suspend fun setDarkModeEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.DARK_MODE_ENABLED] = enabled
        }
    }
}

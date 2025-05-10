package services

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.time.LocalTime
import org.koin.core.component.KoinComponent

class AppPreferencesService(private val dataStore: DataStore<Preferences>) : KoinComponent {
    // Night Block Preferences
    private val nightBlockTimeKey = stringPreferencesKey("night_block_time")
    private val nightBlockActiveKey = stringPreferencesKey("night_block_active")
    private val nightBlockWhitelistKey = stringSetPreferencesKey("night_block_whitelist")
    private val lastOverrideReasonKey = stringPreferencesKey("last_override_reason")

    // Night Block Time
    suspend fun saveNightBlockTime(time: LocalTime) {
        dataStore.edit { preferences ->
            preferences[nightBlockTimeKey] = "${time.hour}:${time.minute}"
        }
    }

    val nightBlockTime: Flow<LocalTime> = dataStore.data
        .map { preferences ->
            preferences[nightBlockTimeKey]?.let { timeStr ->
                val (hour, minute) = timeStr.split(":").map { it.toInt() }
                LocalTime.of(hour, minute)
            } ?: LocalTime.of(20, 30) // Default time: 8:30 PM
        }

    // Night Block Active State
    suspend fun saveNightBlockActive(isActive: Boolean) {
        dataStore.edit { preferences ->
            preferences[nightBlockActiveKey] = isActive.toString()
        }
    }

    val isNightBlockActive: Flow<Boolean> = dataStore.data
        .map { preferences ->
            preferences[nightBlockActiveKey]?.toBoolean() ?: false
        }

    // Night Block Whitelist
    suspend fun saveNightBlockWhitelist(habitIds: Set<String>) {
        dataStore.edit { preferences ->
            preferences[nightBlockWhitelistKey] = habitIds
        }
    }

    val nightBlockWhitelist: Flow<Set<String>> = dataStore.data
        .map { preferences ->
            preferences[nightBlockWhitelistKey] ?: emptySet()
        }

    // Last Override Reason
    suspend fun saveLastOverrideReason(reason: String?) {
        dataStore.edit { preferences ->
            if (reason != null) {
                preferences[lastOverrideReasonKey] = reason
            } else {
                preferences.remove(lastOverrideReasonKey)
            }
        }
    }

    val lastOverrideReason: Flow<String?> = dataStore.data
        .map { preferences ->
            preferences[lastOverrideReasonKey]
        }

    // Add more preference keys and methods as needed for other app-wide settings
} 
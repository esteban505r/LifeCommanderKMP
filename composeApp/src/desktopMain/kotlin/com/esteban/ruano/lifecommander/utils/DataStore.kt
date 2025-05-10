package utils

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import okio.Path.Companion.toPath
import java.io.File


val tokenKey = stringPreferencesKey("token")
val timersLoopEnabledKey = booleanPreferencesKey("timersLoopEnabled")
val timersKey = stringPreferencesKey("timers")
fun createDataStore(): DataStore<Preferences> {
    val file = File(System.getProperty("user.home"), ".config/LifeCommanderDesktop/file.preferences_pb")
    return PreferenceDataStoreFactory.createWithPath(
        produceFile = { file.toPath().toString().toPath() }
    )
}

internal const val dataStoreFileName = "life.preferences_pb"
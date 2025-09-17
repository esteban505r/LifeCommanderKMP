package services.auth

import com.esteban.ruano.lifecommander.utils.TokenStorage
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class TokenStorageImpl(private val dataStore: DataStore<Preferences>): TokenStorage {
    private val tokenKey = stringPreferencesKey("auth_token")

    override suspend fun saveToken(token: String) {
        dataStore.edit { preferences ->
            preferences[tokenKey] = token
        }
    }
    
    override suspend fun getToken(): String? {
        return dataStore.data.map { preferences ->
            preferences[tokenKey]
        }.first()
    }

    
    override suspend fun clearToken() {
        dataStore.edit { preferences ->
            preferences.remove(tokenKey)
        }
    }
} 
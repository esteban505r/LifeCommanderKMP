package com.esteban.ruano.timers_presentation.ui.screens.viewmodel

import com.esteban.ruano.core.domain.preferences.Preferences
import com.esteban.ruano.lifecommander.utils.TokenStorage
import kotlinx.coroutines.flow.first

class PreferencesTokenStorage(
    private val preferences: Preferences
) : TokenStorage {
    override suspend fun saveToken(token: String) {
        preferences.saveAuthToken(token)
    }

    override suspend fun getToken(): String? {
        return preferences.loadAuthToken().first().takeIf { it.isNotEmpty() }
    }

    override suspend fun clearToken() {
        preferences.clearAuthToken()
    }
}



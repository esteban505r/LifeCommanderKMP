package com.esteban.ruano.repository

import com.esteban.ruano.lifecommander.models.UserSettings
import com.esteban.ruano.service.SettingsService

class SettingsRepository(private val settingsService: SettingsService) {
    
    fun getUserSettings(userId: Int): UserSettings {
        return settingsService.getUserSettings(userId)
    }
    
    fun updateUserSettings(userId: Int, settings: UserSettings): UserSettings {
        return settingsService.updateUserSettings(userId, settings)
    }
} 
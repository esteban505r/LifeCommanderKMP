package com.esteban.ruano.lifecommander.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.esteban.ruano.lifecommander.models.UserSettings
import com.esteban.ruano.lifecommander.services.settings.SettingsService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val settingsService: SettingsService
) : ViewModel() {

    private val _settings = MutableStateFlow<UserSettings?>(null)
    val settings: StateFlow<UserSettings?> = _settings.asStateFlow()
    
    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    private val _saving = MutableStateFlow(false)
    val saving: StateFlow<Boolean> = _saving.asStateFlow()
    
    fun loadSettings() {
        viewModelScope.launch {
            try {
                _loading.value = true
                _error.value = null
                _settings.value = settingsService.getUserSettings()
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to load settings"
            } finally {
                _loading.value = false
            }
        }
    }
    
    fun updateSettings(settings: UserSettings) {
        viewModelScope.launch {
            try {
                _saving.value = true
                _error.value = null
                val updatedSettings = settingsService.updateUserSettings(settings)
                _settings.value = updatedSettings
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to update settings"
            } finally {
                _saving.value = false
            }
        }
    }
} 
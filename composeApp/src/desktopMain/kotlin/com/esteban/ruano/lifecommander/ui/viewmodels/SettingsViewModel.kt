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
    
    private val _testNotificationResult = MutableStateFlow<String?>(null)
    val testNotificationResult: StateFlow<String?> = _testNotificationResult.asStateFlow()
    
    private val _testDueTasksNotificationResult = MutableStateFlow<String?>(null)
    val testDueTasksNotificationResult: StateFlow<String?> = _testDueTasksNotificationResult.asStateFlow()
    
    private val _testDueHabitsNotificationResult = MutableStateFlow<String?>(null)
    val testDueHabitsNotificationResult: StateFlow<String?> = _testDueHabitsNotificationResult.asStateFlow()
    
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
    
    fun testNotification() {
        viewModelScope.launch {
            try {
                _error.value = null
                _testNotificationResult.value = null
                
                val result = settingsService.testNotification()
                val message = result["message"] as? String ?: "Test notification sent successfully"
                val tokensCount = result["tokensCount"] as? Int ?: 0
                
                _testNotificationResult.value = "$message (sent to $tokensCount device(s))"
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to send test notification"
            }
        }
    }
    
    fun testDueTasksNotification() {
        viewModelScope.launch {
            try {
                _error.value = null
                _testDueTasksNotificationResult.value = null
                
                val result = settingsService.testDueTasksNotification()
                val message = result["message"] as? String ?: "Due tasks test notification sent successfully"
                val tokensCount = result["tokensCount"] as? Int ?: 0
                
                _testDueTasksNotificationResult.value = "$message (sent to $tokensCount device(s))"
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to send due tasks test notification"
            }
        }
    }
    
    fun testDueHabitsNotification() {
        viewModelScope.launch {
            try {
                _error.value = null
                _testDueHabitsNotificationResult.value = null
                
                val result = settingsService.testDueHabitsNotification()
                val message = result["message"] as? String ?: "Due habits test notification sent successfully"
                val tokensCount = result["tokensCount"] as? Int ?: 0
                
                _testDueHabitsNotificationResult.value = "$message (sent to $tokensCount device(s))"
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to send due habits test notification"
            }
        }
    }
    
    fun clearTestNotificationResult() {
        _testNotificationResult.value = null
    }
    
    fun clearTestDueTasksNotificationResult() {
        _testDueTasksNotificationResult.value = null
    }
    
    fun clearTestDueHabitsNotificationResult() {
        _testDueHabitsNotificationResult.value = null
    }
} 
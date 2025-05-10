package services

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.LocalTime
import utils.BackgroundServiceManager
import utils.StatusBarService

class NightBlockService(
    private val appPreferencesService: AppPreferencesService,
    private val statusBarService: StatusBarService
) {
    private val _isNightBlockActive = MutableStateFlow(false)
    val isNightBlockActive: StateFlow<Boolean> = _isNightBlockActive

    private val _nightBlockTime = MutableStateFlow(LocalTime.of(20, 30)) // 8:30 PM
    val nightBlockTime: StateFlow<LocalTime> = _nightBlockTime

    private val _lastOverrideReason = MutableStateFlow<String?>(null)
    val lastOverrideReason: StateFlow<String?> = _lastOverrideReason

    private val _whitelistedHabits = MutableStateFlow<Set<String>>(emptySet())
    val whitelistedHabits: StateFlow<Set<String>> = _whitelistedHabits

    init {
        // Load initial values from preferences
        kotlinx.coroutines.MainScope().launch {
            appPreferencesService.isNightBlockActive.collect { isActive ->
                _isNightBlockActive.value = isActive
                statusBarService.updateNightBlockStatus(isActive)
            }
        }
        
        kotlinx.coroutines.MainScope().launch {
            appPreferencesService.nightBlockTime.collect { time ->
                _nightBlockTime.value = time
            }
        }
        
        kotlinx.coroutines.MainScope().launch {
            appPreferencesService.lastOverrideReason.collect { reason ->
                _lastOverrideReason.value = reason
            }
        }
        
        kotlinx.coroutines.MainScope().launch {
            appPreferencesService.nightBlockWhitelist.collect { whitelist ->
                _whitelistedHabits.value = whitelist
            }
        }
    }

    suspend fun toggleNightBlock() {
        val newValue = !_isNightBlockActive.value
        _isNightBlockActive.value = newValue
        appPreferencesService.saveNightBlockActive(newValue)
        statusBarService.updateNightBlockStatus(newValue)
    }

    suspend fun setNightBlockTime(hour: Int, minute: Int) {
        val newTime = LocalTime.of(hour, minute)
        _nightBlockTime.value = newTime
        appPreferencesService.saveNightBlockTime(newTime)
    }

    suspend fun overrideNightBlock(reason: String) {
        _lastOverrideReason.value = reason
        _isNightBlockActive.value = false
        appPreferencesService.saveLastOverrideReason(reason)
        appPreferencesService.saveNightBlockActive(false)
        statusBarService.updateNightBlockStatus(false)
    }

    suspend fun checkAndActivateNightBlock() {
        val currentTime = LocalTime.now()
        if (currentTime.isAfter(_nightBlockTime.value) || currentTime == _nightBlockTime.value) {
            _isNightBlockActive.value = true
            appPreferencesService.saveNightBlockActive(true)
            statusBarService.updateNightBlockStatus(true)
        } else {
            val timeUntilActivation = Duration.between(currentTime, _nightBlockTime.value)
            statusBarService.updateNightBlockStatus(false, formatDuration(timeUntilActivation))
        }
    }

    private fun formatDuration(duration: Duration): String {
        val hours = duration.toHours()
        val minutes = duration.toMinutesPart()
        return when {
            hours > 0 -> "${hours}h ${minutes}m"
            else -> "${minutes}m"
        }
    }

    suspend fun toggleWhitelistedHabit(habitId: String) {
        val currentWhitelist = _whitelistedHabits.value.toMutableSet()
        if (currentWhitelist.contains(habitId)) {
            currentWhitelist.remove(habitId)
        } else {
            currentWhitelist.add(habitId)
        }
        _whitelistedHabits.value = currentWhitelist
        appPreferencesService.saveNightBlockWhitelist(currentWhitelist)
    }
} 
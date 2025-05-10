package com.esteban.ruano.core.domain.preferences

import androidx.datastore.preferences.core.stringPreferencesKey
import com.esteban.ruano.core.domain.model.ActivityLevel
import com.esteban.ruano.core.domain.model.Gender
import com.esteban.ruano.core.domain.model.GoalType
import com.esteban.ruano.core.domain.model.UserInfo
import kotlinx.coroutines.flow.Flow

interface Preferences {
    suspend fun saveGender(gender: Gender)
    suspend fun saveAge(age:Int)
    suspend fun saveWeight(weight:Float)
    suspend fun saveHeight(height:Int)
    suspend fun saveActivityLevel(level:ActivityLevel)
    suspend fun saveGoalType(goalType: GoalType)
    suspend fun saveCarbRatio(ratio:Float)
    suspend fun saveProteinRatio(ratio:Float)
    suspend fun saveFatRatio(ratio: Float)
    suspend fun saveOfflineMode(offlineMode: Boolean)
    suspend fun saveAuthToken(token: String)

    fun loadUserInfo(): Flow<UserInfo>
    fun loadShouldShowOnboarding(): Flow<Boolean>
    fun loadOfflineMode(): Flow<Boolean>
    suspend fun saveShouldShowOnboarding(shouldShow: Boolean)
    fun loadLastFetchTime(): Flow<Long>
    fun loadLastSyncTime(): Flow<Long>
    fun loadAuthToken(): Flow<String>
    suspend fun saveLastFetchTime(time: Long)
    suspend fun saveLastSyncTime(time: Long)
    suspend fun saveTaskLastNotificationReminderTime(time: String)
    suspend fun saveHabitLastNotificationReminderTime(time: String)
    fun loadLastTaskNotificationReminderTime(): Flow<String>
    fun loadLastHabitNotificationReminderTime(): Flow<String>

    companion object {
        const val KEY_GENDER = "gender"
        const val KEY_AGE = "age"
        const val KEY_WEIGHT = "weight"
        const val KEY_HEIGHT = "height"
        const val KEY_ACTIVITY_LEVEL = "activity_level"
        const val KEY_GOAL_TYPE = "goal_type"
        const val KEY_CARB_RATIO = "carb_ratio"
        const val KEY_PROTEIN_RATIO = "protein_ratio"
        const val KEY_FAT_RATIO = "fat_ratio"
        val KEY_SHOULD_SHOW_ONBOARDING = stringPreferencesKey("should_show_onboarding")
        val KEY_LAST_FETCH_TIME = stringPreferencesKey("last_fetch_time")
        val KEY_LAST_SYNC_TIME = stringPreferencesKey("last_sync_time")
        val KEY_OFFLINE_MODE = stringPreferencesKey("offline_mode")
        val KEY_AUTH_TOKEN = stringPreferencesKey("auth_token")
        val KEY_LAST_TASK_NOTIFICATION_REMINDER_TIME = stringPreferencesKey("last_task_notification_reminder_time")
        val KEY_LAST_HABIT_NOTIFICATION_REMINDER_TIME = stringPreferencesKey("last_habit_notification_reminder_time")
    }
}
package com.esteban.ruano.core.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.esteban.ruano.core.domain.model.ActivityLevel
import com.esteban.ruano.core.domain.model.Gender
import com.esteban.ruano.core.domain.model.GoalType
import com.esteban.ruano.core.domain.model.UserInfo
import com.esteban.ruano.core.domain.preferences.Preferences as LifePreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private const val PREFERENCES_NAME = "life_commander_preferences"

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = PREFERENCES_NAME)

class DataStorePreferences(context: Context) : LifePreferences {

    private val dataStore = context.dataStore

    companion object {
        private val KEY_GENDER = stringPreferencesKey("gender")
        private val KEY_AGE = intPreferencesKey("age")
        private val KEY_WEIGHT = floatPreferencesKey("weight")
        private val KEY_HEIGHT = intPreferencesKey("height")
        private val KEY_ACTIVITY_LEVEL = stringPreferencesKey("activity_level")
        private val KEY_GOAL_TYPE = stringPreferencesKey("goal_type")
        private val KEY_CARB_RATIO = floatPreferencesKey("carb_ratio")
        private val KEY_PROTEIN_RATIO = floatPreferencesKey("protein_ratio")
        private val KEY_FAT_RATIO = floatPreferencesKey("fat_ratio")
        private val KEY_OFFLINE_MODE = booleanPreferencesKey("offline_mode")
        private val KEY_AUTH_TOKEN = stringPreferencesKey("auth_token")
        private val KEY_SHOULD_SHOW_ONBOARDING = booleanPreferencesKey("should_show_onboarding")
        private val KEY_LAST_FETCH_TIME = longPreferencesKey("last_fetch_time")
        private val KEY_LAST_SYNC_TIME = longPreferencesKey("last_sync_time")
        private val KEY_LAST_TASK_NOTIFICATION_REMINDER_TIME = stringPreferencesKey("last_task_notification_reminder_time")
        private val KEY_LAST_HABIT_NOTIFICATION_REMINDER_TIME = stringPreferencesKey("last_habit_notification_reminder_time")
    }

    override suspend fun saveGender(gender: Gender) {
        dataStore.edit { it[KEY_GENDER] = gender.name }
    }

    override suspend fun saveAge(age: Int) {
        dataStore.edit { it[KEY_AGE] = age }
    }

    override suspend fun saveWeight(weight: Float) {
        dataStore.edit { it[KEY_WEIGHT] = weight }
    }

    override suspend fun saveHeight(height: Int) {
        dataStore.edit { it[KEY_HEIGHT] = height }
    }

    override suspend fun saveActivityLevel(level: ActivityLevel) {
        dataStore.edit { it[KEY_ACTIVITY_LEVEL] = level.name }
    }

    override suspend fun saveGoalType(goalType: GoalType) {
        dataStore.edit { it[KEY_GOAL_TYPE] = goalType.name }
    }

    override suspend fun saveCarbRatio(ratio: Float) {
        dataStore.edit { it[KEY_CARB_RATIO] = ratio }
    }

    override suspend fun saveProteinRatio(ratio: Float) {
        dataStore.edit { it[KEY_PROTEIN_RATIO] = ratio }
    }

    override suspend fun saveFatRatio(ratio: Float) {
        dataStore.edit { it[KEY_FAT_RATIO] = ratio }
    }

    override suspend fun saveOfflineMode(offlineMode: Boolean) {
        dataStore.edit { it[KEY_OFFLINE_MODE] = offlineMode }
    }

    override suspend fun saveAuthToken(token: String) {
        dataStore.edit { it[KEY_AUTH_TOKEN] = token }
    }

    override suspend fun clearAuthToken() {
        dataStore.edit { it.remove(KEY_AUTH_TOKEN) }
    }

    override suspend fun saveShouldShowOnboarding(shouldShow: Boolean) {
        dataStore.edit { it[KEY_SHOULD_SHOW_ONBOARDING] = shouldShow }
    }

    override suspend fun saveLastFetchTime(time: Long) {
        dataStore.edit { it[KEY_LAST_FETCH_TIME] = time }
    }

    override suspend fun saveLastSyncTime(time: Long) {
        dataStore.edit { it[KEY_LAST_SYNC_TIME] = time }
    }

    override suspend fun saveTaskLastNotificationReminderTime(time: String) {
        dataStore.edit { it[KEY_LAST_TASK_NOTIFICATION_REMINDER_TIME] = time }
    }

    override suspend fun saveHabitLastNotificationReminderTime(time: String) {
        dataStore.edit { it[KEY_LAST_HABIT_NOTIFICATION_REMINDER_TIME] = time }
    }

    override fun loadUserInfo(): Flow<UserInfo> {
        return dataStore.data.map { preferences ->
            val genderString = preferences[KEY_GENDER] ?: "male"
            val age = preferences[KEY_AGE] ?: -1
            val weight = preferences[KEY_WEIGHT] ?: -1f
            val height = preferences[KEY_HEIGHT] ?: -1
            val activityLevelString = preferences[KEY_ACTIVITY_LEVEL] ?: "medium"
            val goalType = preferences[KEY_GOAL_TYPE] ?: "keep_weight"
            val carbRatio = preferences[KEY_CARB_RATIO] ?: -1f
            val proteinRatio = preferences[KEY_PROTEIN_RATIO] ?: -1f
            val fatRatio = preferences[KEY_FAT_RATIO] ?: -1f

            UserInfo(
                gender = Gender.fromString(genderString),
                age = age,
                weight = weight,
                height = height,
                activityLevel = ActivityLevel.fromString(activityLevelString),
                goalType = GoalType.fromString(goalType),
                carbRatio = carbRatio,
                proteinRatio = proteinRatio,
                fatRatio = fatRatio
            )
        }
    }

    override fun loadShouldShowOnboarding(): Flow<Boolean> {
        return dataStore.data.map { it[KEY_SHOULD_SHOW_ONBOARDING] ?: true }
    }

    override fun loadOfflineMode(): Flow<Boolean> {
        return dataStore.data.map { it[KEY_OFFLINE_MODE] ?: true }
    }

    override fun loadLastFetchTime(): Flow<Long> {
        return dataStore.data.map { it[KEY_LAST_FETCH_TIME] ?: 0L }
    }

    override fun loadLastSyncTime(): Flow<Long> {
        return dataStore.data.map { it[KEY_LAST_SYNC_TIME] ?: 0L }
    }

    override fun loadAuthToken(): Flow<String> {
        return dataStore.data.map { it[KEY_AUTH_TOKEN] ?: "" }
    }

    override fun loadLastTaskNotificationReminderTime(): Flow<String> {
        return dataStore.data.map { it[KEY_LAST_TASK_NOTIFICATION_REMINDER_TIME] ?: "00:00" }
    }

    override fun loadLastHabitNotificationReminderTime(): Flow<String> {
        return dataStore.data.map { it[KEY_LAST_HABIT_NOTIFICATION_REMINDER_TIME] ?: "00:00" }
    }
}

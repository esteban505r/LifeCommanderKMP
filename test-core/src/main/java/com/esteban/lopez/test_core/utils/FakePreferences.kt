package com.esteban.ruano.test_core.utils

import com.esteban.ruano.core.domain.model.ActivityLevel
import com.esteban.ruano.core.domain.model.Gender
import com.esteban.ruano.core.domain.model.GoalType
import com.esteban.ruano.core.domain.model.UserInfo
import com.esteban.ruano.core.domain.preferences.Preferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class FakePreferences: Preferences {
    private var gender: Gender? = null
    private var age: Int = 0
    private var weight: Float = 0f
    private var height: Int = 0
    private var activityLevel: ActivityLevel? = null
    private var goalType: GoalType? = null
    private var carbRatio: Float = 0f
    private var proteinRatio: Float = 0f
    private var fatRatio: Float = 0f
    private var offlineMode: Boolean = false
    private var authToken: String = ""
    private var shouldShowOnboarding: Boolean = true
    private var lastFetchTime: Long = 0L
    private var lastSyncTime: Long = 0L
    private var taskLastNotificationTime: String = ""
    private var habitLastNotificationTime: String = ""

    override suspend fun saveGender(gender: Gender) {
        this.gender = gender
    }

    override suspend fun saveAge(age: Int) {
        this.age = age
    }

    override suspend fun saveWeight(weight: Float) {
        this.weight = weight
    }

    override suspend fun saveHeight(height: Int) {
        this.height = height
    }

    override suspend fun saveActivityLevel(level: ActivityLevel) {
        this.activityLevel = level
    }

    override suspend fun saveGoalType(goalType: GoalType) {
        this.goalType = goalType
    }

    override suspend fun saveCarbRatio(ratio: Float) {
        this.carbRatio = ratio
    }

    override suspend fun saveProteinRatio(ratio: Float) {
        this.proteinRatio = ratio
    }

    override suspend fun saveFatRatio(ratio: Float) {
        this.fatRatio = ratio
    }

    override suspend fun saveOfflineMode(offlineMode: Boolean) {
        this.offlineMode = offlineMode
    }

    override suspend fun saveAuthToken(token: String) {
        this.authToken = token
    }

    override suspend fun clearAuthToken() {
        this.authToken = ""
    }

    override fun loadUserInfo(): Flow<UserInfo> = flow {
        emit(UserInfo(
            gender = gender ?: Gender.Male,
            age = age,
            weight = weight,
            height = height,
            activityLevel = activityLevel ?: ActivityLevel.Medium,
            goalType = goalType ?: GoalType.KeepWeight,
            carbRatio = carbRatio,
            proteinRatio = proteinRatio,
            fatRatio = fatRatio
        ))
    }

    override fun loadShouldShowOnboarding(): Flow<Boolean> = flow {
        emit(shouldShowOnboarding)
    }

    override fun loadOfflineMode(): Flow<Boolean> = flow {
        emit(offlineMode)
    }

    override suspend fun saveShouldShowOnboarding(shouldShow: Boolean) {
        this.shouldShowOnboarding = shouldShow
    }

    override fun loadLastFetchTime(): Flow<Long> = flow {
        emit(lastFetchTime)
    }

    override fun loadLastSyncTime(): Flow<Long> = flow {
        emit(lastSyncTime)
    }

    override fun loadAuthToken(): Flow<String> = flow {
        emit(authToken)
    }

    override suspend fun saveLastFetchTime(time: Long) {
        this.lastFetchTime = time
    }

    override suspend fun saveLastSyncTime(time: Long) {
        this.lastSyncTime = time
    }

    override suspend fun saveTaskLastNotificationReminderTime(time: String) {
        this.taskLastNotificationTime = time
    }

    override suspend fun saveHabitLastNotificationReminderTime(time: String) {
        this.habitLastNotificationTime = time
    }

    override fun loadLastTaskNotificationReminderTime(): Flow<String> = flow {
        emit(taskLastNotificationTime)
    }

    override fun loadLastHabitNotificationReminderTime(): Flow<String> = flow {
        emit(habitLastNotificationTime)
    }
}
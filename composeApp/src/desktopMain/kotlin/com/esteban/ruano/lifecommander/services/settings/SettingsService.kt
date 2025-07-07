package com.esteban.ruano.lifecommander.services.settings

import com.esteban.ruano.lifecommander.models.UserSettings
import com.esteban.ruano.lifecommander.utils.appHeaders
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import services.auth.TokenStorageImpl

class SettingsService(
    private val baseUrl: String,
    private val httpClient: HttpClient,
    private val tokenStorageImpl: TokenStorageImpl
) {
    suspend fun getUserSettings(): UserSettings {
        return httpClient.get("$baseUrl/settings") {
            appHeaders(tokenStorageImpl.getToken())
        }.body()
    }

    suspend fun updateUserSettings(settings: UserSettings): UserSettings {
        val response = httpClient.put("$baseUrl/settings") {
            contentType(ContentType.Application.Json)
            appHeaders(tokenStorageImpl.getToken())
            setBody(settings)
        }
        
        if (response.status != HttpStatusCode.OK) {
            throw Exception("Failed to update user settings: ${response.status}")
        }
        
        return response.body()
    }
    
    suspend fun testNotification(): Map<String, String> {
        val response = httpClient.post("$baseUrl/settings/test-notification") {
            appHeaders(tokenStorageImpl.getToken())
        }
        
        if (response.status != HttpStatusCode.OK) {
            throw Exception("Failed to send test notification: ${response.status}")
        }

        return response.body()
    }
    
    suspend fun testDueTasksNotification(): Map<String, String> {
        val response = httpClient.post("$baseUrl/settings/test-due-tasks-notification") {
            appHeaders(tokenStorageImpl.getToken())
        }
        
        if (response.status != HttpStatusCode.OK) {
            throw Exception("Failed to send due tasks test notification: ${response.status}")
        }

        return response.body()
    }
    
    suspend fun testDueHabitsNotification(): Map<String, String> {
        val response = httpClient.post("$baseUrl/settings/test-due-habits-notification") {
            appHeaders(tokenStorageImpl.getToken())
        }
        
        if (response.status != HttpStatusCode.OK) {
            throw Exception("Failed to send due habits test notification: ${response.status}")
        }

        return response.body()
    }
} 
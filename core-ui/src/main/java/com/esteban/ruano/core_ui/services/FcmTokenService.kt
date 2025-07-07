package com.esteban.ruano.core_ui.services

import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FcmTokenService @Inject constructor() {

    suspend fun getFcmToken(): String? {
        return try {
            Log.i("FcmTokenService", "Attempting to retrieve FCM token...")
            
            // Check if Firebase is initialized
            if (!isFirebaseInitialized()) {
                Log.w("FcmTokenService", "Firebase is not initialized, cannot retrieve FCM token")
                return null
            }
            
            Log.i("FcmTokenService", "Firebase is initialized, proceeding with FCM token retrieval")
            
            // Check if this is an emulator
            if (isEmulator()) {
                Log.w("FcmTokenService", "Device appears to be an emulator - FCM may not work properly")
                Log.w("FcmTokenService", "Consider testing on a physical device for FCM functionality")
            }
            
            val token = FirebaseMessaging.getInstance().token.await()
            if (token != null && token.isNotEmpty()) {
                Log.i("FcmTokenService", "FCM token retrieved successfully: ${token.take(20)}...")
                token
            } else {
                Log.w("FcmTokenService", "FCM token retrieval returned null or empty token")
                null
            }
        } catch (e: Exception) {
            Log.w("FcmTokenService", "FCM token retrieval failed: ${e.message}")
            
            // Provide specific guidance based on the error
            when {
                e.message?.contains("SERVICE_NOT_AVAILABLE") == true -> {
                    Log.w("FcmTokenService", "SERVICE_NOT_AVAILABLE error detected")
                    Log.w("FcmTokenService", "This usually means:")
                    Log.w("FcmTokenService", "1. Running on emulator without Google Play Services")
                    Log.w("FcmTokenService", "2. Google Play Services is not available")
                    Log.w("FcmTokenService", "3. Device has network connectivity issues")
                    Log.w("FcmTokenService", "4. Firebase project configuration issues")
                }
                e.message?.contains("NETWORK_ERROR") == true -> {
                    Log.w("FcmTokenService", "Network error during FCM token retrieval")
                }
                e.message?.contains("TIMEOUT") == true -> {
                    Log.w("FcmTokenService", "Timeout during FCM token retrieval")
                }
                else -> {
                    Log.w("FcmTokenService", "Unknown error during FCM token retrieval: ${e.message}")
                }
            }
            
            null
        }
    }

    private fun isFirebaseInitialized(): Boolean {
        return try {
            val firebaseApp = FirebaseApp.getInstance()
            firebaseApp != null
        } catch (e: Exception) {
            Log.w("FcmTokenService", "Firebase not initialized: ${e.message}")
            false
        }
    }

    private fun isEmulator(): Boolean {
        return try {
            val isEmulator = android.os.Build.FINGERPRINT.startsWith("generic") ||
            android.os.Build.FINGERPRINT.startsWith("unknown") ||
            android.os.Build.MODEL.contains("google_sdk") ||
            android.os.Build.MODEL.contains("Emulator") ||
            android.os.Build.MODEL.contains("Android SDK built for x86") ||
            android.os.Build.MANUFACTURER.contains("Genymotion") ||
            (android.os.Build.BRAND.startsWith("generic") && android.os.Build.DEVICE.startsWith("generic")) ||
            "google_sdk" == android.os.Build.PRODUCT
            
            Log.i("FcmTokenService", "Device info - Emulator: $isEmulator, Model: ${android.os.Build.MODEL}, Manufacturer: ${android.os.Build.MANUFACTURER}")
            isEmulator
        } catch (e: Exception) {
            Log.w("FcmTokenService", "Could not determine if device is emulator: ${e.message}")
            false
        }
    }
} 
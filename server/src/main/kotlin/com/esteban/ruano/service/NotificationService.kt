package com.esteban.ruano.service

import com.google.api.core.ApiFuture
import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.messaging.BatchResponse
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.FirebaseMessagingException
import com.google.firebase.messaging.Message
import com.google.firebase.messaging.Notification
import com.google.firebase.messaging.SendResponse
import io.sentry.Sentry
import java.io.FileNotFoundException
import java.io.InputStream

class NotificationService() {
    private var initialized = false

    private fun initialize() {
        if (!initialized) {
            try {
                // Load the service account file from the classpath
                val serviceAccount: InputStream = javaClass.classLoader
                    .getResourceAsStream("firebase-service-account.json")
                    ?: throw FileNotFoundException("firebase-service-account.json not found in classpath")
                
                val options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .build()
                
                // Check if Firebase is already initialized
                if (FirebaseApp.getApps().isEmpty()) {
                    FirebaseApp.initializeApp(options)
                }
                initialized = true
                println("[NotificationService] Firebase initialized successfully")
            } catch (e: Exception) {
                println("[NotificationService] Error initializing Firebase: ${e.message}")
                Sentry.captureException(e)
                e.printStackTrace()
            }
        }
    }

    /**
     * Send a notification to a single device token
     * @return true if notification was sent successfully, false otherwise
     */
    fun sendNotificationToToken(token: String, title: String, body: String, data: Map<String, String> = emptyMap()): Boolean {
        initialize()
        if (!initialized) {
            println("[NotificationService] Cannot send notification: Firebase not initialized")
            return false
        }
        
        return try {
            val message = Message.builder()
                .setToken(token)
                .setNotification(Notification.builder().setTitle(title).setBody(body).build())
                .putAllData(data)
                .build()
            
            val future: ApiFuture<String> = FirebaseMessaging.getInstance().sendAsync(message)
            val messageId = future.get() // Wait for the result
            
            println("[NotificationService] Notification sent successfully to token: $token (messageId: $messageId)")
            true
        } catch (e: FirebaseMessagingException) {
            println("[NotificationService] Firebase error sending notification to token $token: ${e.errorCode} - ${e.message}")
            Sentry.captureException(e)
            false
        } catch (e: Exception) {
            println("[NotificationService] Error sending notification to token $token: ${e.message}")
            Sentry.captureException(e)
            e.printStackTrace()
            false
        }
    }

    /**
     * Send notifications to multiple device tokens
     * @return Number of successfully sent notifications
     */
    fun sendNotificationToTokens(tokens: List<String>, title: String, body: String, data: Map<String, String> = emptyMap()): Int {
        if (tokens.isEmpty()) {
            println("[NotificationService] No tokens provided for notification")
            return 0
        }
        
        initialize()
        if (!initialized) {
            println("[NotificationService] Cannot send notifications: Firebase not initialized")
            return 0
        }
        
        return try {
            val messages = tokens.map { token ->
                Message.builder()
                    .setToken(token)
                    .setNotification(Notification.builder().setTitle(title).setBody(body).build())
                    .putAllData(data)
                    .build()
            }
            
            val future: ApiFuture<BatchResponse> = FirebaseMessaging.getInstance().sendEachAsync(messages)
            val batchResponse = future.get() // Wait for the result
            
            val successCount = batchResponse.responses.count { it.isSuccessful }
            val failureCount = batchResponse.responses.size - successCount
            
            if (failureCount > 0) {
                val failures = batchResponse.responses
                    .filter { !it.isSuccessful }
                    .mapIndexed { index, response ->
                        "Token ${tokens[index]}: ${response.exception?.errorCode ?: "Unknown error"}"
                    }
                println("[NotificationService] Sent $successCount/${tokens.size} notifications. Failures: ${failures.joinToString(", ")}")
            } else {
                println("[NotificationService] Successfully sent notifications to all ${tokens.size} tokens")
            }
            
            // Log individual failures to Sentry
            batchResponse.responses.forEachIndexed { index, response ->
                if (!response.isSuccessful && response.exception != null) {
                    Sentry.captureException(response.exception)
                }
            }
            
            successCount
        } catch (e: Exception) {
            println("[NotificationService] Error sending notifications to tokens: ${e.message}")
            Sentry.captureException(e)
            e.printStackTrace()
            0
        }
    }
} 
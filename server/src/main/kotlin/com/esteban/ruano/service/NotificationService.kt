package com.esteban.ruano.service

import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.Message
import com.google.firebase.messaging.Notification
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
                println("Firebase initialized successfully")
            } catch (e: Exception) {
                println("Error initializing Firebase: ${e.message}")
                e.printStackTrace()
            }
        }
    }

    fun sendNotificationToToken(token: String, title: String, body: String, data: Map<String, String> = emptyMap()) {
        initialize()
        try {
            val message = Message.builder()
                .setToken(token)
                .setNotification(Notification.builder().setTitle(title).setBody(body).build())
                .putAllData(data)
                .build()
            
            FirebaseMessaging.getInstance().sendAsync(message)
            println("Notification sent to token: $token")
        } catch (e: Exception) {
            println("Error sending notification to token: ${e.message}")
            e.printStackTrace()
        }
    }

    fun sendNotificationToTokens(tokens: List<String>, title: String, body: String, data: Map<String, String> = emptyMap()) {
        if (tokens.isEmpty()) {
            println("No tokens provided for notification")
            return
        }
        
        initialize()
        try {
            val messages = tokens.map { token ->
                Message.builder()
                    .setToken(token)
                    .setNotification(Notification.builder().setTitle(title).setBody(body).build())
                    .putAllData(data)
                    .build()
            }
            
            FirebaseMessaging.getInstance().sendEachAsync(messages)
            println("Notifications sent to ${tokens.size} tokens")
        } catch (e: Exception) {
            println("Error sending notifications to tokens: ${e.message}")
            e.printStackTrace()
        }
    }
} 
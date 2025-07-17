package com.shikshak.transfer.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.shikshak.transfer.MainActivity
import com.shikshak.transfer.R
import com.shikshak.transfer.ui.theme.updates.NotificationItem
import timber.log.Timber
import com.google.firebase.messaging.FirebaseMessaging

class FCMService : FirebaseMessagingService() {
    
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    
    companion object {
        private const val CHANNEL_ID = "mutual_transfer_notifications"
        private const val CHANNEL_NAME = "Mutual Transfer Notifications"
        private const val CHANNEL_DESCRIPTION = "Notifications for mutual transfer updates"
    }
    
    override fun onCreate() {
        super.onCreate()
        Timber.d("=== FCM SERVICE CREATED ===")
        
        // Check current FCM token
        checkCurrentFCMToken()
    }
    
    private fun checkCurrentFCMToken() {
        FirebaseMessaging.getInstance().token.addOnSuccessListener { currentToken ->
            Timber.d("=== CURRENT FCM TOKEN CHECK ===")
            Timber.d("Current FCM token: $currentToken")
            
            val userId = auth.currentUser?.uid
            if (userId != null) {
                // Check what token is stored in Firestore
                firestore.collection("users")
                    .document(userId)
                    .get()
                    .addOnSuccessListener { document ->
                        val storedToken = document.getString("fcmToken")
                        Timber.d("Stored FCM token in Firestore: $storedToken")
                        Timber.d("Tokens match: ${currentToken == storedToken}")
                        
                        if (currentToken != storedToken) {
                            Timber.w("FCM token mismatch! Updating Firestore...")
                            // Create or update the user document
                            val userData = hashMapOf(
                                "fcmToken" to currentToken,
                                "lastUpdated" to System.currentTimeMillis()
                            )
                            
                            firestore.collection("users")
                                .document(userId)
                                .set(userData, com.google.firebase.firestore.SetOptions.merge())
                                .addOnSuccessListener {
                                    Timber.d("FCM token updated in Firestore")
                                }
                                .addOnFailureListener { exception ->
                                    Timber.e(exception, "Failed to update FCM token in Firestore")
                                }
                        }
                    }
                    .addOnFailureListener { exception ->
                        Timber.e(exception, "Failed to get stored FCM token from Firestore")
                        // If document doesn't exist, create it
                        Timber.d("Creating new user document for: $userId")
                        val userData = hashMapOf(
                            "fcmToken" to currentToken,
                            "lastUpdated" to System.currentTimeMillis()
                        )
                        
                        firestore.collection("users")
                            .document(userId)
                            .set(userData)
                            .addOnSuccessListener {
                                Timber.d("New user document created with FCM token")
                            }
                            .addOnFailureListener { createException ->
                                Timber.e(createException, "Failed to create user document")
                            }
                    }
            } else {
                Timber.w("Cannot check FCM token - no user ID available")
            }
        }.addOnFailureListener { exception ->
            Timber.e(exception, "Failed to get current FCM token")
        }
    }
    
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Timber.d("=== NEW FCM TOKEN RECEIVED ===")
        Timber.d("New FCM token: $token")
        
        // Save the new token to Firestore
        val userId = auth.currentUser?.uid
        if (userId != null) {
            Timber.d("Saving FCM token for user: $userId")
            firestore.collection("users")
                .document(userId)
                .update("fcmToken", token)
                .addOnSuccessListener {
                    Timber.d("FCM token saved successfully to Firestore")
                }
                .addOnFailureListener { exception ->
                    Timber.e(exception, "Error saving FCM token to Firestore")
                }
        } else {
            Timber.w("Cannot save FCM token - no user ID available")
            Timber.w("Current user: ${auth.currentUser}")
        }
    }
    
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        
        Timber.d("=== FCM MESSAGE RECEIVED ===")
        Timber.d("Message received from: ${remoteMessage.from}")
        Timber.d("Message data: ${remoteMessage.data}")
        Timber.d("Message notification: ${remoteMessage.notification}")
        Timber.d("Message ID: ${remoteMessage.messageId}")
        Timber.d("Message Type: ${remoteMessage.messageType}")
        Timber.d("Message Priority: ${remoteMessage.priority}")
        Timber.d("Message TTL: ${remoteMessage.ttl}")
        Timber.d("Message Collapse Key: ${remoteMessage.collapseKey}")
        Timber.d("Message Sent Time: ${remoteMessage.sentTime}")
        Timber.d("Message To: ${remoteMessage.to}")
        Timber.d("================================")
        
        // Extract notification details from data payload
        val title = remoteMessage.data["title"] 
            ?: remoteMessage.notification?.title 
            ?: "New Update"
        
        val message = remoteMessage.data["message"] 
            ?: remoteMessage.notification?.body 
            ?: ""
        
        val type = remoteMessage.data["type"] ?: "general"
        
        Timber.d("Processing notification - Title: $title, Message: $message, Type: $type")
        
        // Show system notification first
        showNotification(title, message, type)
        
        // Try to save notification to Firestore
        // Use a background thread to avoid blocking the UI
        Thread {
            try {
                saveNotificationToFirestore(title, message, type, remoteMessage.data)
            } catch (e: Exception) {
                Timber.e(e, "Error saving notification in background thread")
            }
        }.start()
    }
    
    private fun saveNotificationToFirestore(
        title: String,
        message: String,
        type: String,
        data: Map<String, String>
    ) {
        // Try to get user ID from multiple sources
        val userId = auth.currentUser?.uid 
            ?: data["userId"] 
            ?: data["user_id"]
        
        // Handle multicast notifications
        val userIds = data["userIds"] ?: data["user_ids"]
        
        Timber.d("=== SAVING NOTIFICATION ===")
        Timber.d("Title: $title")
        Timber.d("Message: $message")
        Timber.d("Type: $type")
        Timber.d("Data: $data")
        Timber.d("Auth current user: ${auth.currentUser}")
        Timber.d("User ID from auth: ${auth.currentUser?.uid}")
        Timber.d("User ID from data: ${data["userId"]}")
        Timber.d("User IDs from data: $userIds")
        Timber.d("Final user ID: $userId")
        
        if (userId != null && userId != "null") {
            saveNotificationForUser(title, message, type, data, userId)
        } else if (userIds != null) {
            // Handle multicast - save to all users
            val userIdList = userIds.split(",")
            Timber.d("Saving multicast notification to ${userIdList.size} users")
            userIdList.forEach { uid ->
                if (uid.isNotEmpty() && uid != "null") {
                    saveNotificationForUser(title, message, type, data, uid)
                }
            }
        } else {
            Timber.w("Cannot save notification - no valid user ID available")
            Timber.w("Current user: ${auth.currentUser}")
            Timber.w("Notification data: $data")
            
            // Try to save to general notifications collection
            saveToGeneralNotifications(title, message, type, data, "unknown")
        }
    }
    
    private fun saveNotificationForUser(
        title: String,
        message: String,
        type: String,
        data: Map<String, String>,
        userId: String
    ) {
        val notification = NotificationItem(
            id = "", // Will be set by Firestore
            title = title,
            message = message,
            timestamp = System.currentTimeMillis(),
            isRead = false,
            type = type,
            data = data
        )
        
        Timber.d("Saving notification to Firestore for user: $userId")
        Timber.d("Notification data: $notification")
        
        // First, ensure the user document exists
        val userData = hashMapOf(
            "lastUpdated" to System.currentTimeMillis()
        )
        
        firestore.collection("users")
            .document(userId)
            .set(userData, com.google.firebase.firestore.SetOptions.merge())
            .addOnSuccessListener {
                // Now save the notification
                firestore.collection("users")
                    .document(userId)
                    .collection("notifications")
                    .add(notification)
                    .addOnSuccessListener { documentReference ->
                        Timber.d("Notification saved successfully with ID: ${documentReference.id}")
                        Timber.d("Document path: ${documentReference.path}")
                    }
                    .addOnFailureListener { exception ->
                        Timber.e(exception, "Error saving notification to Firestore")
                        Timber.e("User ID: $userId")
                        Timber.e("Notification data: $notification")
                        
                        // Try alternative approach - save to a general notifications collection
                        saveToGeneralNotifications(title, message, type, data, userId)
                    }
            }
            .addOnFailureListener { exception ->
                Timber.e(exception, "Failed to create/update user document")
                // Try to save notification anyway
                firestore.collection("users")
                    .document(userId)
                    .collection("notifications")
                    .add(notification)
                    .addOnSuccessListener { documentReference ->
                        Timber.d("Notification saved successfully with ID: ${documentReference.id}")
                    }
                    .addOnFailureListener { notificationException ->
                        Timber.e(notificationException, "Error saving notification to Firestore")
                        saveToGeneralNotifications(title, message, type, data, userId)
                    }
            }
    }
    
    private fun saveToGeneralNotifications(
        title: String,
        message: String,
        type: String,
        data: Map<String, String>,
        userId: String
    ) {
        Timber.d("Attempting to save to general notifications collection")
        
        val notificationData = mapOf(
            "title" to title,
            "message" to message,
            "type" to type,
            "timestamp" to System.currentTimeMillis(),
            "isRead" to false,
            "userId" to userId,
            "data" to data
        )
        
        firestore.collection("notifications")
            .add(notificationData)
            .addOnSuccessListener { documentReference ->
                Timber.d("Notification saved to general collection with ID: ${documentReference.id}")
            }
            .addOnFailureListener { exception ->
                Timber.e(exception, "Failed to save to general notifications collection")
            }
    }
    
    private fun showNotification(title: String, message: String, type: String) {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        
        // Create notification channel for Android O and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = CHANNEL_DESCRIPTION
            }
            notificationManager.createNotificationChannel(channel)
        }
        
        // Create intent for notification tap
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("notification_type", type)
            // Add additional data for specific navigation
            when (type) {
                "match" -> {
                    putExtra("notification_action", "navigate_to_home")
                }
                "transfer_request" -> {
                    putExtra("notification_action", "navigate_to_request")
                }
                "general" -> {
                    putExtra("notification_action", "navigate_to_updates")
                }
                else -> {
                    putExtra("notification_action", "navigate_to_updates")
                }
            }
        }
        
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        // Build notification
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(message)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()
        
        // Show notification with unique ID
        val notificationId = System.currentTimeMillis().toInt()
        notificationManager.notify(notificationId, notification)
        Timber.d("System notification shown with ID: $notificationId")
    }
} 
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
import com.shikshak.transfer.ui.theme.utils.AppUpdateUtils

class FCMService : FirebaseMessagingService() {
    
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    
    companion object {
        private const val CHANNEL_ID = "mutual_transfer_notifications"
        private const val CHANNEL_NAME = "Mutual Transfer Notifications"
        private const val CHANNEL_DESCRIPTION = "Notifications for mutual transfer updates"
        
        // Topic constants
        private const val TOPIC_GENERAL = "general"
        private const val TOPIC_MATCHES = "matches"
        private const val TOPIC_TRANSFER_REQUESTS = "transfer_requests"
        private const val TOPIC_UPDATES = "updates"

        /**
         * Safe entry-point for saving notifications outside the Service lifecycle
         * (e.g. from MainActivity when a notification Intent is opened).
         * Do NOT construct FCMService() manually — Services need a real Android Context.
         */
        fun saveNotificationFromOutside(
            title: String,
            message: String,
            type: String,
            data: Map<String, String>
        ) {
            NotificationStore.save(title, message, type, data)
        }
    }
    
    override fun onCreate() {
        super.onCreate()
        Timber.d("=== FCM SERVICE CREATED ===")
        
        // Check current FCM token
        checkCurrentFCMToken()
        
        // Subscribe to relevant topics
        subscribeToTopics()
    }
    
    private fun subscribeToTopics() {
        Timber.d("=== SUBSCRIBING TO TOPICS ===")
        
        // Subscribe to general notifications
        FirebaseMessaging.getInstance().subscribeToTopic(TOPIC_GENERAL)
            .addOnSuccessListener {
                Timber.d("Successfully subscribed to topic: $TOPIC_GENERAL")
            }
            .addOnFailureListener { exception ->
                Timber.e(exception, "Failed to subscribe to topic: $TOPIC_GENERAL")
            }
        
        // Subscribe to matches topic
        FirebaseMessaging.getInstance().subscribeToTopic(TOPIC_MATCHES)
            .addOnSuccessListener {
                Timber.d("Successfully subscribed to topic: $TOPIC_MATCHES")
            }
            .addOnFailureListener { exception ->
                Timber.e(exception, "Failed to subscribe to topic: $TOPIC_MATCHES")
            }
        
        // Subscribe to transfer requests topic
        FirebaseMessaging.getInstance().subscribeToTopic(TOPIC_TRANSFER_REQUESTS)
            .addOnSuccessListener {
                Timber.d("Successfully subscribed to topic: $TOPIC_TRANSFER_REQUESTS")
            }
            .addOnFailureListener { exception ->
                Timber.e(exception, "Failed to subscribe to topic: $TOPIC_TRANSFER_REQUESTS")
            }
        
        // Subscribe to updates topic
        FirebaseMessaging.getInstance().subscribeToTopic(TOPIC_UPDATES)
            .addOnSuccessListener {
                Timber.d("Successfully subscribed to topic: $TOPIC_UPDATES")
            }
            .addOnFailureListener { exception ->
                Timber.e(exception, "Failed to subscribe to topic: $TOPIC_UPDATES")
            }
    }
    
    /**
     * Subscribe to a specific topic
     */
    fun subscribeToTopic(topic: String) {
        FirebaseMessaging.getInstance().subscribeToTopic(topic)
            .addOnSuccessListener {
                Timber.d("Successfully subscribed to topic: $topic")
                // Save subscription to Firestore
                saveTopicSubscription(topic, true)
            }
            .addOnFailureListener { exception ->
                Timber.e(exception, "Failed to subscribe to topic: $topic")
            }
    }
    
    /**
     * Unsubscribe from a specific topic
     */
    fun unsubscribeFromTopic(topic: String) {
        FirebaseMessaging.getInstance().unsubscribeFromTopic(topic)
            .addOnSuccessListener {
                Timber.d("Successfully unsubscribed from topic: $topic")
                // Save subscription to Firestore
                saveTopicSubscription(topic, false)
            }
            .addOnFailureListener { exception ->
                Timber.e(exception, "Failed to unsubscribe from topic: $topic")
            }
    }
    
    /**
     * Save topic subscription status to Firestore
     */
    private fun saveTopicSubscription(topic: String, isSubscribed: Boolean) {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            val subscriptionData = hashMapOf(
                "topic" to topic,
                "isSubscribed" to isSubscribed,
                "timestamp" to System.currentTimeMillis()
            )
            
            firestore.collection("users")
                .document(userId)
                .collection("topic_subscriptions")
                .document(topic)
                .set(subscriptionData)
                .addOnSuccessListener {
                    Timber.d("Topic subscription saved to Firestore: $topic = $isSubscribed")
                }
                .addOnFailureListener { exception ->
                    Timber.e(exception, "Failed to save topic subscription to Firestore")
                }
        }
    }
    
    /**
     * Get all subscribed topics for the current user
     */
    fun getSubscribedTopics(callback: (List<String>) -> Unit) {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            firestore.collection("users")
                .document(userId)
                .collection("topic_subscriptions")
                .whereEqualTo("isSubscribed", true)
                .get()
                .addOnSuccessListener { documents ->
                    val topics = documents.mapNotNull { doc ->
                        doc.getString("topic")
                    }
                    callback(topics)
                }
                .addOnFailureListener { exception ->
                    Timber.e(exception, "Failed to get subscribed topics")
                    callback(emptyList())
                }
        } else {
            callback(emptyList())
        }
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
        
        val message = remoteMessage.data["body"] 
            ?: remoteMessage.notification?.body 
            ?: ""
        
        val type = remoteMessage.data["messageType"] ?: "general"
        
        // Check for app update notification
        val isAppUpdate = remoteMessage.data["isAppUpdate"]?.toBoolean() ?: false
        val isForceUpdate = remoteMessage.data["isForceUpdate"]?.toBoolean() ?: false
        val newVersion = remoteMessage.data["version"] ?: ""
        
        Timber.d("Processing notification - Title: $title, Message: $message, Type: $type")
        Timber.d("App update: $isAppUpdate, Force update: $isForceUpdate, New version: $newVersion")
        Timber.d("App state: ${if (isAppInForeground()) "FOREGROUND" else "BACKGROUND"}")
        
        // Handle app update notification
        if (isAppUpdate && newVersion.isNotEmpty()) {
            handleAppUpdateNotification(isForceUpdate, newVersion, title, message, remoteMessage.data)
            return
        }
        
        // Always show system notification (works for both foreground and background)
        showNotification(title, message, type, remoteMessage.data)
        
        // Save notification to Firestore (works for both foreground and background)
        // Use a background thread to avoid blocking the UI
        Thread {
            try {
                Timber.d("Saving notification in background thread")
                saveNotificationToFirestore(title, message, type, remoteMessage.data)
            } catch (e: Exception) {
                Timber.e(e, "Error saving notification in background thread")
            }
        }.start()
    }
    
    private fun handleAppUpdateNotification(
        isForceUpdate: Boolean,
        newVersion: String,
        title: String,
        message: String,
        data: Map<String, String>
    ) {
        Timber.d("=== HANDLING APP UPDATE NOTIFICATION ===")
        Timber.d("Force update: $isForceUpdate")
        Timber.d("New version: $newVersion")
        
        val currentVersion = AppUpdateUtils.getCurrentVersion(this)
        Timber.d("Current version: $currentVersion")
        
        // Check if update is needed
        if (!AppUpdateUtils.isUpdateNeeded(currentVersion, newVersion)) {
            Timber.d("Update not needed - current version is up to date")
            return
        }
        
        // Check if force update is required
        val forceUpdateVersion = data["forceUpdateVersion"] ?: newVersion
        val shouldForceUpdate = isForceUpdate && AppUpdateUtils.isForceUpdateRequired(
            currentVersion, newVersion, forceUpdateVersion
        )
        
        Timber.d("Should force update: $shouldForceUpdate")
        
        // Show update dialog
        showAppUpdateDialog(shouldForceUpdate, currentVersion, newVersion, message)
        
        // Also save as regular notification for Updates screen
        Thread {
            try {
                Timber.d("Saving app update notification to Firestore")
                saveNotificationToFirestore(title, message, "app_update", data)
            } catch (e: Exception) {
                Timber.e(e, "Error saving app update notification")
            }
        }.start()
    }
    
    private fun showAppUpdateDialog(
        isForceUpdate: Boolean,
        currentVersion: String,
        newVersion: String,
        updateMessage: String
    ) {
        // This will be handled by the MainActivity or UI layer
        // For now, we'll save the update info to SharedPreferences
        val sharedPrefs = getSharedPreferences("app_update_prefs", Context.MODE_PRIVATE)
        sharedPrefs.edit().apply {
            putBoolean("show_update_dialog", true)
            putBoolean("is_force_update", isForceUpdate)
            putString("current_version", currentVersion)
            putString("new_version", newVersion)
            putString("update_message", updateMessage)
            putLong("update_timestamp", System.currentTimeMillis())
        }.apply()
        
        Timber.d("App update dialog info saved to SharedPreferences")
        
        // Send broadcast to notify MainActivity
        val intent = Intent("APP_UPDATE_AVAILABLE")
        intent.putExtra("is_force_update", isForceUpdate)
        intent.putExtra("current_version", currentVersion)
        intent.putExtra("new_version", newVersion)
        intent.putExtra("update_message", updateMessage)
        sendBroadcast(intent)
    }
    
    private fun isAppInForeground(): Boolean {
        val activityManager = getSystemService(Context.ACTIVITY_SERVICE) as android.app.ActivityManager
        val appProcesses = activityManager.runningAppProcesses ?: return false
        
        val packageName = packageName
        for (appProcess in appProcesses) {
            if (appProcess.importance == android.app.ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND 
                && appProcess.processName == packageName) {
                return true
            }
        }
        return false
    }
    
    fun saveNotificationToFirestore(
        title: String,
        message: String,
        type: String,
        data: Map<String, String>
    ) {
        NotificationStore.save(title, message, type, data)
    }

    private fun saveNotificationForUser(
        title: String,
        message: String,
        type: String,
        data: Map<String, String>,
        userId: String
    ) {
        // Extract additional fields from data
        val imageUri = data["imageUrl"] ?: data["imageUri"] ?: data["image_uri"]
        val linkUrl = data["linkUrl"] ?: data["link_url"]
        val fullContent = data["fullContent"] ?: data["full_content"]
        val timestamp = data["timestamp"] ?: System.currentTimeMillis().toString()
        
        // Check for duplicate notification before saving
        checkForDuplicateAndSave(title, message, timestamp, type, data, userId, imageUri, linkUrl, fullContent)
    }
    
    private fun checkForDuplicateAndSave(
        title: String,
        message: String,
        timestamp: String,
        type: String,
        data: Map<String, String>,
        userId: String,
        imageUri: String?,
        linkUrl: String?,
        fullContent: String?
    ) {
        // Create a time window for duplicate detection (5 minutes)
        val timeWindow = 5 * 60 * 1000L // 5 minutes in milliseconds
        val timestampLong = timestamp.toLongOrNull() ?: System.currentTimeMillis()
        val startTime = (timestampLong - timeWindow).toString()
        val endTime = (timestampLong + timeWindow).toString()
        
        // Use a simpler query to avoid composite index requirement
        firestore.collection("users")
            .document(userId)
            .collection("notifications")
            .whereEqualTo("title", title)
            .whereEqualTo("message", message)
            .get()
            .addOnSuccessListener { documents ->
                // Check if any recent notification matches (within time window)
                val hasDuplicate = documents.any { document ->
                    val docTimestamp = document.getString("timestamp")?.toLongOrNull() ?: 0L
                    docTimestamp >= timestampLong - timeWindow && docTimestamp <= timestampLong + timeWindow
                }
                
                if (!hasDuplicate) {
                    // No duplicate found, save the notification
                    Timber.d("No duplicate found, saving notification")
                    saveNotificationToFirestore(title, message, timestamp, type, data, userId, imageUri, linkUrl, fullContent)
                } else {
                    Timber.d("Duplicate notification found, skipping save")
                    Timber.d("Existing notification ID: ${documents.documents.firstOrNull()?.id}")
                }
            }
            .addOnFailureListener { exception ->
                Timber.e(exception, "Error checking for duplicate notification")
                // If we can't check for duplicates, save anyway to ensure notification is not lost
                saveNotificationToFirestore(title, message, timestamp, type, data, userId, imageUri, linkUrl, fullContent)
            }
    }
    
    private fun saveNotificationToFirestore(
        title: String,
        message: String,
        timestamp: String,
        type: String,
        data: Map<String, String>,
        userId: String,
        imageUri: String?,
        linkUrl: String?,
        fullContent: String?
    ) {
        val notification = hashMapOf<String, Any?>(
            "title" to title,
            "message" to message,
            "timestamp" to timestamp,
            "isRead" to false,
            "type" to type,
            "data" to data,
            "imageUri" to imageUri,
            "linkUrl" to linkUrl,
            "fullContent" to fullContent
        )
        
        Timber.d("Saving notification to Firestore for user: $userId")
        
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
    
    private fun showNotification(title: String, message: String, type: String, data: Map<String, String> = emptyMap()) {
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
        
        // Create intent for notification tap with all notification data
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("notification_type", type)
            putExtra("title", title)
            putExtra("body", message) // Use "body" to match payload structure
            putExtra("message", message) // Keep for backward compatibility
            putExtra("timestamp", data["timestamp"] ?: System.currentTimeMillis().toString())
            
            // Add all data fields to ensure complete information is passed
            data.forEach { (key, value) ->
                putExtra(key, value)
            }
            
            // Add additional fields if available
            data["imageUrl"]?.let { putExtra("imageUrl", it) }
            data["imageUri"]?.let { putExtra("imageUri", it) } // Keep for backward compatibility
            data["linkUrl"]?.let { putExtra("linkUrl", it) }
            data["fullContent"]?.let { putExtra("fullContent", it) }
            
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
                            .setSmallIcon(R.drawable.ic_notification)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()
        
        // Show notification with unique ID
        val notificationId = System.currentTimeMillis().toInt()
        notificationManager.notify(notificationId, notification)
        Timber.d("System notification shown with ID: $notificationId")
        Timber.d("Notification data included in intent: title=$title, message=$message, type=$type, data=$data")
    }
} 
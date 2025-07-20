package com.shikshak.transfer.ui.theme.updates

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class UpdatesViewModel @Inject constructor() : ViewModel() {
    
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val messaging = FirebaseMessaging.getInstance()
    
    private val _notifications = MutableStateFlow<List<NotificationItem>>(emptyList())
    val notifications: StateFlow<List<NotificationItem>> = _notifications
    
    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading
    
    init {
        setupRealtimeListener()
    }
    
    private fun setupRealtimeListener() {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            Timber.d("Setting up real-time listener for user: $userId")
            
            // Listen to user's notifications collection
            firestore.collection("users")
                .document(userId)
                .collection("notifications")
                .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .limit(50)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Timber.e(error, "Error in real-time listener")
                        return@addSnapshotListener
                    }
                    
                    if (snapshot != null) {
                        val notificationList = mutableListOf<NotificationItem>()
                        Timber.d("Real-time update: Found ${snapshot.size()} notification documents")
                        Timber.d("User ID being queried: $userId")
                        
                        for (document in snapshot) {
                            Timber.d("Real-time processing document: ${document.id}")
                            Timber.d("Document data: ${document.data}")
                            
                            val notification = NotificationItem.fromDocument(document)
                            if (notification != null) {
                                notificationList.add(notification)
                                Timber.d("Real-time loaded notification: ${notification.title}, isRead: ${notification.isRead}, ID: ${notification.id}")
                            } else {
                                Timber.w("Real-time failed to parse notification document: ${document.id}")
                                Timber.w("Document data that failed to parse: ${document.data}")
                            }
                        }
                        
                        _notifications.value = notificationList
                        _isLoading.value = false
                        Timber.d("Real-time successfully loaded ${notificationList.size} notifications")
                    }
                }
                
            // Also listen to general notifications collection for this user
            firestore.collection("notifications")
                .whereEqualTo("userId", userId)
                .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .limit(50)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Timber.e(error, "Error in general notifications real-time listener")
                        return@addSnapshotListener
                    }
                    
                    if (snapshot != null) {
                        Timber.d("General notifications real-time update: Found ${snapshot.size()} documents")
                        
                        val generalNotifications = mutableListOf<NotificationItem>()
                        for (document in snapshot) {
                            val data = document.data
                            val notification = NotificationItem(
                                id = document.id,
                                title = data["title"] as? String ?: "",
                                message = data["message"] as? String ?: "",
                                timestamp = ((data["timestamp"]) ?: System.currentTimeMillis().toString()).toString(),
                                isRead = (data["isRead"] as? Boolean) ?: false,
                                type = data["type"] as? String ?: "general",
                                data = (data["data"] as? Map<String, String>) ?: emptyMap(),
                                imageUri = data["imageUri"] as? String,
                                linkUrl = data["linkUrl"] as? String,
                                fullContent = data["fullContent"] as? String
                            )
                            generalNotifications.add(notification)
                            Timber.d("General notification loaded: ${notification.title}")
                        }
                        
                        // Merge with existing notifications
                        val currentNotifications = _notifications.value.toMutableList()
                        currentNotifications.addAll(generalNotifications)
                        _notifications.value = currentNotifications.distinctBy { it.id }
                    }
                }
        } else {
            Timber.w("Cannot setup real-time listener - no user ID available")
            Timber.w("Current user: ${auth.currentUser}")
            _isLoading.value = false
        }
    }
    
    fun onNotificationPermissionGranted() {
        // Initialize FCM when permission is granted
        messaging.token.addOnSuccessListener { token ->
            Timber.d("FCM Token obtained after permission grant: $token")
            saveFCMToken(token)
        }.addOnFailureListener { exception ->
            Timber.e(exception, "Failed to get FCM token after permission grant")
        }
    }
    
    fun loadNotifications() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val userId = auth.currentUser?.uid
                
                if (userId != null) {
                    Timber.d("Loading notifications for user: $userId")
                    
                    firestore.collection("users")
                        .document(userId)
                        .collection("notifications")
                        .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
                        .limit(50)
                        .get()
                        .addOnSuccessListener { documents ->
                            val notificationList = mutableListOf<NotificationItem>()
                            
                            Timber.d("Found ${documents.size()} notification documents")
                            Timber.d("User ID being queried: $userId")
                            
                            for (document in documents) {
                                Timber.d("Processing document: ${document.id}")
                                Timber.d("Document data: ${document.data}")
                                
                                val notification = NotificationItem.fromDocument(document)
                                if (notification != null) {
                                    notificationList.add(notification)
                                    Timber.d("Loaded notification: ${notification.title}, isRead: ${notification.isRead}, ID: ${notification.id}")
                                } else {
                                    Timber.w("Failed to parse notification document: ${document.id}")
                                    Timber.w("Document data that failed to parse: ${document.data}")
                                }
                            }
                            
                            _notifications.value = notificationList
                            _isLoading.value = false
                            Timber.d("Successfully loaded ${notificationList.size} notifications")
                        }
                        .addOnFailureListener { exception ->
                            Timber.e(exception, "Error loading notifications")
                            _isLoading.value = false
                        }
                } else {
                    Timber.w("No user ID available for loading notifications")
                    Timber.w("Current user: ${auth.currentUser}")
                    _isLoading.value = false
                }
            } catch (e: Exception) {
                Timber.e(e, "Error in loadNotifications")
                _isLoading.value = false
            }
        }
    }
    
    fun markAsRead(notificationId: String) {
        viewModelScope.launch {
            try {
                val userId = auth.currentUser?.uid
                if (userId != null) {
                    Timber.d("Marking notification as read: $notificationId")
                    
                    firestore.collection("users")
                        .document(userId)
                        .collection("notifications")
                        .document(notificationId)
                        .update("isRead", true)
                        .addOnSuccessListener {
                            // Update local state immediately
                            val updatedNotifications = _notifications.value.map { notification ->
                                if (notification.id == notificationId) {
                                    notification.copy(isRead = true)
                                } else {
                                    notification
                                }
                            }
                            _notifications.value = updatedNotifications
                            Timber.d("Successfully marked notification as read: $notificationId")
                        }
                        .addOnFailureListener { exception ->
                            Timber.e(exception, "Error marking notification as read: $notificationId")
                            // Even if Firestore update fails, update local state for better UX
                            val updatedNotifications = _notifications.value.map { notification ->
                                if (notification.id == notificationId) {
                                    notification.copy(isRead = true)
                                } else {
                                    notification
                                }
                            }
                            _notifications.value = updatedNotifications
                        }
                } else {
                    Timber.w("Cannot mark notification as read - no user ID available")
                }
            } catch (e: Exception) {
                Timber.e(e, "Error in markAsRead")
            }
        }
    }
    
    private fun saveFCMToken(token: String) {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            firestore.collection("users")
                .document(userId)
                .update("fcmToken", token)
                .addOnSuccessListener {
                    Timber.d("FCM token saved successfully")
                }
                .addOnFailureListener { exception ->
                    Timber.e(exception, "Error saving FCM token")
                }
        }
    }
    
    fun refreshNotifications() {
        Timber.d("Refreshing notifications")
        loadNotifications()
    }
    
    fun clearAllNotifications() {
        viewModelScope.launch {
            try {
                val userId = auth.currentUser?.uid
                if (userId != null) {
                    firestore.collection("users")
                        .document(userId)
                        .collection("notifications")
                        .get()
                        .addOnSuccessListener { documents ->
                            val batch = firestore.batch()
                            for (document in documents) {
                                batch.delete(document.reference)
                            }
                            batch.commit()
                                .addOnSuccessListener {
                                    _notifications.value = emptyList()
                                    Timber.d("All notifications cleared")
                                }
                                .addOnFailureListener { exception ->
                                    Timber.e(exception, "Error clearing notifications")
                                }
                        }
                        .addOnFailureListener { exception ->
                            Timber.e(exception, "Error getting notifications for deletion")
                        }
                }
            } catch (e: Exception) {
                Timber.e(e, "Error in clearAllNotifications")
            }
        }
    }
} 
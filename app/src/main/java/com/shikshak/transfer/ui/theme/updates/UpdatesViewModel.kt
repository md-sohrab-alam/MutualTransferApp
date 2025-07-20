package com.shikshak.transfer.ui.theme.updates

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class UpdatesViewModel @Inject constructor() : ViewModel() {
    
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val messaging = FirebaseMessaging.getInstance()
    
    // Pagination state
    private val _notifications = MutableStateFlow<List<NotificationItem>>(emptyList())
    val notifications: StateFlow<List<NotificationItem>> = _notifications.asStateFlow()
    
    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _isLoadingMore = MutableStateFlow(false)
    val isLoadingMore: StateFlow<Boolean> = _isLoadingMore.asStateFlow()
    
    private val _hasMoreData = MutableStateFlow(true)
    val hasMoreData: StateFlow<Boolean> = _hasMoreData.asStateFlow()
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    // Pagination parameters
    private val pageSize = 20
    private var lastDocument: com.google.firebase.firestore.DocumentSnapshot? = null
    private var isInitialLoad = true
    
    // Cache for notifications
    private val notificationCache = mutableMapOf<String, NotificationItem>()
    
    init {
        setupRealtimeListener()
    }
    
    private fun setupRealtimeListener() {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            Timber.d("Setting up real-time listener for user: $userId")
            
            // Listen to user's notifications collection with pagination
            val query = firestore.collection("users")
                .document(userId)
                .collection("notifications")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(pageSize.toLong())
            
            query.addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Timber.e(error, "Error in real-time listener")
                    _error.value = "Failed to load notifications"
                    return@addSnapshotListener
                }
                
                if (snapshot != null) {
                    val notificationList = mutableListOf<NotificationItem>()
                    Timber.d("Real-time update: Found ${snapshot.size()} notification documents")
                    
                    for (document in snapshot) {
                        val notification = NotificationItem.fromDocument(document)
                        if (notification != null) {
                            notificationList.add(notification)
                            notificationCache[notification.id] = notification
                            Timber.d("Real-time loaded notification: ${notification.title}")
                        } else {
                            Timber.w("Real-time failed to parse notification document: ${document.id}")
                        }
                    }
                    
                    _notifications.value = notificationList
                    _isLoading.value = false
                    _error.value = null
                    
                    // Update pagination state
                    if (snapshot.size() < pageSize) {
                        _hasMoreData.value = false
                    }
                    
                    if (!snapshot.isEmpty) {
                        lastDocument = snapshot.documents.last()
                    }
                    
                    Timber.d("Real-time successfully loaded ${notificationList.size} notifications")
                }
            }
        } else {
            Timber.w("Cannot setup real-time listener - no user ID available")
            _isLoading.value = false
            _error.value = "User not authenticated"
        }
    }
    
    fun loadMoreNotifications() {
        if (_isLoadingMore.value || !_hasMoreData.value) return
        
        viewModelScope.launch {
            try {
                _isLoadingMore.value = true
                val userId = auth.currentUser?.uid
                
                if (userId != null && lastDocument != null) {
                    Timber.d("Loading more notifications for user: $userId")
                    
                    val query = firestore.collection("users")
                        .document(userId)
                        .collection("notifications")
                        .orderBy("timestamp", Query.Direction.DESCENDING)
                        .startAfter(lastDocument)
                        .limit(pageSize.toLong())
                    
                    query.get().addOnSuccessListener { documents ->
                        val newNotifications = mutableListOf<NotificationItem>()
                        
                        for (document in documents) {
                            val notification = NotificationItem.fromDocument(document)
                            if (notification != null) {
                                newNotifications.add(notification)
                                notificationCache[notification.id] = notification
                            }
                        }
                        
                        // Append to existing notifications
                        val currentNotifications = _notifications.value.toMutableList()
                        currentNotifications.addAll(newNotifications)
                        _notifications.value = currentNotifications
                        
                        // Update pagination state
                        if (documents.size() < pageSize) {
                            _hasMoreData.value = false
                        }
                        
                        if (!documents.isEmpty) {
                            lastDocument = documents.documents.last()
                        }
                        
                        _isLoadingMore.value = false
                        Timber.d("Successfully loaded ${newNotifications.size} more notifications")
                    }.addOnFailureListener { exception ->
                        Timber.e(exception, "Error loading more notifications")
                        _isLoadingMore.value = false
                        _error.value = "Failed to load more notifications"
                    }
                } else {
                    _isLoadingMore.value = false
                    _hasMoreData.value = false
                }
            } catch (e: Exception) {
                Timber.e(e, "Error in loadMoreNotifications")
                _isLoadingMore.value = false
                _error.value = "Failed to load more notifications"
            }
        }
    }
    
    fun refreshNotifications() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                lastDocument = null
                _hasMoreData.value = true
                notificationCache.clear()
                
                val userId = auth.currentUser?.uid
                
                if (userId != null) {
                    Timber.d("Refreshing notifications for user: $userId")
                    
                    val query = firestore.collection("users")
                        .document(userId)
                        .collection("notifications")
                        .orderBy("timestamp", Query.Direction.DESCENDING)
                        .limit(pageSize.toLong())
                    
                    query.get().addOnSuccessListener { documents ->
                        val notificationList = mutableListOf<NotificationItem>()
                        
                        for (document in documents) {
                            val notification = NotificationItem.fromDocument(document)
                            if (notification != null) {
                                notificationList.add(notification)
                                notificationCache[notification.id] = notification
                            }
                        }
                        
                        _notifications.value = notificationList
                        _isLoading.value = false
                        
                        // Update pagination state
                        if (documents.size() < pageSize) {
                            _hasMoreData.value = false
                        }
                        
                        if (!documents.isEmpty) {
                            lastDocument = documents.documents.last()
                        }
                        
                        Timber.d("Successfully refreshed ${notificationList.size} notifications")
                    }.addOnFailureListener { exception ->
                        Timber.e(exception, "Error refreshing notifications")
                        _isLoading.value = false
                        _error.value = "Failed to refresh notifications"
                    }
                } else {
                    _isLoading.value = false
                    _error.value = "User not authenticated"
                }
            } catch (e: Exception) {
                Timber.e(e, "Error in refreshNotifications")
                _isLoading.value = false
                _error.value = "Failed to refresh notifications"
            }
        }
    }
    
    fun onNotificationPermissionGranted() {
        viewModelScope.launch {
            try {
                // Initialize FCM when permission is granted
                messaging.token.addOnSuccessListener { token ->
                    Timber.d("FCM Token obtained after permission grant: $token")
                    saveFCMToken(token)
                }.addOnFailureListener { exception ->
                    Timber.e(exception, "Failed to get FCM token after permission grant")
                }
            } catch (e: Exception) {
                Timber.e(e, "Error in onNotificationPermissionGranted")
            }
        }
    }
    
    fun markAsRead(notificationId: String) {
        viewModelScope.launch {
            try {
                val userId = auth.currentUser?.uid
                if (userId != null) {
                    Timber.d("Marking notification as read: $notificationId")
                    
                    // Update local state immediately for better UX
                    val updatedNotifications = _notifications.value.map { notification ->
                        if (notification.id == notificationId) {
                            notification.copy(isRead = true)
                        } else {
                            notification
                        }
                    }
                    _notifications.value = updatedNotifications
                    
                    // Update cache
                    notificationCache[notificationId]?.let { cached ->
                        notificationCache[notificationId] = cached.copy(isRead = true)
                    }
                    
                    // Update in Firestore
                    firestore.collection("users")
                        .document(userId)
                        .collection("notifications")
                        .document(notificationId)
                        .update("isRead", true)
                        .addOnSuccessListener {
                            Timber.d("Successfully marked notification as read: $notificationId")
                        }
                        .addOnFailureListener { exception ->
                            Timber.e(exception, "Failed to mark notification as read: $notificationId")
                            // Revert local state on failure
                            val revertedNotifications = _notifications.value.map { notification ->
                                if (notification.id == notificationId) {
                                    notification.copy(isRead = false)
                                } else {
                                    notification
                                }
                            }
                            _notifications.value = revertedNotifications
                        }
                }
            } catch (e: Exception) {
                Timber.e(e, "Error in markAsRead")
            }
        }
    }
    
    fun markAllAsRead() {
        viewModelScope.launch {
            try {
                val userId = auth.currentUser?.uid
                if (userId != null) {
                    Timber.d("Marking all notifications as read for user: $userId")
                    
                    // Update local state immediately
                    val updatedNotifications = _notifications.value.map { notification ->
                        notification.copy(isRead = true)
                    }
                    _notifications.value = updatedNotifications
                    
                    // Update cache
                    notificationCache.values.forEach { notification ->
                        notificationCache[notification.id] = notification.copy(isRead = true)
                    }
                    
                    // Batch update in Firestore
                    val batch = firestore.batch()
                    _notifications.value.forEach { notification ->
                        if (!notification.isRead) {
                            val docRef = firestore.collection("users")
                                .document(userId)
                                .collection("notifications")
                                .document(notification.id)
                            batch.update(docRef, "isRead", true)
                        }
                    }
                    
                    batch.commit()
                        .addOnSuccessListener {
                            Timber.d("Successfully marked all notifications as read")
                        }
                        .addOnFailureListener { exception ->
                            Timber.e(exception, "Failed to mark all notifications as read")
                        }
                }
            } catch (e: Exception) {
                Timber.e(e, "Error in markAllAsRead")
            }
        }
    }
    
    private fun saveFCMToken(token: String) {
        viewModelScope.launch {
            try {
                val userId = auth.currentUser?.uid
                if (userId != null) {
                    firestore.collection("users")
                        .document(userId)
                        .update("fcmToken", token)
                        .addOnSuccessListener {
                            Timber.d("FCM token saved to Firestore successfully")
                        }
                        .addOnFailureListener { exception ->
                            Timber.e(exception, "Failed to save FCM token to Firestore")
                        }
                }
            } catch (e: Exception) {
                Timber.e(e, "Error in saveFCMToken")
            }
        }
    }
    
    fun clearError() {
        _error.value = null
    }
    
    fun getCachedNotification(id: String): NotificationItem? {
        return notificationCache[id]
    }
    
    override fun onCleared() {
        super.onCleared()
        notificationCache.clear()
    }
} 
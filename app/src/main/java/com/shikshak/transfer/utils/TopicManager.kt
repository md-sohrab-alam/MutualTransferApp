package com.shikshak.transfer.utils

import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import timber.log.Timber

object TopicManager {
    
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    
    // Topic constants
    const val TOPIC_GENERAL = "general"
    const val TOPIC_MATCHES = "matches"
    const val TOPIC_TRANSFER_REQUESTS = "transfer_requests"
    const val TOPIC_UPDATES = "updates"
    const val TOPIC_ANNOUNCEMENTS = "announcements"
    const val TOPIC_SYSTEM = "system"
    
    /**
     * Subscribe to a topic
     */
    fun subscribeToTopic(topic: String, onSuccess: () -> Unit = {}, onFailure: (Exception) -> Unit = {}) {
        FirebaseMessaging.getInstance().subscribeToTopic(topic)
            .addOnSuccessListener {
                Timber.d("Successfully subscribed to topic: $topic")
                saveTopicSubscription(topic, true)
                onSuccess()
            }
            .addOnFailureListener { exception ->
                Timber.e(exception, "Failed to subscribe to topic: $topic")
                onFailure(exception)
            }
    }
    
    /**
     * Unsubscribe from a topic
     */
    fun unsubscribeFromTopic(topic: String, onSuccess: () -> Unit = {}, onFailure: (Exception) -> Unit = {}) {
        FirebaseMessaging.getInstance().unsubscribeFromTopic(topic)
            .addOnSuccessListener {
                Timber.d("Successfully unsubscribed from topic: $topic")
                saveTopicSubscription(topic, false)
                onSuccess()
            }
            .addOnFailureListener { exception ->
                Timber.e(exception, "Failed to unsubscribe from topic: $topic")
                onFailure(exception)
            }
    }
    
    /**
     * Subscribe to multiple topics
     */
    fun subscribeToTopics(topics: List<String>, onComplete: (successCount: Int, failureCount: Int) -> Unit = { _, _ -> }) {
        var successCount = 0
        var failureCount = 0
        var completedCount = 0
        
        topics.forEach { topic ->
            subscribeToTopic(
                topic = topic,
                onSuccess = {
                    successCount++
                    completedCount++
                    if (completedCount == topics.size) {
                        onComplete(successCount, failureCount)
                    }
                },
                onFailure = { exception ->
                    failureCount++
                    completedCount++
                    if (completedCount == topics.size) {
                        onComplete(successCount, failureCount)
                    }
                }
            )
        }
    }
    
    /**
     * Unsubscribe from multiple topics
     */
    fun unsubscribeFromTopics(topics: List<String>, onComplete: (successCount: Int, failureCount: Int) -> Unit = { _, _ -> }) {
        var successCount = 0
        var failureCount = 0
        var completedCount = 0
        
        topics.forEach { topic ->
            unsubscribeFromTopic(
                topic = topic,
                onSuccess = {
                    successCount++
                    completedCount++
                    if (completedCount == topics.size) {
                        onComplete(successCount, failureCount)
                    }
                },
                onFailure = { exception ->
                    failureCount++
                    completedCount++
                    if (completedCount == topics.size) {
                        onComplete(successCount, failureCount)
                    }
                }
            )
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
    
    /**
     * Check if user is subscribed to a specific topic
     */
    fun isSubscribedToTopic(topic: String, callback: (Boolean) -> Unit) {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            firestore.collection("users")
                .document(userId)
                .collection("topic_subscriptions")
                .document(topic)
                .get()
                .addOnSuccessListener { document ->
                    val isSubscribed = document.exists() && (document.getBoolean("isSubscribed") ?: false)
                    callback(isSubscribed)
                }
                .addOnFailureListener { exception ->
                    Timber.e(exception, "Failed to check topic subscription")
                    callback(false)
                }
        } else {
            callback(false)
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
     * Subscribe to default topics for new users
     */
    fun subscribeToDefaultTopics() {
        val defaultTopics = listOf(
            TOPIC_GENERAL,
            TOPIC_UPDATES,
            TOPIC_SYSTEM
        )
        
        subscribeToTopics(defaultTopics) { successCount, failureCount ->
            Timber.d("Default topic subscription completed: $successCount success, $failureCount failures")
        }
    }
    
    /**
     * Subscribe to topic based on user preferences
     */
    fun subscribeToUserPreferenceTopics(preferences: Map<String, Boolean>) {
        preferences.forEach { (topic, isEnabled) ->
            if (isEnabled) {
                subscribeToTopic(topic)
            } else {
                unsubscribeFromTopic(topic)
            }
        }
    }
} 
package com.shikshak.transfer.utils

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import timber.log.Timber

object FCMTokenManager {
    
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val messaging = FirebaseMessaging.getInstance()
    
    fun registerForNotifications() {
        messaging.token.addOnSuccessListener { token ->
            Timber.d("FCM Token: $token")
            saveTokenToFirestore(token)
        }.addOnFailureListener { exception ->
            Timber.e(exception, "Failed to get FCM token")
        }
    }
    
    fun unregisterFromNotifications() {
        messaging.deleteToken().addOnSuccessListener {
            Timber.d("FCM token deleted successfully")
            removeTokenFromFirestore()
        }.addOnFailureListener { exception ->
            Timber.e(exception, "Failed to delete FCM token")
        }
    }
    
    private fun saveTokenToFirestore(token: String) {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            firestore.collection("users")
                .document(userId)
                .update("fcmToken", token)
                .addOnSuccessListener {
                    Timber.d("FCM token saved to Firestore")
                }
                .addOnFailureListener { exception ->
                    Timber.e(exception, "Error saving FCM token to Firestore")
                }
        }
    }
    
    private fun removeTokenFromFirestore() {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            firestore.collection("users")
                .document(userId)
                .update("fcmToken", null)
                .addOnSuccessListener {
                    Timber.d("FCM token removed from Firestore")
                }
                .addOnFailureListener { exception ->
                    Timber.e(exception, "Error removing FCM token from Firestore")
                }
        }
    }
    
    fun subscribeToTopic(topic: String) {
        messaging.subscribeToTopic(topic)
            .addOnSuccessListener {
                Timber.d("Subscribed to topic: $topic")
            }
            .addOnFailureListener { exception ->
                Timber.e(exception, "Failed to subscribe to topic: $topic")
            }
    }
    
    fun unsubscribeFromTopic(topic: String) {
        messaging.unsubscribeFromTopic(topic)
            .addOnSuccessListener {
                Timber.d("Unsubscribed from topic: $topic")
            }
            .addOnFailureListener { exception ->
                Timber.e(exception, "Failed to unsubscribe from topic: $topic")
            }
    }
} 
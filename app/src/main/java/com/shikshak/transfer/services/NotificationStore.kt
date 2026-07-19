package com.shikshak.transfer.services

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import timber.log.Timber

/**
 * Firestore notification writer that does not depend on the FCM Service lifecycle.
 * Safe to call from Activity / BroadcastReceiver / companion helpers.
 */
object NotificationStore {

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    fun save(
        title: String,
        message: String,
        type: String,
        data: Map<String, String>
    ) {
        val userId = auth.currentUser?.uid
            ?: data["userId"]
            ?: data["user_id"]

        val userIds = data["userIds"] ?: data["user_ids"]

        when {
            !userId.isNullOrBlank() && userId != "null" -> saveForUser(title, message, type, data, userId)
            !userIds.isNullOrBlank() -> {
                userIds.split(",").forEach { uid ->
                    val trimmed = uid.trim()
                    if (trimmed.isNotEmpty() && trimmed != "null") {
                        saveForUser(title, message, type, data, trimmed)
                    }
                }
            }
            else -> {
                Timber.w("Cannot save notification - no valid user ID")
                saveToGeneral(title, message, type, data, "unknown")
            }
        }
    }

    private fun saveForUser(
        title: String,
        message: String,
        type: String,
        data: Map<String, String>,
        userId: String
    ) {
        val imageUri = data["imageUrl"] ?: data["imageUri"] ?: data["image_uri"]
        val linkUrl = data["linkUrl"] ?: data["link_url"]
        val fullContent = data["fullContent"] ?: data["full_content"]
        val timestamp = data["timestamp"] ?: System.currentTimeMillis().toString()

        val payload = hashMapOf<String, Any?>(
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

        firestore.collection("users")
            .document(userId)
            .set(hashMapOf("lastUpdated" to System.currentTimeMillis()), SetOptions.merge())
            .addOnSuccessListener {
                firestore.collection("users")
                    .document(userId)
                    .collection("notifications")
                    .add(payload)
                    .addOnSuccessListener { ref ->
                        Timber.d("Notification saved: ${ref.id}")
                    }
                    .addOnFailureListener { e ->
                        Timber.e(e, "Failed saving user notification")
                        saveToGeneral(title, message, type, data, userId)
                    }
            }
            .addOnFailureListener { e ->
                Timber.e(e, "Failed ensuring user doc for notification")
                firestore.collection("users")
                    .document(userId)
                    .collection("notifications")
                    .add(payload)
                    .addOnFailureListener { inner ->
                        Timber.e(inner, "Failed saving notification after user-doc error")
                        saveToGeneral(title, message, type, data, userId)
                    }
            }
    }

    private fun saveToGeneral(
        title: String,
        message: String,
        type: String,
        data: Map<String, String>,
        userId: String
    ) {
        val payload = hashMapOf(
            "title" to title,
            "message" to message,
            "type" to type,
            "timestamp" to System.currentTimeMillis().toString(),
            "isRead" to false,
            "userId" to userId,
            "data" to data
        )
        firestore.collection("notifications")
            .add(payload)
            .addOnFailureListener { e ->
                Timber.e(e, "Failed saving to general notifications")
            }
    }
}

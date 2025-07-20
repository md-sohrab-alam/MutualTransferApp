package com.shikshak.transfer.ui.theme.updates

import com.google.firebase.firestore.PropertyName
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestoreException

data class NotificationItem(
    var id: String = "",
    val title: String = "",
    val message: String = "",
    @PropertyName("timestamp")
    var timestamp: String? = System.currentTimeMillis().toString(),
    val isRead: Boolean = false,
    val type: String = "general", // general, match, transfer_request, etc.
    val data: Map<String, String> = emptyMap(), // Additional data for deep linking
    val imageUri: String? = null, // Image URL for notification
    val linkUrl: String? = null, // Clickable link URL
    val fullContent: String? = null, // Full content for detailed view
    val isExpanded: Boolean = false // UI state for expanded view
) {
    companion object {
        fun fromDocument(document: DocumentSnapshot): NotificationItem? {
            return try {
                val data = document.data ?: return null
                
                // Handle timestamp conversion from Long to String
                val timestamp = when (val timestampValue = data["timestamp"]) {
                    is Long -> timestampValue.toString()
                    is String -> timestampValue
                    else -> System.currentTimeMillis().toString()
                }
                
                NotificationItem(
                    id = document.id,
                    title = data["title"] as? String ?: "",
                    message = data["message"] as? String ?: "",
                    timestamp = timestamp,
                    isRead = data["isRead"] as? Boolean ?: false,
                    type = data["type"] as? String ?: "general",
                    data = (data["data"] as? Map<String, String>) ?: emptyMap(),
                    imageUri = data["imageUri"] as? String,
                    linkUrl = data["linkUrl"] as? String,
                    fullContent = data["fullContent"] as? String
                )
            } catch (e: Exception) {
                null
            }
        }
    }
} 
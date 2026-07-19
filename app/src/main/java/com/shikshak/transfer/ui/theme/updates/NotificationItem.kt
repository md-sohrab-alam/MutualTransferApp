package com.shikshak.transfer.ui.theme.updates

import androidx.annotation.Keep
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.PropertyName
import timber.log.Timber

@Keep
data class NotificationItem(
    var id: String = "",
    val title: String = "",
    val message: String = "",
    @PropertyName("timestamp")
    var timestamp: String? = System.currentTimeMillis().toString(),
    val isRead: Boolean = false,
    val type: String = "general",
    val data: Map<String, String> = emptyMap(),
    val imageUri: String? = null,
    val linkUrl: String? = null,
    val fullContent: String? = null,
    val isExpanded: Boolean = false
) {
    companion object {
        fun fromDocument(document: DocumentSnapshot): NotificationItem? {
            return try {
                val data = document.data ?: return null

                val timestamp = when (val timestampValue = data["timestamp"]) {
                    is Long -> timestampValue.toString()
                    is Int -> timestampValue.toString()
                    is Double -> timestampValue.toLong().toString()
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
                    data = parseStringMap(data["data"]),
                    imageUri = data["imageUri"] as? String ?: data["imageUrl"] as? String,
                    linkUrl = data["linkUrl"] as? String,
                    fullContent = data["fullContent"] as? String
                )
            } catch (e: Exception) {
                Timber.e(e, "Failed to parse NotificationItem ${document.id}")
                null
            }
        }

        private fun parseStringMap(raw: Any?): Map<String, String> {
            val map = raw as? Map<*, *> ?: return emptyMap()
            return map.mapNotNull { (key, value) ->
                val k = key as? String ?: return@mapNotNull null
                val v = when (value) {
                    null -> return@mapNotNull null
                    is String -> value
                    is Number, is Boolean -> value.toString()
                    else -> value.toString()
                }
                k to v
            }.toMap()
        }
    }
}

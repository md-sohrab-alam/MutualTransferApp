package com.shikshak.transfer.ui.theme.data

import androidx.annotation.Keep
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
import timber.log.Timber

@Keep
data class Teacher(
    val uid: String = "",
    val name: String = "",
    val gender: String = "",
    val subject: String = "",
    val post: String = "",
    val district: String = "",
    val block: String = "",
    val schoolName: String = "",
    val contact: Contact = Contact(),
    val timestamp: Long? = null,
    val designation: String = "",
    val contactPreference: Boolean = true,
    val willingToMove: Boolean = false,
    val preferredDistricts: List<String> = emptyList(),
    val preferredBlocks: List<String> = emptyList(),
    val qualification: String = ""
) {
    companion object {
        /**
         * Safe Firestore mapping that survives R8 and mixed/legacy field types.
         * Prefer this over DocumentSnapshot.toObject(Teacher::class.java).
         */
        fun fromDocument(document: DocumentSnapshot): Teacher? {
            if (!document.exists()) return null
            return try {
                val data = document.data ?: return null
                Teacher(
                    uid = data.string("uid").ifBlank { document.id },
                    name = data.string("name"),
                    gender = data.string("gender"),
                    subject = data.string("subject"),
                    post = data.string("post"),
                    district = data.string("district"),
                    block = data.string("block"),
                    schoolName = data.string("schoolName"),
                    contact = parseContact(data["contact"]),
                    timestamp = parseTimestamp(data["timestamp"]),
                    designation = data.string("designation"),
                    contactPreference = data.boolean("contactPreference", true),
                    willingToMove = data.boolean("willingToMove", false),
                    preferredDistricts = data.stringList("preferredDistricts"),
                    preferredBlocks = data.stringList("preferredBlocks"),
                    qualification = data.string("qualification")
                )
            } catch (e: Exception) {
                Timber.e(e, "Failed to parse Teacher from document ${document.id}")
                null
            }
        }

        private fun parseContact(raw: Any?): Contact {
            return when (raw) {
                is Map<*, *> -> Contact(
                    email = (raw["email"] as? String).orEmpty(),
                    phone = (raw["phone"] as? String).orEmpty()
                )
                is String -> Contact(phone = raw)
                else -> Contact()
            }
        }

        private fun parseTimestamp(raw: Any?): Long? {
            return when (raw) {
                is Long -> raw
                is Int -> raw.toLong()
                is Double -> raw.toLong()
                is Timestamp -> raw.toDate().time
                is String -> raw.toLongOrNull()
                else -> null
            }
        }
    }
}

@Keep
data class Contact(
    val email: String = "",
    val phone: String = ""
)

@Keep
data class District(
    val name: String,
    val blocks: List<String>
)

@Keep
data class PostLevel(
    val name: String,
    val requiresSubject: Boolean = false
)

@Keep
data class Subject(
    val name: String,
    val postLevels: List<String>
)

internal fun Map<String, Any>.string(key: String): String =
    (this[key] as? String).orEmpty()

internal fun Map<String, Any>.boolean(key: String, default: Boolean): Boolean =
    this[key] as? Boolean ?: default

@Suppress("UNCHECKED_CAST")
internal fun Map<String, Any>.stringList(key: String): List<String> {
    return when (val value = this[key]) {
        is List<*> -> value.mapNotNull { it as? String }
        is String -> if (value.isBlank()) emptyList() else listOf(value)
        else -> emptyList()
    }
}

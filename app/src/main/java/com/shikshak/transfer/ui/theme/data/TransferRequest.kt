package com.shikshak.transfer.ui.theme.data

import androidx.annotation.Keep
import com.google.firebase.firestore.DocumentSnapshot
import timber.log.Timber

@Keep
data class TransferRequest(
    val teacherId: String = "",
    val teacherName: String = "",
    val currentDistrict: String = "",
    val currentSchool: String = "",
    val preferredDistricts: List<String> = emptyList(),
    val preferredBlocks: List<String> = emptyList(),
    val post: String = "",
    val designation: String = "",
    val subject: String = "",
    val qualification: String = "",
    val status: String = "PENDING",
    val submittedDate: String = "",
    val contactPreference: Boolean = true,
    val notes: String = ""
) {
    companion object {
        /**
         * Safe Firestore mapping that survives R8 and mixed/legacy field types.
         * Prefer this over DocumentSnapshot.toObject(TransferRequest::class.java).
         */
        fun fromDocument(document: DocumentSnapshot): TransferRequest? {
            if (!document.exists()) return null
            return try {
                val data = document.data ?: return null
                TransferRequest(
                    teacherId = data.string("teacherId").ifBlank { document.id },
                    teacherName = data.string("teacherName"),
                    currentDistrict = data.string("currentDistrict"),
                    currentSchool = data.string("currentSchool"),
                    preferredDistricts = data.stringList("preferredDistricts"),
                    preferredBlocks = data.stringList("preferredBlocks"),
                    post = data.string("post"),
                    designation = data.string("designation"),
                    subject = data.string("subject"),
                    qualification = data.string("qualification"),
                    status = data.string("status").ifBlank { "PENDING" },
                    submittedDate = data.string("submittedDate"),
                    contactPreference = data.boolean("contactPreference", true),
                    notes = data.string("notes")
                )
            } catch (e: Exception) {
                Timber.e(e, "Failed to parse TransferRequest from document ${document.id}")
                null
            }
        }
    }
}

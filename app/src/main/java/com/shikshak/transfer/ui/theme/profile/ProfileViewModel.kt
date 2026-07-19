package com.shikshak.transfer.ui.theme.profile

import com.shikshak.transfer.ui.theme.data.Teacher
import com.shikshak.transfer.ui.theme.data.Contact
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.shikshak.transfer.ui.theme.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor() : BaseViewModel() {
    private val db = Firebase.firestore
    private var currentTeacher: Teacher? = null

    fun saveTeacherProfile(teacher: Teacher, onSuccess: () -> Unit = {}) {
        updateLoadingState(true)
        // Write as a plain map so R8 cannot break Firestore field names.
        val payload = hashMapOf(
            "uid" to teacher.uid,
            "name" to teacher.name,
            "gender" to teacher.gender,
            "subject" to teacher.subject,
            "post" to teacher.post,
            "district" to teacher.district,
            "block" to teacher.block,
            "schoolName" to teacher.schoolName,
            "contact" to hashMapOf(
                "email" to teacher.contact.email,
                "phone" to teacher.contact.phone
            ),
            "timestamp" to (teacher.timestamp ?: System.currentTimeMillis()),
            "designation" to teacher.designation,
            "contactPreference" to teacher.contactPreference,
            "willingToMove" to teacher.willingToMove,
            "preferredDistricts" to teacher.preferredDistricts,
            "preferredBlocks" to teacher.preferredBlocks,
            "qualification" to teacher.qualification
        )
        db.collection("teachers").document(teacher.uid).set(payload)
            .addOnSuccessListener {
                // After saving the profile, update the transfer request if it exists
                db.collection("transfer_requests").document(teacher.uid).get()
                    .addOnSuccessListener { doc ->
                        if (doc.exists()) {
                            val request = com.shikshak.transfer.ui.theme.data.TransferRequest.fromDocument(doc)
                            if (request != null) {
                                val updatedRequest = request.copy(
                                    teacherName = teacher.name,
                                    currentDistrict = teacher.district,
                                    currentSchool = teacher.schoolName,
                                    post = teacher.post,
                                    designation = teacher.designation,
                                    subject = teacher.subject,
                                    qualification = teacher.qualification,
                                    contactPreference = teacher.contactPreference
                                )
                                db.collection("transfer_requests").document(teacher.uid).set(
                                    hashMapOf(
                                        "teacherId" to updatedRequest.teacherId,
                                        "teacherName" to updatedRequest.teacherName,
                                        "currentDistrict" to updatedRequest.currentDistrict,
                                        "currentSchool" to updatedRequest.currentSchool,
                                        "preferredDistricts" to updatedRequest.preferredDistricts,
                                        "preferredBlocks" to updatedRequest.preferredBlocks,
                                        "post" to updatedRequest.post,
                                        "designation" to updatedRequest.designation,
                                        "subject" to updatedRequest.subject,
                                        "qualification" to updatedRequest.qualification,
                                        "status" to updatedRequest.status,
                                        "submittedDate" to updatedRequest.submittedDate,
                                        "contactPreference" to updatedRequest.contactPreference,
                                        "notes" to updatedRequest.notes
                                    )
                                )
                            }
                        }
                        onSuccess()
                        updateLoadingState(false)
                    }
                    .addOnFailureListener { exception ->
                        // Even if request update fails, still call onSuccess for profile
                        onSuccess()
                        updateLoadingState(false)
                    }
            }
            .addOnFailureListener { exception ->
                handleFirestoreError(exception)
            }
    }
    
    fun loadTeacherProfile(uid: String, onLoaded: (Teacher) -> Unit) {
        updateLoadingState(true)
        db.collection("teachers").document(uid).get()
            .addOnSuccessListener { document ->
                updateLoadingState(false)
                if (document.exists()) {
                    val teacher = Teacher.fromDocument(document) ?: Teacher()
                    currentTeacher = teacher
                    onLoaded(teacher)
                } else {
                    val teacher = Teacher()
                    currentTeacher = teacher
                    onLoaded(teacher)
                }
            }
            .addOnFailureListener { exception ->
                updateLoadingState(false)
                handleFirestoreError(exception)
            }
    }
    
    fun getCurrentTeacher(): Teacher? = currentTeacher
    
}

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
        db.collection("teachers").document(teacher.uid).set(teacher)
            .addOnSuccessListener {
                // After saving the profile, update the transfer request if it exists
                db.collection("transfer_requests").document(teacher.uid).get()
                    .addOnSuccessListener { doc ->
                        if (doc.exists()) {
                            val request = doc.toObject(com.shikshak.transfer.ui.theme.data.TransferRequest::class.java)
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
                                db.collection("transfer_requests").document(teacher.uid).set(updatedRequest)
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
                    val teacher = document.toObject(Teacher::class.java) ?: Teacher()
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

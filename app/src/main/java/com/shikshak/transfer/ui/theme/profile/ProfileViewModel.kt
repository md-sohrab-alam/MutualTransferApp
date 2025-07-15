package com.shikshak.transfer.ui.theme.profile

import Teacher
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.shikshak.transfer.ui.theme.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor() : BaseViewModel() {
    private val db = Firebase.firestore

    fun saveTeacherProfile(teacher: Teacher) {
        updateLoadingState(true)
        db.collection("teachers").document(teacher.uid).set(teacher)
            .addOnSuccessListener {
                updateLoadingState(false)
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
                    onLoaded(teacher)
                } else {
                    onLoaded(Teacher())
                }
            }
            .addOnFailureListener { exception ->
                updateLoadingState(false)
                handleFirestoreError(exception)
            }
    }
}

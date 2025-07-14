package com.shikshak.transfer.ui.theme.navigation

import Teacher
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.lifecycle.HiltViewModel
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class SharedViewModel @Inject constructor() : ViewModel() {
    var currentTeacher by mutableStateOf<Teacher?>(null)
        private set

    private val db = Firebase.firestore
    private val auth = FirebaseAuth.getInstance()

    fun loadCurrentTeacher(onLoaded: () -> Unit, onError: (String) -> Unit) {
        val uid = auth.currentUser?.uid ?: return onError("Not logged in")
        db.collection("teachers").document(uid).get()
            .addOnSuccessListener {
                currentTeacher = it.toObject(Teacher::class.java)
                Timber.i("Loaded teacher: $currentTeacher")
                onLoaded()
            }
            .addOnFailureListener { e ->
                onError(e.localizedMessage ?: "Failed to load profile")
            }
    }

    fun setTeacher(teacher: Teacher) {
        currentTeacher = teacher
        Timber.i("Set teacher: $teacher")
    }

    fun isProfileComplete(): Boolean {
        val t = currentTeacher
        return t != null &&
                t.name.isNotBlank() &&
                t.subject.isNotBlank() &&
                t.district.isNotBlank() &&
                t.preferredDistrict.isNotBlank()
    }

}

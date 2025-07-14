package com.shikshak.transfer.ui.theme.profile

import Teacher
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor() : ViewModel() {
    private val db = Firebase.firestore

    fun saveTeacherProfile(teacher: Teacher) {
        db.collection("teachers").document(teacher.uid).set(teacher)
    }
}

package com.shikshak.transfer.ui.theme.register

import com.shikshak.transfer.ui.theme.data.Teacher
import com.shikshak.transfer.ui.theme.data.Contact
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.shikshak.transfer.ui.theme.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject

@HiltViewModel
class RegisterViewModel @Inject constructor() : BaseViewModel() {

    fun registerUser(
        name: String,
        email: String,
        password: String,
        school: String,
        district: String,
        subject: String,
        post: String,
        onSuccess: () -> Unit
    ) {
        updateLoadingState(true)
        val auth = FirebaseAuth.getInstance()
        val firestore = Firebase.firestore

        auth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener { result ->
                val uid = result.user?.uid ?: return@addOnSuccessListener
                val teacher = Teacher(
                    uid = uid,
                    name = name,
                    subject = subject,
                    post = post,
                    district = district,
                    schoolName = school,
                    contact = Contact(email = email)
                )
                firestore.collection("teachers").document(uid)
                    .set(teacher)
                    .addOnSuccessListener {
                        updateLoadingState(false)
                        onSuccess()
                    }
                    .addOnFailureListener { exception ->
                        handleFirestoreError(exception)
                    }
            }
            .addOnFailureListener { exception ->
                updateLoadingState(false)
                showError("Registration failed: ${exception.message}")
            }
    }
}

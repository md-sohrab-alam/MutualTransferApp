package com.shikshak.transfer.ui.theme.register

import Teacher
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject

@HiltViewModel
class RegisterViewModel @Inject constructor() : ViewModel() {

    var isLoading by mutableStateOf(false)
    var errorMessage by mutableStateOf<String?>(null)

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
        isLoading = true
        val auth = FirebaseAuth.getInstance()
        val firestore = Firebase.firestore

        auth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener { result ->
                val uid = result.user?.uid ?: return@addOnSuccessListener
                val teacher = Teacher(uid, name, email, school, district, subject, post)
                firestore.collection("teachers").document(uid)
                    .set(teacher)
                    .addOnSuccessListener {
                        isLoading = false
                        onSuccess()
                    }
                    .addOnFailureListener {
                        isLoading = false
                        errorMessage = "Failed to save teacher info: ${it.message}"
                    }
            }
            .addOnFailureListener {
                isLoading = false
                errorMessage = "Registration failed: ${it.message}"
            }
    }
}

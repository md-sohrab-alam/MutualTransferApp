package com.shikshak.transfer.ui.theme.login

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor() : ViewModel() {
    private val auth = FirebaseAuth.getInstance()

    fun login(
        email: String,
        password: String,
        onLoginSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener { onLoginSuccess() }
            .addOnFailureListener { onError(it.localizedMessage ?: "Login failed") }
    }
}

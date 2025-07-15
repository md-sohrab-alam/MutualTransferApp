package com.shikshak.transfer.ui.theme.login

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.shikshak.transfer.ui.theme.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor() : BaseViewModel() {
    private val auth = FirebaseAuth.getInstance()

    fun login(
        email: String,
        password: String,
        onLoginSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        updateLoadingState(true)
        auth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener { 
                updateLoadingState(false)
                onLoginSuccess() 
            }
            .addOnFailureListener { exception ->
                updateLoadingState(false)
                showError(exception.localizedMessage ?: "Login failed")
                onError(exception.localizedMessage ?: "Login failed")
            }
    }
}

package com.shikshak.transfer.ui.theme.login

import android.app.Activity
import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.shikshak.transfer.ui.theme.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import timber.log.Timber
import java.util.concurrent.TimeUnit

@HiltViewModel
class PhoneAuthViewModel @Inject constructor() : BaseViewModel() {

    private val _verificationId = mutableStateOf("")
    val verificationId: State<String> = _verificationId

    private val _otpSent = mutableStateOf(false)
    val otpSent: State<Boolean> = _otpSent

    private val _isOtpVerified = mutableStateOf(false)
    val isOtpVerified: State<Boolean> = _isOtpVerified

    fun sendOtp(phoneNumber: String, activity: Activity) {
        updateLoadingState(true)
        val options = PhoneAuthOptions.newBuilder(FirebaseAuth.getInstance())
            .setPhoneNumber("+91$phoneNumber")
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(activity)
            .setCallbacks(object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                    Timber.i("Verification completed: $credential")
                    signInWithPhoneAuthCredential(credential)
                    updateLoadingState(false)
                }

                override fun onVerificationFailed(e: FirebaseException) {
                    Timber.e("Verification failed: ${e.message}")
                    updateLoadingState(false)
                    errorMessage.value = e.message
                    showErrorDialog.value = true

                }

                override fun onCodeSent(
                    verificationId: String,
                    token: PhoneAuthProvider.ForceResendingToken
                ) {
                    Timber.i("Code sent: $verificationId")
                    _verificationId.value = verificationId
                    _otpSent.value = true
                    updateLoadingState(false)
                }
            })
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    fun verifyOtp(code: String) {
        Timber.i("verifyOtp: $code, verificationId: ${verificationId.value}")
        val credential = PhoneAuthProvider.getCredential(verificationId.value, code)
        signInWithPhoneAuthCredential(credential)
    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        FirebaseAuth.getInstance().signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Timber.tag("PhoneAuth").d("Sign-in successful")
                    _isOtpVerified.value = true
                } else {
                    Timber.tag("PhoneAuth").e("Sign-in failed: ${task.exception?.message}")
                }
            }
    }
}

package com.shikshak.transfer.ui.theme.request

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.shikshak.transfer.ui.theme.BaseViewModel
import com.shikshak.transfer.ui.theme.data.Teacher
import com.shikshak.transfer.ui.theme.data.TransferRequest
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class CreateRequestViewModel @Inject constructor() : BaseViewModel() {
    
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    
    private val _currentTeacher = MutableStateFlow<Teacher?>(null)
    val currentTeacher: StateFlow<Teacher?> = _currentTeacher.asStateFlow()
    
    fun loadCurrentTeacher() {
        updateLoadingState(true)
        viewModelScope.launch {
            try {
                val userId = auth.currentUser?.uid
                if (userId != null) {
                    firestore.collection("teachers").document(userId).get()
                        .addOnSuccessListener { document ->
                            if (document.exists()) {
                                val teacher = document.toObject(Teacher::class.java)
                                _currentTeacher.value = teacher
                                Timber.d("Loaded current teacher: ${teacher?.name}")
                            } else {
                                Timber.d("No teacher profile found")
                            }
                            updateLoadingState(false)
                        }
                        .addOnFailureListener { exception ->
                            Timber.e(exception, "Error loading current teacher")
                            handleFirestoreError(exception)
                        }
                } else {
                    updateLoadingState(false)
                }
            } catch (e: Exception) {
                Timber.e(e, "Error in loadCurrentTeacher")
                handleFirestoreError(e)
            }
        }
    }
    
    fun submitTransferRequest(transferRequest: TransferRequest) {
        updateLoadingState(true)
        viewModelScope.launch {
            try {
                val userId = auth.currentUser?.uid
                if (userId != null) {
                    firestore.collection("transfer_requests").document(userId).set(transferRequest)
                        .addOnSuccessListener {
                            Timber.d("Transfer request submitted successfully")
                            updateLoadingState(false)
                        }
                        .addOnFailureListener { exception ->
                            Timber.e(exception, "Error submitting transfer request")
                            handleFirestoreError(exception)
                        }
                } else {
                    updateLoadingState(false)
                }
            } catch (e: Exception) {
                Timber.e(e, "Error in submitTransferRequest")
                handleFirestoreError(e)
            }
        }
    }
} 
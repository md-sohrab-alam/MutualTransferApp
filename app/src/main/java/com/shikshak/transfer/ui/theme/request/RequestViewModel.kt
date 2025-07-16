package com.shikshak.transfer.ui.theme.request

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.shikshak.transfer.ui.theme.BaseViewModel
import com.shikshak.transfer.ui.theme.data.TransferRequest
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class RequestViewModel @Inject constructor() : BaseViewModel() {
    
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    
    private val _hasTransferRequest = MutableStateFlow(false)
    val hasTransferRequest: StateFlow<Boolean> = _hasTransferRequest.asStateFlow()
    
    private val _transferRequest = MutableStateFlow<TransferRequest?>(null)
    val transferRequest: StateFlow<TransferRequest?> = _transferRequest.asStateFlow()
    
    var showCancelDialog by mutableStateOf(false)
    
    fun checkTransferRequest() {
        updateLoadingState(true)
        viewModelScope.launch {
            try {
                val userId = auth.currentUser?.uid
                if (userId != null) {
                    val requestDoc = firestore.collection("transfer_requests").document(userId).get()
                    requestDoc.addOnSuccessListener { document ->
                        if (document.exists()) {
                            val request = document.toObject(TransferRequest::class.java)
                            _transferRequest.value = request
                            _hasTransferRequest.value = true
                            Timber.d("Transfer request found: ${request?.status}")
                        } else {
                            _hasTransferRequest.value = false
                            _transferRequest.value = null
                            Timber.d("No transfer request found")
                        }
                        updateLoadingState(false)
                    }.addOnFailureListener { exception ->
                        Timber.e(exception, "Error checking transfer request")
                        handleFirestoreError(exception)
                    }
                } else {
                    updateLoadingState(false)
                }
            } catch (e: Exception) {
                Timber.e(e, "Error in checkTransferRequest")
                handleFirestoreError(e)
            }
        }
    }
    
    private var onNavigateToCreateRequest: (() -> Unit)? = null
    private var onNavigateToEditRequest: (() -> Unit)? = null
    
    fun setNavigationCallbacks(
        onNavigateToCreateRequest: () -> Unit,
        onNavigateToEditRequest: () -> Unit
    ) {
        this.onNavigateToCreateRequest = onNavigateToCreateRequest
        this.onNavigateToEditRequest = onNavigateToEditRequest
    }
    
    fun navigateToCreateRequest() {
        onNavigateToCreateRequest?.invoke()
    }
    
    fun navigateToEditRequest() {
        // Navigate to the EditRequest screen
        // The navigation will be handled by the parent composable
        onNavigateToEditRequest?.invoke()
    }
    
    fun showCancelConfirmation() {
        showCancelDialog = true
    }
    
    fun hideCancelConfirmation() {
        showCancelDialog = false
    }
    
    fun cancelRequest() {
        updateLoadingState(true)
        viewModelScope.launch {
            try {
                val userId = auth.currentUser?.uid
                if (userId != null) {
                    firestore.collection("transfer_requests").document(userId).delete()
                        .addOnSuccessListener {
                            _hasTransferRequest.value = false
                            _transferRequest.value = null
                            Timber.d("Transfer request cancelled")
                            updateLoadingState(false)
                        }
                        .addOnFailureListener { exception ->
                            Timber.e(exception, "Error cancelling transfer request")
                            handleFirestoreError(exception)
                        }
                } else {
                    updateLoadingState(false)
                }
            } catch (e: Exception) {
                Timber.e(e, "Error in cancelRequest")
                handleFirestoreError(e)
            }
        }
    }
} 
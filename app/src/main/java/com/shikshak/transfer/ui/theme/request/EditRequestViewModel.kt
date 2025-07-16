package com.shikshak.transfer.ui.theme.request

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.shikshak.transfer.ui.theme.BaseViewModel
import com.shikshak.transfer.ui.theme.data.TransferRequest
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class EditRequestViewModel @Inject constructor() : BaseViewModel() {
    
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    
    fun updateTransferRequest(transferRequest: TransferRequest) {
        updateLoadingState(true)
        viewModelScope.launch {
            try {
                val userId = auth.currentUser?.uid
                if (userId != null) {
                    firestore.collection("transfer_requests").document(userId).set(transferRequest)
                        .addOnSuccessListener {
                            Timber.d("Transfer request updated successfully")
                            updateLoadingState(false)
                        }
                        .addOnFailureListener { exception ->
                            Timber.e(exception, "Error updating transfer request")
                            handleFirestoreError(exception)
                        }
                } else {
                    updateLoadingState(false)
                }
            } catch (e: Exception) {
                Timber.e(e, "Error in updateTransferRequest")
                handleFirestoreError(e)
            }
        }
    }
} 
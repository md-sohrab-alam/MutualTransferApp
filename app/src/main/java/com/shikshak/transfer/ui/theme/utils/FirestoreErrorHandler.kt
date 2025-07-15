package com.shikshak.transfer.ui.theme.utils

import com.google.firebase.firestore.FirebaseFirestoreException
import timber.log.Timber

object FirestoreErrorHandler {
    
    fun handleFirestoreError(exception: Exception, onError: (String) -> Unit) {
        Timber.e(exception, "Firestore operation failed")
        
        val errorMessage = when (exception) {
            is FirebaseFirestoreException -> {
                when (exception.code) {
                    FirebaseFirestoreException.Code.PERMISSION_DENIED -> 
                        "Access denied. Please check your permissions."
                    FirebaseFirestoreException.Code.UNAVAILABLE -> 
                        "Service temporarily unavailable. Please try again."
                    FirebaseFirestoreException.Code.DEADLINE_EXCEEDED -> 
                        "Request timed out. Please check your connection and try again."
                    FirebaseFirestoreException.Code.NOT_FOUND -> 
                        "Data not found."
                    FirebaseFirestoreException.Code.ALREADY_EXISTS -> 
                        "Data already exists."
                    FirebaseFirestoreException.Code.RESOURCE_EXHAUSTED -> 
                        "Service quota exceeded. Please try again later."
                    FirebaseFirestoreException.Code.FAILED_PRECONDITION -> 
                        "Operation failed due to invalid state."
                    FirebaseFirestoreException.Code.ABORTED -> 
                        "Operation was aborted. Please try again."
                    FirebaseFirestoreException.Code.OUT_OF_RANGE -> 
                        "Requested data is out of range."
                    FirebaseFirestoreException.Code.UNIMPLEMENTED -> 
                        "Operation not implemented."
                    FirebaseFirestoreException.Code.INTERNAL -> 
                        "Internal server error. Please try again."
                    FirebaseFirestoreException.Code.UNAUTHENTICATED -> 
                        "Authentication required. Please log in again."
                    else -> "An error occurred: ${exception.message}"
                }
            }
            else -> {
                when {
                    exception.message?.contains("network", ignoreCase = true) == true -> 
                        "Network error. Please check your internet connection."
                    exception.message?.contains("timeout", ignoreCase = true) == true -> 
                        "Request timed out. Please try again."
                    else -> "An unexpected error occurred: ${exception.message ?: "Unknown error"}"
                }
            }
        }
        
        onError(errorMessage)
    }
    
    fun handleFirestoreSuccess(
        onSuccess: () -> Unit,
        onError: (String) -> Unit,
        errorMessage: String = "Operation failed"
    ): (Exception?) -> Unit {
        return { exception ->
            if (exception == null) {
                onSuccess()
            } else {
                handleFirestoreError(exception, onError)
            }
        }
    }
} 
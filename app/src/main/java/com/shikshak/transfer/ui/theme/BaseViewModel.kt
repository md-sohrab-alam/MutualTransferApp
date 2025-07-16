package com.shikshak.transfer.ui.theme

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shikshak.transfer.ui.theme.utils.FirestoreErrorHandler
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

@HiltViewModel
open class BaseViewModel @Inject constructor() : ViewModel() {
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    fun updateLoadingState(isLoading: Boolean) {
        _isLoading.value = isLoading
    }

    // State to control the visibility of the error dialog
    val showErrorDialog = mutableStateOf(false)
    // State to hold the error message
    val errorMessage =  mutableStateOf<String?>(null)

    // Common method to handle Firestore errors
    protected fun handleFirestoreError(exception: Exception) {
        updateLoadingState(false)
        FirestoreErrorHandler.handleFirestoreError(exception) { errorMsg ->
            errorMessage.value = errorMsg
            showErrorDialog.value = true
        }
    }

    // Common method to clear error dialog
    fun clearError() {
        showErrorDialog.value = false
        errorMessage.value = null
    }

    // Common method to show error
    protected fun showError(message: String) {
        updateLoadingState(false)
        errorMessage.value = message
        showErrorDialog.value = true
    }
}
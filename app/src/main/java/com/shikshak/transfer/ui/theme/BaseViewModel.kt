package com.shikshak.transfer.ui.theme

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shikshak.transfer.ui.theme.utils.FirestoreErrorHandler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

open class BaseViewModel : ViewModel() {
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    fun updateLoadingState(isLoading: Boolean) {
        _isLoading.value = isLoading
    }

    val showErrorDialog = mutableStateOf(false)
    val errorMessage = mutableStateOf<String?>(null)

    protected fun handleFirestoreError(exception: Exception) {
        updateLoadingState(false)
        FirestoreErrorHandler.handleFirestoreError(exception) { errorMsg ->
            errorMessage.value = errorMsg
            showErrorDialog.value = true
        }
    }

    fun clearError() {
        showErrorDialog.value = false
        errorMessage.value = null
    }

    protected fun showError(message: String) {
        updateLoadingState(false)
        errorMessage.value = message
        showErrorDialog.value = true
    }
}

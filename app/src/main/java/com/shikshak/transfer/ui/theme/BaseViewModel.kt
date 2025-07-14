package com.shikshak.transfer.ui.theme

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject

@HiltViewModel
open class BaseViewModel @Inject constructor() : ViewModel() {
    private val _isLoading = mutableStateOf(false)
    val isLoading: State<Boolean> = _isLoading

    fun updateLoadingState(isLoading: Boolean) {
        _isLoading.value = isLoading
    }

    // State to control the visibility of the error dialog
    val showErrorDialog = mutableStateOf(false)
    // State to hold the error message
    val errorMessage =  mutableStateOf<String?>(null)

}
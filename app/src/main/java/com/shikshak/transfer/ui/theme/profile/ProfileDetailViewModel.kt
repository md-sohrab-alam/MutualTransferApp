package com.shikshak.transfer.ui.theme.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.shikshak.transfer.ui.theme.data.Teacher
import com.shikshak.transfer.ui.theme.data.TransferRequest
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class ProfileDetailViewModel @Inject constructor() : ViewModel() {
    
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    
    private val _teacher = MutableStateFlow<Teacher?>(null)
    val teacher: StateFlow<Teacher?> = _teacher.asStateFlow()
    
    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _currentRequest = MutableStateFlow<TransferRequest?>(null)
    val currentRequest: StateFlow<TransferRequest?> = _currentRequest.asStateFlow()
    
    fun loadTeacherProfile(teacherId: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                
                val teacherDoc = firestore.collection("teachers").document(teacherId).get().await()
                
                if (teacherDoc.exists()) {
                    val teacherData = teacherDoc.toObject(Teacher::class.java)
                    _teacher.value = teacherData
                    Timber.d("Loaded teacher profile: ${teacherData?.name}")
                } else {
                    Timber.e("Teacher profile not found for ID: $teacherId")
                }
            } catch (e: Exception) {
                Timber.e(e, "Error loading teacher profile")
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun loadCurrentRequest() {
        viewModelScope.launch {
            try {
                val currentUserId = auth.currentUser?.uid ?: return@launch
                
                val requestQuery = firestore.collection("transferRequests")
                    .whereEqualTo("teacherId", currentUserId)
                    .limit(1)
                    .get()
                    .await()
                
                if (!requestQuery.isEmpty) {
                    val requestDoc = requestQuery.documents.first()
                    val requestData = requestDoc.toObject(TransferRequest::class.java)
                    _currentRequest.value = requestData
                    Timber.d("Loaded current request for user: $currentUserId")
                }
            } catch (e: Exception) {
                Timber.e(e, "Error loading current request")
            }
        }
    }
} 
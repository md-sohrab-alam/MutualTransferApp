package com.shikshak.transfer.ui.theme.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.shikshak.transfer.ui.theme.data.Teacher
import com.shikshak.transfer.ui.theme.data.TransferRequest
import com.shikshak.transfer.ui.theme.utils.MatchingService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor() : ViewModel() {
    
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    
    private val _currentTeacher = MutableStateFlow<Teacher?>(null)
    val currentTeacher: StateFlow<Teacher?> = _currentTeacher
    
    private val _hasTransferRequest = MutableStateFlow(false)
    val hasTransferRequest: StateFlow<Boolean> = _hasTransferRequest
    
    private val _transferRequest = MutableStateFlow<TransferRequest?>(null)
    val transferRequest: StateFlow<TransferRequest?> = _transferRequest
    
    private val _matches = MutableStateFlow<List<Pair<Teacher, TransferRequest>>>(emptyList())
    val matches: StateFlow<List<Pair<Teacher, TransferRequest>>> = _matches
    
    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading
    
    fun loadTeacherData() {
        viewModelScope.launch {
            try {
                val userId = auth.currentUser?.uid
                if (userId != null) {
                    val teacherDoc = firestore.collection("teachers").document(userId).get()
                    teacherDoc.addOnSuccessListener { document ->
                        if (document.exists()) {
                            val teacher = document.toObject(Teacher::class.java)
                            _currentTeacher.value = teacher
                            Timber.d("Teacher data loaded: ${teacher?.name}")
                        } else {
                            Timber.d("No teacher document found")
                        }
                    }.addOnFailureListener { exception ->
                        Timber.e(exception, "Error loading teacher data")
                    }
                } else {
                    Timber.d("No user ID found")
                }
            } catch (e: Exception) {
                Timber.e(e, "Error in loadTeacherData")
            }
        }
    }
    
    fun checkTransferRequest() {
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
                            loadMatches()
                        } else {
                            _hasTransferRequest.value = false
                            Timber.d("No transfer request found")
                        }
                        _isLoading.value = false
                    }.addOnFailureListener { exception ->
                        Timber.e(exception, "Error checking transfer request")
                        _isLoading.value = false
                    }
                } else {
                    _isLoading.value = false
                }
            } catch (e: Exception) {
                Timber.e(e, "Error in checkTransferRequest")
                _isLoading.value = false
            }
        }
    }
    
    private fun loadMatches() {
        viewModelScope.launch {
            try {
                val userId = auth.currentUser?.uid
                val currentRequest = _transferRequest.value
                
                if (userId != null && currentRequest != null) {
                    // Step 1: Find all transfer requests that want to come to current teacher's district
                    // Note: This requires a composite index in Firestore
                    val matchesQuery = firestore.collection("transfer_requests")
                        .whereArrayContains("preferredDistricts", currentRequest.currentDistrict)
                        .whereNotEqualTo("teacherId", userId)
                        .get()
                    
                    matchesQuery.addOnSuccessListener { documents ->
                        val potentialMatches = mutableListOf<Pair<Teacher, TransferRequest>>()
                        var processedCount = 0
                        
                        if (documents.isEmpty) {
                            _matches.value = emptyList()
                            Timber.d("No potential matches found")
                            return@addOnSuccessListener
                        }
                        
                        for (document in documents) {
                            val request = document.toObject(TransferRequest::class.java)
                            if (request != null) {
                                // Step 2: Check if this teacher's current district is in our preferred districts
                                if (currentRequest.preferredDistricts.contains(request.currentDistrict)) {
                                    // Step 3: Get the teacher details for this request
                                    firestore.collection("teachers").document(request.teacherId).get()
                                        .addOnSuccessListener { teacherDoc ->
                                            if (teacherDoc.exists()) {
                                                val teacher = teacherDoc.toObject(Teacher::class.java)
                                                if (teacher != null) {
                                                    // Step 4: Apply additional matching criteria
                                    if (MatchingService.isCompatibleMatch(currentRequest, request, teacher)) {
                                        potentialMatches.add(Pair(teacher, request))
                                    }
                                                }
                                            }
                                            processedCount++
                                            if (processedCount == documents.size()) {
                                                // Step 5: Sort matches by relevance score
                                                val sortedMatches = MatchingService.sortMatchesByRelevance(potentialMatches.map { it.first }, currentRequest)
                                                _matches.value = potentialMatches
                                                Timber.d("Found ${potentialMatches.size} compatible matches")
                                            }
                                        }
                                        .addOnFailureListener { exception ->
                                            Timber.e(exception, "Error loading teacher details")
                                            processedCount++
                                            if (processedCount == documents.size()) {
                                                val sortedMatches = MatchingService.sortMatchesByRelevance(potentialMatches.map { it.first }, currentRequest)
                                                _matches.value = potentialMatches
                                            }
                                        }
                                } else {
                                    processedCount++
                                    if (processedCount == documents.size()) {
                                        val sortedMatches = MatchingService.sortMatchesByRelevance(potentialMatches.map { it.first }, currentRequest)
                                        _matches.value = potentialMatches
                                        Timber.d("Found ${potentialMatches.size} compatible matches")
                                    }
                                }
                            } else {
                                processedCount++
                                if (processedCount == documents.size()) {
                                    val sortedMatches = MatchingService.sortMatchesByRelevance(potentialMatches.map { it.first }, currentRequest)
                                    _matches.value = potentialMatches
                                }
                            }
                        }
                    }.addOnFailureListener { exception ->
                        Timber.e(exception, "Error loading matches")
                        
                        // Check if it's an index error
                        if (exception.message?.contains("FAILED_PRECONDITION") == true && 
                            exception.message?.contains("requires an index") == true) {
                            // Use fallback method when index is not available
                            Timber.e("Firestore index required. Using fallback method...")
                            loadMatchesFallback()
                        } else {
                            _matches.value = emptyList()
                        }
                    }
                } else {
                    _matches.value = emptyList()
                }
            } catch (e: Exception) {
                Timber.e(e, "Error in loadMatches")
                _matches.value = emptyList()
            }
        }
    }
    
    /**
     * Fallback method to load matches without requiring composite index
     * This method fetches all transfer requests and filters them in memory
     */
    private fun loadMatchesFallback() {
        viewModelScope.launch {
            try {
                val userId = auth.currentUser?.uid
                val currentRequest = _transferRequest.value
                
                if (userId != null && currentRequest != null) {
                    // Fetch all transfer requests and filter in memory
                    val allRequestsQuery = firestore.collection("transfer_requests").get()
                    
                    allRequestsQuery.addOnSuccessListener { documents ->
                        val potentialMatches = mutableListOf<Pair<Teacher, TransferRequest>>()
                        var processedCount = 0
                        
                        if (documents.isEmpty) {
                            _matches.value = emptyList()
                            Timber.d("No transfer requests found")
                            return@addOnSuccessListener
                        }
                        
                        for (document in documents) {
                            val request = document.toObject(TransferRequest::class.java)
                            if (request != null && request.teacherId != userId) {
                                // Check if this teacher wants to come to current teacher's district
                                if (request.preferredDistricts.contains(currentRequest.currentDistrict)) {
                                    // Check if current teacher wants to go to this teacher's district
                                    if (currentRequest.preferredDistricts.contains(request.currentDistrict)) {
                                        // Get the teacher details for this request
                                        firestore.collection("teachers").document(request.teacherId).get()
                                            .addOnSuccessListener { teacherDoc ->
                                                if (teacherDoc.exists()) {
                                                    val teacher = teacherDoc.toObject(Teacher::class.java)
                                                    if (teacher != null) {
                                                        // Apply additional matching criteria
                                                        if (MatchingService.isCompatibleMatch(currentRequest, request, teacher)) {
                                                            potentialMatches.add(Pair(teacher, request))
                                                        }
                                                    }
                                                }
                                                processedCount++
                                                if (processedCount == documents.size()) {
                                                    val sortedMatches = MatchingService.sortMatchesByRelevance(potentialMatches.map { it.first }, currentRequest)
                                                    _matches.value = potentialMatches
                                                    Timber.d("Found ${potentialMatches.size} compatible matches (fallback method)")
                                                }
                                            }
                                            .addOnFailureListener { exception ->
                                                Timber.e(exception, "Error loading teacher details")
                                                processedCount++
                                                if (processedCount == documents.size()) {
                                                    val sortedMatches = MatchingService.sortMatchesByRelevance(potentialMatches.map { it.first }, currentRequest)
                                                    _matches.value = potentialMatches
                                                }
                                            }
                                    } else {
                                        processedCount++
                                        if (processedCount == documents.size()) {
                                            val sortedMatches = MatchingService.sortMatchesByRelevance(potentialMatches.map { it.first }, currentRequest)
                                            _matches.value = potentialMatches
                                        }
                                    }
                                } else {
                                    processedCount++
                                    if (processedCount == documents.size()) {
                                        val sortedMatches = MatchingService.sortMatchesByRelevance(potentialMatches.map { it.first }, currentRequest)
                                        _matches.value = potentialMatches
                                    }
                                }
                            } else {
                                processedCount++
                                if (processedCount == documents.size()) {
                                    val sortedMatches = MatchingService.sortMatchesByRelevance(potentialMatches.map { it.first }, currentRequest)
                                    _matches.value = potentialMatches
                                }
                            }
                        }
                    }.addOnFailureListener { exception ->
                        Timber.e(exception, "Error loading matches (fallback method)")
                        _matches.value = emptyList()
                    }
                } else {
                    _matches.value = emptyList()
                }
            } catch (e: Exception) {
                Timber.e(e, "Error in loadMatchesFallback")
                _matches.value = emptyList()
            }
        }
    }
    

    
    fun cancelRequest() {
        viewModelScope.launch {
            try {
                val userId = auth.currentUser?.uid
                if (userId != null) {
                    firestore.collection("transfer_requests").document(userId).delete()
                        .addOnSuccessListener {
                            _hasTransferRequest.value = false
                            _transferRequest.value = null
                            _matches.value = emptyList()
                            Timber.d("Transfer request cancelled")
                        }
                        .addOnFailureListener { exception ->
                            Timber.e(exception, "Error cancelling transfer request")
                        }
                }
            } catch (e: Exception) {
                Timber.e(e, "Error in cancelRequest")
            }
        }
    }
    
    fun contactMatch(match: Teacher) {
        // This would typically open a contact dialog or navigate to a contact screen
        Timber.d("Contacting match: ${match.name}")
    }
    
    fun refreshData() {
        _isLoading.value = true
        loadTeacherData()
        checkTransferRequest()
    }
} 
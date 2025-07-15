package com.shikshak.transfer.ui.theme.matchprofile

import Teacher
import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.shikshak.transfer.ui.theme.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class MatchViewModel @Inject constructor() : BaseViewModel() {
    private val db = Firebase.firestore
    private val auth = FirebaseAuth.getInstance()

    private val _matches = mutableStateListOf<Teacher>()
    val matches: List<Teacher> = _matches

    fun findMutualMatches(currentTeacher: Teacher) {
        updateLoadingState(true)
        db.collection("teachers")
            .whereEqualTo("district", currentTeacher.preferredDistrict)
            .whereEqualTo("preferredDistrict", currentTeacher.district)
            .get()
            .addOnSuccessListener { documents ->
                Timber.i("Found ${documents.size()} matches")
                _matches.clear()
                for (doc in documents) {
                    val teacher = doc.toObject(Teacher::class.java)
                    if (teacher != null && teacher.uid != currentTeacher.uid) {
                        _matches.add(teacher)
                    }
                }
                updateLoadingState(false)
            }
            .addOnFailureListener { exception ->
                Timber.e(exception, "Failed to find matches")
                handleFirestoreError(exception)
            }
    }
}

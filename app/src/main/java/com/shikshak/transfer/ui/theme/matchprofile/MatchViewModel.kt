package com.shikshak.transfer.ui.theme.matchprofile

import Teacher
import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.lifecycle.HiltViewModel
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class MatchViewModel @Inject constructor() : ViewModel() {
    private val db = Firebase.firestore
    private val auth = FirebaseAuth.getInstance()

    private val _matches = mutableStateListOf<Teacher>()
    val matches: List<Teacher> = _matches

    fun findMutualMatches(currentTeacher: Teacher) {
        db.collection("teachers")
            .whereEqualTo("district", currentTeacher.preferredDistrict)
            .whereEqualTo("preferredDistrict", currentTeacher.district)
            .get()
            .addOnSuccessListener { documents ->
                Timber.i("Found ${documents.size()} matches")
                _matches.clear()
                for (doc in documents) {
                    val teacher = doc.toObject(Teacher::class.java)
                    if (teacher.uid != currentTeacher.uid) {
                        _matches.add(teacher)
                    }
                }
            }
            .addOnFailureListener {
                Timber.e(it, "Failed to find matches")
            }
    }
}

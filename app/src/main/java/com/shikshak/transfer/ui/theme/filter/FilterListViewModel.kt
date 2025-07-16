package com.shikshak.transfer.ui.theme.filter

import com.shikshak.transfer.ui.theme.data.Teacher
import com.shikshak.transfer.ui.theme.data.Contact
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.shikshak.transfer.ui.theme.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class FilterListViewModel @Inject constructor() : BaseViewModel() {
    private val db = Firebase.firestore
    private val _allTeachers = mutableStateListOf<Teacher>()
    val filteredTeachers = mutableStateListOf<Teacher>()

    init {
        loadAllTeachers()
    }

    private fun loadAllTeachers() {
        updateLoadingState(true)
        db.collection("teachers").get()
            .addOnSuccessListener { result ->
                _allTeachers.clear()
                filteredTeachers.clear()
                for (doc in result) {
                    val t = doc.toObject(Teacher::class.java)
                    if (t != null) {
                        _allTeachers.add(t)
                        filteredTeachers.add(t)
                    }
                }
                updateLoadingState(false)
            }
            .addOnFailureListener { exception ->
                Timber.e(exception, "Failed to load teachers")
                handleFirestoreError(exception)
            }
    }

    fun filterTeachers(query: String) {
        val lower = query.lowercase()
        filteredTeachers.clear()
        filteredTeachers.addAll(_allTeachers.filter {
            it.district.lowercase().contains(lower) ||
                    it.subject.lowercase().contains(lower) ||
                    it.post.lowercase().contains(lower)
        })
    }
}

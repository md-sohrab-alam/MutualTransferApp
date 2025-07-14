package com.shikshak.transfer.ui.theme.filter

import Teacher
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class FilterListViewModel @Inject constructor() : ViewModel() {
    private val db = Firebase.firestore
    private val _allTeachers = mutableStateListOf<Teacher>()
    val filteredTeachers = mutableStateListOf<Teacher>()

    init {
        db.collection("teachers").get()
            .addOnSuccessListener { result ->
                _allTeachers.clear()
                filteredTeachers.clear()
                for (doc in result) {
                    val t = doc.toObject(Teacher::class.java)
                    _allTeachers.add(t)
                    filteredTeachers.add(t)
                }
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

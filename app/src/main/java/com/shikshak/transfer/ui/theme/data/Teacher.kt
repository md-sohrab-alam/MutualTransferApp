package com.shikshak.transfer.ui.theme.data

data class Teacher(
    val uid: String = "",
    val name: String = "",
    val gender: String = "",
    val subject: String = "",
    val post: String = "",
    val district: String = "",
    val block: String = "",
    val schoolName: String = "",
    val preferredDistrict: String = "",
    val preferredBlock: String = "",
    val contact: Contact = Contact(),
    val timestamp: Long? = null,
    // New fields for enhanced form
    val designation: String = "",
    val contactPreference: Boolean = true, // Allow contact: Yes/No
    val willingToMove: Boolean = false, // Willing to move to partner's school
    val preferredDistricts: List<String> = emptyList(), // Multi-select preferred districts
    val preferredBlocks: List<String> = emptyList(), // Multi-select preferred blocks
    val qualification: String = "" // Educational qualification
)

data class Contact(
    val email: String = "",
    val phone: String = ""
)

// Data classes for dropdown options
data class District(
    val name: String,
    val blocks: List<String>
)

data class PostLevel(
    val name: String,
    val requiresSubject: Boolean = false
)

data class Subject(
    val name: String,
    val postLevels: List<String> // Which post levels this subject is available for
)

package com.shikshak.transfer.ui.theme.data

data class TransferRequest(
    val teacherId: String = "",
    val teacherName: String = "",
    val currentDistrict: String = "",
    val currentSchool: String = "",
    val preferredDistricts: List<String> = emptyList(),
    val preferredBlocks: List<String> = emptyList(),
    val postLevel: String = "",
    val designation: String = "",
    val subject: String = "",
    val status: String = "PENDING", // PENDING, MATCHED, COMPLETED, CANCELLED
    val submittedDate: String = "",
    val contactPreference: Boolean = true,
    val notes: String = ""
) 
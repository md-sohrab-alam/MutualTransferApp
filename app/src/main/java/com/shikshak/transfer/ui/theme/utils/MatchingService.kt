package com.shikshak.transfer.ui.theme.utils

import com.shikshak.transfer.ui.theme.data.Teacher
import com.shikshak.transfer.ui.theme.data.TransferRequest
import timber.log.Timber

/**
 * Service for matching teachers for mutual transfer
 */
object MatchingService {
    
    /**
     * Match criteria weights for scoring
     */
    private object MatchWeights {
        const val PERFECT_SUBJECT_MATCH = 100
        const val POST_LEVEL_MATCH = 50
        const val DESIGNATION_MATCH = 30
        const val CONTACT_PREFERENCE_MATCH = 20
        const val WILLING_TO_MOVE = 10
        const val BLOCK_PREFERENCE_MATCH = 15
        const val SAME_DISTRICT_PREFERENCE = 25
    }
    
    /**
     * Check if two teachers are compatible for mutual transfer
     */
    fun isCompatibleMatch(
        currentRequest: TransferRequest,
        otherRequest: TransferRequest,
        otherTeacher: Teacher
    ): Boolean {
        // Basic compatibility checks
        if (currentRequest.teacherId == otherRequest.teacherId) {
            return false // Same teacher
        }
        
        // Check if both teachers want to swap districts
        if (!currentRequest.preferredDistricts.contains(otherRequest.currentDistrict) ||
            !otherRequest.preferredDistricts.contains(currentRequest.currentDistrict)) {
            return false
        }
        // TODO: Temporarily disabled post level and subject compatibility checks
        /*
        // Check post level compatibility
        if (!isPostLevelCompatible(currentRequest.postLevel, otherRequest.postLevel)) {
            return false
        }
        
        // Check subject compatibility for secondary/higher secondary
        if (currentRequest.postLevel in listOf("Secondary", "Higher Secondary") &&
            otherRequest.postLevel in listOf("Secondary", "Higher Secondary")) {
            if (currentRequest.subject != otherRequest.subject) {
                return false // Subject must match for secondary levels
            }
        }
        */
        
        return true
    }
    
    /**
     * Check if two post levels are compatible for transfer
     */
    fun isPostLevelCompatible(level1: String, level2: String): Boolean {
        val compatibleGroups = mapOf(
            "Primary" to listOf("Primary"),
            "Upper Primary" to listOf("Upper Primary"),
            "Secondary" to listOf("Secondary"),
            "Higher Secondary" to listOf("Higher Secondary")
        )
        
        val group1 = compatibleGroups.entries.find { it.value.contains(level1) }?.key
        val group2 = compatibleGroups.entries.find { it.value.contains(level2) }?.key
        
        return group1 == group2
    }
    
    /**
     * Calculate relevance score for a potential match
     */
    fun calculateRelevanceScore(teacher: Teacher, currentRequest: TransferRequest): Int {
        var score = 0
        
        // Perfect subject match (highest priority)
        if (teacher.subject == currentRequest.subject) {
            score += MatchWeights.PERFECT_SUBJECT_MATCH
        }
        
        // Post level match
        if (teacher.post == currentRequest.postLevel) {
            score += MatchWeights.POST_LEVEL_MATCH
        }
        
        // Designation match
        if (teacher.designation == currentRequest.designation) {
            score += MatchWeights.DESIGNATION_MATCH
        }
        
        // Contact preference match
        if (teacher.contactPreference == currentRequest.contactPreference) {
            score += MatchWeights.CONTACT_PREFERENCE_MATCH
        }
        
        // Willing to move (bonus points)
        if (teacher.willingToMove) {
            score += MatchWeights.WILLING_TO_MOVE
        }
        
        // Block preference match
        val commonBlocks = currentRequest.preferredBlocks.intersect(teacher.preferredBlocks.toSet())
        if (commonBlocks.isNotEmpty()) {
            score += MatchWeights.BLOCK_PREFERENCE_MATCH
        }
        
        // Same district preference (if both want same district)
        val commonDistricts = currentRequest.preferredDistricts.intersect(teacher.preferredDistricts.toSet())
        if (commonDistricts.isNotEmpty()) {
            score += MatchWeights.SAME_DISTRICT_PREFERENCE
        }
        
        return score
    }
    
    /**
     * Sort matches by relevance score (better matches first)
     */
    fun sortMatchesByRelevance(
        matches: List<Teacher>,
        currentRequest: TransferRequest
    ): List<Teacher> {
        return matches.sortedByDescending { teacher ->
            calculateRelevanceScore(teacher, currentRequest)
        }
    }
    
    /**
     * Get match quality description
     */
    fun getMatchQualityDescription(score: Int): String {
        return when {
            score >= 150 -> "Perfect Match"
            score >= 100 -> "Excellent Match"
            score >= 70 -> "Good Match"
            score >= 50 -> "Compatible Match"
            else -> "Basic Match"
        }
    }
    
    /**
     * Get match compatibility details
     */
    fun getMatchCompatibilityDetails(
        teacher: Teacher,
        currentRequest: TransferRequest
    ): List<String> {
        val details = mutableListOf<String>()
        
        if (teacher.subject == currentRequest.subject) {
            details.add("✓ Same Subject: ${teacher.subject}")
        }
        
        if (teacher.post == currentRequest.postLevel) {
            details.add("✓ Same Post Level: ${teacher.post}")
        }
        
        if (teacher.designation == currentRequest.designation) {
            details.add("✓ Same Designation: ${teacher.designation}")
        }
        
        if (teacher.contactPreference == currentRequest.contactPreference) {
            details.add("✓ Contact Preference Match")
        }
        
        if (teacher.willingToMove) {
            details.add("✓ Willing to Move")
        }
        
        val commonBlocks = currentRequest.preferredBlocks.intersect(teacher.preferredBlocks.toSet())
        if (commonBlocks.isNotEmpty()) {
            details.add("✓ Common Block Preferences: ${commonBlocks.joinToString(", ")}")
        }
        
        val commonDistricts = currentRequest.preferredDistricts.intersect(teacher.preferredDistricts.toSet())
        if (commonDistricts.isNotEmpty()) {
            details.add("✓ Common District Preferences: ${commonDistricts.joinToString(", ")}")
        }
        
        return details
    }
    
    /**
     * Validate transfer request for matching
     */
    fun validateTransferRequest(request: TransferRequest): List<String> {
        val errors = mutableListOf<String>()
        
        if (request.preferredDistricts.isEmpty()) {
            errors.add("No preferred districts selected")
        }
        
        if (request.postLevel.isEmpty()) {
            errors.add("Post level not specified")
        }
        
        if (request.designation.isEmpty()) {
            errors.add("Designation not specified")
        }
        
        // For secondary levels, subject is required
        if (request.postLevel in listOf("Secondary", "Higher Secondary") && request.subject.isEmpty()) {
            errors.add("Subject is required for Secondary/Higher Secondary levels")
        }
        
        return errors
    }
} 
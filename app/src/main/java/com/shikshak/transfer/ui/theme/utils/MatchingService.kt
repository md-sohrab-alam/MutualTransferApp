package com.shikshak.transfer.ui.theme.utils

import com.shikshak.transfer.ui.theme.data.Teacher
import com.shikshak.transfer.ui.theme.data.TransferRequest
import com.shikshak.transfer.R
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
        // Match if at least one common district and post matches
        val commonDistricts = currentRequest.preferredDistricts.intersect(listOf(otherRequest.currentDistrict)).isNotEmpty() &&
            otherRequest.preferredDistricts.intersect(listOf(currentRequest.currentDistrict)).isNotEmpty()
        val postMatch = isPostCompatible(currentRequest.post, otherRequest.post)
        return commonDistricts && postMatch
    }
    
    /**
     * Check if two post levels are compatible for transfer
     */
    fun isPostCompatible(level1: String, level2: String): Boolean {
        return level1.trim() == level2.trim()
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
        if (teacher.post == currentRequest.post) {
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
    fun getMatchQualityDescription(score: Int, context: android.content.Context): String {
        return when {
            score >= 150 -> context.getString(R.string.perfect_match)
            score >= 100 -> context.getString(R.string.excellent_match)
            score >= 70 -> context.getString(R.string.good_match)
            score >= 50 -> context.getString(R.string.compatible_match)
            else -> context.getString(R.string.basic_match)
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
        // Post match
        if (teacher.post == currentRequest.post && teacher.post.isNotEmpty()) {
            details.add("✓ Same Post Level: ${teacher.post}")
        }
        // Block match
        val commonBlocks = currentRequest.preferredBlocks.intersect(teacher.preferredBlocks.toSet())
        if (commonBlocks.isNotEmpty()) {
            details.add("✓ Common Block Preferences: ${commonBlocks.joinToString(", ")}")
        }
        // Qualification match
        if (teacher.qualification == currentRequest.qualification && teacher.qualification.isNotEmpty()) {
            details.add("✓ Same Qualification: ${teacher.qualification}")
        }
        // Subject match
        if (teacher.subject == currentRequest.subject && teacher.subject.isNotEmpty()) {
            details.add("✓ Same Subject: ${teacher.subject}")
        }
        // Designation match
        if (teacher.designation == currentRequest.designation && teacher.designation.isNotEmpty()) {
            details.add("✓ Same Designation: ${teacher.designation}")
        }
        // Willing to move
        if (teacher.willingToMove) {
            details.add("✓ Willing to Move")
        }
        // District match
        if (teacher.district == currentRequest.currentDistrict || currentRequest.preferredDistricts.contains(teacher.district)) {
            details.add("✓ District matches: ${teacher.district}")
        }
        return details
    }
    
    /**
     * Validate transfer request for matching
     */
    fun validateTransferRequest(request: TransferRequest, context: android.content.Context): List<String> {
        val errors = mutableListOf<String>()
        
        if (request.preferredDistricts.isEmpty()) {
            errors.add(context.getString(R.string.no_preferred_districts_selected))
        }
        
        if (request.post.isEmpty()) {
            errors.add(context.getString(R.string.post_level_not_specified))
        }
        
        if (request.designation.isEmpty()) {
            errors.add(context.getString(R.string.designation_not_specified))
        }
        
        // For secondary levels, subject is required
        if (request.post in listOf("Secondary", "Senior Secondary") && request.subject.isEmpty()) {
            errors.add(context.getString(R.string.subject_required_for_secondary))
        }
        
        return errors
    }
} 
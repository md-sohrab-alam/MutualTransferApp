import { Teacher, TransferRequest } from '../types';

export class MatchingService {
  private static readonly MatchWeights = {
    PERFECT_SUBJECT_MATCH: 100,
    POST_LEVEL_MATCH: 50,
    DESIGNATION_MATCH: 30,
    CONTACT_PREFERENCE_MATCH: 20,
    WILLING_TO_MOVE: 10,
    BLOCK_PREFERENCE_MATCH: 15,
    SAME_DISTRICT_PREFERENCE: 25,
  };

  /**
   * Check if two teachers are compatible for mutual transfer
   */
  static isCompatibleMatch(
    currentRequest: TransferRequest,
    otherRequest: TransferRequest,
    otherTeacher: Teacher
  ): boolean {
    // Basic compatibility checks
    if (currentRequest.teacherId === otherRequest.teacherId) {
      return false; // Same teacher
    }

    // Match if at least one common district and post matches
    const commonDistricts = 
      currentRequest.preferredDistricts.includes(otherRequest.currentDistrict) &&
      otherRequest.preferredDistricts.includes(currentRequest.currentDistrict);
    
    const postMatch = this.isPostCompatible(currentRequest.post, otherRequest.post);
    
    return commonDistricts && postMatch;
  }

  /**
   * Check if two post levels are compatible for transfer
   */
  static isPostCompatible(level1: string, level2: string): boolean {
    return level1.trim() === level2.trim();
  }

  /**
   * Calculate relevance score for a potential match
   */
  static calculateRelevanceScore(teacher: Teacher, currentRequest: TransferRequest): number {
    let score = 0;

    // Perfect subject match (highest priority)
    if (teacher.subject === currentRequest.subject) {
      score += this.MatchWeights.PERFECT_SUBJECT_MATCH;
    }

    // Post level match
    if (teacher.post === currentRequest.post) {
      score += this.MatchWeights.POST_LEVEL_MATCH;
    }

    // Designation match
    if (teacher.designation === currentRequest.designation) {
      score += this.MatchWeights.DESIGNATION_MATCH;
    }

    // Contact preference match
    if (teacher.contactPreference === currentRequest.contactPreference) {
      score += this.MatchWeights.CONTACT_PREFERENCE_MATCH;
    }

    // Willing to move (bonus points)
    if (teacher.willingToMove) {
      score += this.MatchWeights.WILLING_TO_MOVE;
    }

    // Block preference match
    const commonBlocks = currentRequest.preferredBlocks.filter(block => 
      teacher.preferredBlocks.includes(block)
    );
    if (commonBlocks.length > 0) {
      score += this.MatchWeights.BLOCK_PREFERENCE_MATCH;
    }

    // Same district preference (if both want same district)
    const commonDistricts = currentRequest.preferredDistricts.filter(district => 
      teacher.preferredDistricts.includes(district)
    );
    if (commonDistricts.length > 0) {
      score += this.MatchWeights.SAME_DISTRICT_PREFERENCE;
    }

    return score;
  }

  /**
   * Sort matches by relevance score (better matches first)
   */
  static sortMatchesByRelevance(
    matches: Teacher[],
    currentRequest: TransferRequest
  ): Teacher[] {
    return matches.sort((a, b) => {
      const scoreA = this.calculateRelevanceScore(a, currentRequest);
      const scoreB = this.calculateRelevanceScore(b, currentRequest);
      return scoreB - scoreA;
    });
  }

  /**
   * Get match quality description
   */
  static getMatchQualityDescription(score: number): string {
    if (score >= 150) return 'Perfect Match';
    if (score >= 100) return 'Excellent Match';
    if (score >= 70) return 'Good Match';
    if (score >= 50) return 'Compatible Match';
    return 'Basic Match';
  }

  /**
   * Get match compatibility details
   */
  static getMatchCompatibilityDetails(
    teacher: Teacher,
    currentRequest: TransferRequest
  ): string[] {
    const details: string[] = [];

    // Post match
    if (teacher.post === currentRequest.post && teacher.post) {
      details.push(`✓ Same Post Level: ${teacher.post}`);
    }

    // Block match
    const commonBlocks = currentRequest.preferredBlocks.filter(block => 
      teacher.preferredBlocks.includes(block)
    );
    if (commonBlocks.length > 0) {
      details.push(`✓ Common Block Preferences: ${commonBlocks.join(', ')}`);
    }

    // Qualification match
    if (teacher.qualification === currentRequest.qualification && teacher.qualification) {
      details.push(`✓ Same Qualification: ${teacher.qualification}`);
    }

    // Subject match
    if (teacher.subject === currentRequest.subject && teacher.subject) {
      details.push(`✓ Same Subject: ${teacher.subject}`);
    }

    // Designation match
    if (teacher.designation === currentRequest.designation && teacher.designation) {
      details.push(`✓ Same Designation: ${teacher.designation}`);
    }

    // Willing to move
    if (teacher.willingToMove) {
      details.push('✓ Willing to Move');
    }

    // District match
    if (teacher.district === currentRequest.currentDistrict || 
        currentRequest.preferredDistricts.includes(teacher.district)) {
      details.push(`✓ District matches: ${teacher.district}`);
    }

    return details;
  }

  /**
   * Validate transfer request for matching
   */
  static validateTransferRequest(request: TransferRequest): string[] {
    const errors: string[] = [];

    if (request.preferredDistricts.length === 0) {
      errors.push('No preferred districts selected');
    }

    if (!request.post) {
      errors.push('Post level not specified');
    }

    if (!request.designation) {
      errors.push('Designation not specified');
    }

    // For secondary levels, subject is required
    if (['Secondary', 'Senior Secondary'].includes(request.post) && !request.subject) {
      errors.push('Subject is required for secondary levels');
    }

    return errors;
  }
} 
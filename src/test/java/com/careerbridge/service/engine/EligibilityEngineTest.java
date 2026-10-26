package com.careerbridge.service.engine;

import com.careerbridge.entity.JobRequirement;
import com.careerbridge.entity.Student;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Eligibility Engine Rule Verification")
class EligibilityEngineTest {

    private EligibilityEngine eligibilityEngine;

    @BeforeEach
    void setUp() {
        eligibilityEngine = new EligibilityEngine();
    }

    @Test
    @DisplayName("Should return ELIGIBLE when student satisfies all criteria")
    void testEligibleCandidate() {
        Student student = Student.builder()
                .cgpa(8.5)
                .backlogs(0)
                .graduationYear(2026)
                .branch("Computer Science")
                .build();

        JobRequirement req = JobRequirement.builder()
                .minCgpa(7.0)
                .maxBacklogs(0)
                .graduationYear(2026)
                .eligibleBranches("Computer Science, Information Technology")
                .build();

        EligibilityEngine.EligibilityResult result = eligibilityEngine.checkEligibility(student, req);

        assertEquals(EligibilityEngine.EligibilityStatus.ELIGIBLE, result.status);
        assertTrue(result.reasons.stream().anyMatch(r -> r.contains("CGPA requirement met")));
        assertTrue(result.reasons.stream().anyMatch(r -> r.contains("Backlogs requirement met")));
        assertTrue(result.reasons.stream().anyMatch(r -> r.contains("Graduation year met")));
        assertTrue(result.reasons.stream().anyMatch(r -> r.contains("Branch requirement met")));
    }

    @Test
    @DisplayName("Should return NOT_ELIGIBLE when CGPA is below cutoff")
    void testCgpaBelowCutoff() {
        Student student = Student.builder()
                .cgpa(6.2)
                .backlogs(0)
                .graduationYear(2026)
                .branch("Computer Science")
                .build();

        JobRequirement req = JobRequirement.builder()
                .minCgpa(7.5)
                .maxBacklogs(1)
                .graduationYear(2026)
                .eligibleBranches("Computer Science")
                .build();

        EligibilityEngine.EligibilityResult result = eligibilityEngine.checkEligibility(student, req);

        assertEquals(EligibilityEngine.EligibilityStatus.NOT_ELIGIBLE, result.status);
        assertTrue(result.reasons.stream().anyMatch(r -> r.contains("CGPA requirement not met")));
    }

    @Test
    @DisplayName("Should return NOT_ELIGIBLE when backlogs exceed maximum allowed")
    void testBacklogsExceeded() {
        Student student = Student.builder()
                .cgpa(8.0)
                .backlogs(3)
                .graduationYear(2026)
                .branch("Computer Science")
                .build();

        JobRequirement req = JobRequirement.builder()
                .minCgpa(7.0)
                .maxBacklogs(1)
                .graduationYear(2026)
                .eligibleBranches("Computer Science")
                .build();

        EligibilityEngine.EligibilityResult result = eligibilityEngine.checkEligibility(student, req);

        assertEquals(EligibilityEngine.EligibilityStatus.NOT_ELIGIBLE, result.status);
        assertTrue(result.reasons.stream().anyMatch(r -> r.contains("Backlogs exceed maximum allowed")));
    }

    @Test
    @DisplayName("Should return NOT_ELIGIBLE when branch is not in eligible branches list")
    void testBranchMismatch() {
        Student student = Student.builder()
                .cgpa(8.5)
                .backlogs(0)
                .graduationYear(2026)
                .branch("Civil Engineering")
                .build();

        JobRequirement req = JobRequirement.builder()
                .minCgpa(7.0)
                .maxBacklogs(0)
                .graduationYear(2026)
                .eligibleBranches("Computer Science, Information Technology")
                .build();

        EligibilityEngine.EligibilityResult result = eligibilityEngine.checkEligibility(student, req);

        assertEquals(EligibilityEngine.EligibilityStatus.NOT_ELIGIBLE, result.status);
        assertTrue(result.reasons.stream().anyMatch(r -> r.contains("Branch not eligible")));
    }

    @Test
    @DisplayName("Should return UNCLEAR when job requirements are null")
    void testNullRequirements() {
        Student student = Student.builder().cgpa(8.0).build();
        EligibilityEngine.EligibilityResult result = eligibilityEngine.checkEligibility(student, null);

        assertEquals(EligibilityEngine.EligibilityStatus.UNCLEAR, result.status);
        assertEquals(1, result.reasons.size());
    }
}

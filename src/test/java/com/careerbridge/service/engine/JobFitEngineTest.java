package com.careerbridge.service.engine;

import com.careerbridge.entity.JobRequirement;
import com.careerbridge.entity.Skill;
import com.careerbridge.entity.Student;
import com.careerbridge.entity.StudentSkill;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Job Fit Scoring Engine Verification")
class JobFitEngineTest {

    private JobFitEngine jobFitEngine;

    @BeforeEach
    void setUp() {
        jobFitEngine = new JobFitEngine();
    }

    @Test
    @DisplayName("Should accurately calculate multi-dimensional weighted job fit score")
    void testCalculateJobFitWeightedScores() {
        Student student = Student.builder()
                .cgpa(9.0)
                .branch("Computer Science")
                .build();

        Skill javaSkill = Skill.builder().name("Java").build();
        Skill springSkill = Skill.builder().name("Spring Boot").build();
        Skill sqlSkill = Skill.builder().name("SQL").build();

        List<StudentSkill> skills = List.of(
                StudentSkill.builder().skill(javaSkill).proficiencyLevel("ADVANCED").build(),
                StudentSkill.builder().skill(springSkill).proficiencyLevel("ADVANCED").build(),
                StudentSkill.builder().skill(sqlSkill).proficiencyLevel("INTERMEDIATE").build()
        );

        JobRequirement req = JobRequirement.builder()
                .requiredSkills("Java, Spring Boot, SQL, Docker")
                .eligibleBranches("Computer Science, IT")
                .experienceRequirement(0)
                .build();

        JobFitEngine.JobFitResult result = jobFitEngine.calculateJobFit(student, skills, req);

        assertNotNull(result);
        assertTrue(result.overallScore > 0 && result.overallScore <= 100);
        assertTrue(result.technicalSkillsScore >= 40.0 && result.technicalSkillsScore <= 100.0);
        assertEquals(90.0, result.educationScore, 0.1);
        assertEquals(95.0, result.keywordsScore, 0.1);
        assertEquals(90.0, result.experienceScore, 0.1);

        double expectedOverall = (result.technicalSkillsScore * 0.40) +
                (result.educationScore * 0.20) +
                (result.projectsScore * 0.15) +
                (result.experienceScore * 0.15) +
                (result.keywordsScore * 0.10);

        assertEquals(Math.round(expectedOverall * 10.0) / 10.0, result.overallScore, 0.1);
    }

    @Test
    @DisplayName("Should return fallback score 50.0 when requirement is null")
    void testNullRequirementFallback() {
        Student student = Student.builder().cgpa(8.0).build();
        JobFitEngine.JobFitResult result = jobFitEngine.calculateJobFit(student, List.of(), null);

        assertEquals(50.0, result.overallScore);
    }
}

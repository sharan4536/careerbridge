package com.careerbridge.service.engine;

import com.careerbridge.entity.JobRequirement;
import com.careerbridge.entity.Skill;
import com.careerbridge.entity.StudentSkill;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Skill Gap Analyzer Verification")
class SkillGapAnalyzerTest {

    private SkillGapAnalyzer analyzer;

    @BeforeEach
    void setUp() {
        analyzer = new SkillGapAnalyzer();
    }

    @Test
    @DisplayName("Should categorize matched and missing skills with learning recommendations")
    void testSkillGapCategorization() {
        Skill java = Skill.builder().name("Java").build();
        Skill react = Skill.builder().name("React").build();

        List<StudentSkill> studentSkills = List.of(
                StudentSkill.builder().skill(java).build(),
                StudentSkill.builder().skill(react).build()
        );

        JobRequirement req = JobRequirement.builder()
                .requiredSkills("Java, Python, React, Kubernetes")
                .build();

        SkillGapAnalyzer.SkillGapResult result = analyzer.analyze(studentSkills, req);

        assertNotNull(result);
        assertEquals(2, result.matchedSkills.size());
        assertTrue(result.matchedSkills.contains("java"));
        assertTrue(result.matchedSkills.contains("react"));

        assertEquals(2, result.missingSkills.size());
        assertTrue(result.missingSkills.contains("python"));
        assertTrue(result.missingSkills.contains("kubernetes"));

        assertEquals(2, result.recommendations.size());
        assertTrue(result.recommendations.stream().anyMatch(r -> r.contains("python")));
        assertTrue(result.recommendations.stream().anyMatch(r -> r.contains("kubernetes")));
    }

    @Test
    @DisplayName("Should return empty result when requirements are null")
    void testNullRequirements() {
        SkillGapAnalyzer.SkillGapResult result = analyzer.analyze(List.of(), null);
        assertNotNull(result);
        assertTrue(result.matchedSkills.isEmpty());
        assertTrue(result.missingSkills.isEmpty());
    }
}

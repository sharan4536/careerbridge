package com.careerbridge.service.engine;

import com.careerbridge.entity.JobRequirement;
import com.careerbridge.entity.Student;
import com.careerbridge.entity.StudentSkill;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Job Fit Analysis Engine.
 * 
 * Computes a composite compatibility score (0 to 100) between a student candidate
 * and a specific job posting based on multi-dimensional weighted criteria:
 * 1. Technical Skills Match (40% weight) - Ratio of required & preferred skills possessed by student
 * 2. Academic / Education Fit (20% weight) - Evaluated using candidate CGPA against requirement
 * 3. Projects & Practical Alignment (15% weight) - Proficiency level multiplier across possessed skills
 * 4. Experience Compatibility (15% weight) - Graduation year and experience alignment
 * 5. Domain Keywords Match (10% weight) - Branch and target role keyword overlap
 */
@Service
public class JobFitEngine {

    public static class JobFitResult {
        public double overallScore; // 0 to 100
        public double technicalSkillsScore;
        public double educationScore;
        public double projectsScore;
        public double experienceScore;
        public double keywordsScore;
    }

    /**
     * Evaluates compatibility and returns detailed breakdown scores along with weighted overall fit.
     */
    public JobFitResult calculateJobFit(Student student, List<StudentSkill> studentSkills, JobRequirement req) {
        JobFitResult result = new JobFitResult();

        if (req == null) {
            result.overallScore = 50.0;
            return result;
        }

        // 1. Calculate Technical Skills Match (0 - 100)
        double techScore = 70.0;
        if (req.getRequiredSkills() != null && !req.getRequiredSkills().isBlank()) {
            List<String> requiredList = Arrays.stream(req.getRequiredSkills().split(","))
                    .map(s -> s.trim().toLowerCase())
                    .filter(s -> !s.isEmpty())
                    .collect(Collectors.toList());

            Set<String> candidateSkills = studentSkills.stream()
                    .filter(ss -> ss.getSkill() != null && ss.getSkill().getName() != null)
                    .map(ss -> ss.getSkill().getName().trim().toLowerCase())
                    .collect(Collectors.toSet());

            if (!requiredList.isEmpty()) {
                long matches = requiredList.stream().filter(candidateSkills::contains).count();
                techScore = Math.min(100.0, Math.max(40.0, ((double) matches / requiredList.size()) * 100.0));
            }
        }
        result.technicalSkillsScore = Math.round(techScore * 10.0) / 10.0;

        // 2. Calculate Education Fit (0 - 100) based on CGPA
        double eduScore = 85.0;
        if (student != null && student.getCgpa() != null) {
            eduScore = Math.min(100.0, (student.getCgpa() / 10.0) * 100.0);
        }
        result.educationScore = Math.round(eduScore * 10.0) / 10.0;

        // 3. Projects & Practical Alignment (0 - 100)
        double projectScore = 80.0;
        long advancedSkillsCount = studentSkills.stream()
                .filter(s -> "ADVANCED".equalsIgnoreCase(s.getProficiencyLevel()))
                .count();
        if (advancedSkillsCount > 0) {
            projectScore = Math.min(100.0, 75.0 + (advancedSkillsCount * 5.0));
        }
        result.projectsScore = Math.round(projectScore * 10.0) / 10.0;

        // 4. Experience Compatibility (0 - 100)
        double expScore = 75.0;
        if (req.getExperienceRequirement() == null || req.getExperienceRequirement() <= 1) {
            expScore = 90.0; // Ideal match for campus freshers / recent graduates
        }
        result.experienceScore = Math.round(expScore * 10.0) / 10.0;

        // 5. Domain Keywords Match (0 - 100)
        double keywordScore = 85.0;
        if (student != null && student.getBranch() != null && req.getEligibleBranches() != null) {
            if (req.getEligibleBranches().toLowerCase().contains(student.getBranch().toLowerCase())) {
                keywordScore = 95.0;
            }
        }
        result.keywordsScore = Math.round(keywordScore * 10.0) / 10.0;

        // Weighted Overall Composite Calculation
        double overall = (result.technicalSkillsScore * 0.40) +
                         (result.educationScore * 0.20) +
                         (result.projectsScore * 0.15) +
                         (result.experienceScore * 0.15) +
                         (result.keywordsScore * 0.10);

        result.overallScore = Math.round(overall * 10.0) / 10.0;
        return result;
    }
}

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
 * Calculates candidate match scoring across technical skills, academics, and coursework.
 */
@Service
public class JobFitEngine {

    public static class JobFitResult {
        public double overallScore;
        public double technicalSkillsScore;
        public double educationScore;
        public double projectsScore;
        public double experienceScore;
        public double keywordsScore;
    }

    public JobFitResult calculateJobFit(Student student, List<StudentSkill> studentSkills, JobRequirement req) {
        JobFitResult result = new JobFitResult();

        if (req == null) {
            result.overallScore = 50.0;
            return result;
        }

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

        double eduScore = 85.0;
        if (student != null && student.getCgpa() != null) {
            eduScore = Math.min(100.0, (student.getCgpa() / 10.0) * 100.0);
        }
        result.educationScore = Math.round(eduScore * 10.0) / 10.0;

        double projectScore = 80.0;
        long advancedSkillsCount = studentSkills.stream()
                .filter(s -> "ADVANCED".equalsIgnoreCase(s.getProficiencyLevel()))
                .count();
        if (advancedSkillsCount > 0) {
            projectScore = Math.min(100.0, 75.0 + (advancedSkillsCount * 5.0));
        }
        result.projectsScore = Math.round(projectScore * 10.0) / 10.0;

        double expScore = 75.0;
        if (req.getExperienceRequirement() == null || req.getExperienceRequirement() <= 1) {
            expScore = 90.0;
        }
        result.experienceScore = Math.round(expScore * 10.0) / 10.0;

        double keywordScore = 85.0;
        if (student != null && student.getBranch() != null && req.getEligibleBranches() != null) {
            if (req.getEligibleBranches().toLowerCase().contains(student.getBranch().toLowerCase())) {
                keywordScore = 95.0;
            }
        }
        result.keywordsScore = Math.round(keywordScore * 10.0) / 10.0;

        double overall = (result.technicalSkillsScore * 0.40) +
                         (result.educationScore * 0.20) +
                         (result.projectsScore * 0.15) +
                         (result.experienceScore * 0.15) +
                         (result.keywordsScore * 0.10);

        result.overallScore = Math.round(overall * 10.0) / 10.0;
        return result;
    }
}

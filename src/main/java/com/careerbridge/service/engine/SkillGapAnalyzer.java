package com.careerbridge.service.engine;

import com.careerbridge.entity.JobRequirement;
import com.careerbridge.entity.StudentSkill;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Skill Gap Analysis Engine.
 * 
 * Compares skills required by an employer with skills registered in a student's profile.
 * Identifies:
 * 1. Matched skills already possessed by the candidate
 * 2. Missing prerequisite technical skills
 * 3. Personalized learning recommendations to bridge identified gaps
 */
@Service
public class SkillGapAnalyzer {

    public static class SkillGapResult {
        public List<String> matchedSkills = new ArrayList<>();
        public List<String> missingSkills = new ArrayList<>();
        public List<String> partialSkills = new ArrayList<>();
        public List<String> recommendations = new ArrayList<>();
    }

    /**
     * Performs skill intersection and difference analysis between student profile and job posting.
     */
    public SkillGapResult analyze(List<StudentSkill> studentSkills, JobRequirement req) {
        SkillGapResult result = new SkillGapResult();
        if (req == null || req.getRequiredSkills() == null) return result;

        List<String> required = Arrays.stream(req.getRequiredSkills().split(","))
                .map(String::trim).map(String::toLowerCase).collect(Collectors.toList());

        List<String> studentHas = studentSkills.stream()
                .map(ss -> ss.getSkill().getName().toLowerCase().trim())
                .collect(Collectors.toList());

        for (String reqSkill : required) {
            if (studentHas.contains(reqSkill)) {
                result.matchedSkills.add(reqSkill);
            } else {
                result.missingSkills.add(reqSkill);
                result.recommendations.add("Consider learning " + reqSkill + " through a certification or project.");
            }
        }
        
        return result;
    }
}

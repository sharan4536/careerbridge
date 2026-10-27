package com.careerbridge.service.engine;

import com.careerbridge.entity.JobRequirement;
import com.careerbridge.entity.StudentSkill;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Analyzes candidate skill sets against job posting requirements to identify matched and missing competencies.
 */
@Service
public class SkillGapAnalyzer {

    public static class SkillGapResult {
        public List<String> matchedSkills = new ArrayList<>();
        public List<String> missingSkills = new ArrayList<>();
        public List<String> partialSkills = new ArrayList<>();
        public List<String> recommendations = new ArrayList<>();
    }

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

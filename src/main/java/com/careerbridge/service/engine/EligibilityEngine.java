package com.careerbridge.service.engine;

import com.careerbridge.entity.JobRequirement;
import com.careerbridge.entity.Student;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Evaluates candidate eligibility based on academic benchmarks (CGPA, backlogs, branch, graduation year).
 */
@Service
public class EligibilityEngine {

    public enum EligibilityStatus {
        ELIGIBLE, NOT_ELIGIBLE, UNCLEAR
    }

    public static class EligibilityResult {
        public EligibilityStatus status;
        public List<String> reasons = new ArrayList<>();
    }

    public EligibilityResult checkEligibility(Student student, JobRequirement req) {
        EligibilityResult result = new EligibilityResult();
        boolean eligible = true;

        if (req == null) {
            result.status = EligibilityStatus.UNCLEAR;
            result.reasons.add("No job requirements specified.");
            return result;
        }

        if (req.getMinCgpa() != null && student.getCgpa() != null) {
            if (student.getCgpa() < req.getMinCgpa()) {
                eligible = false;
                result.reasons.add("CGPA requirement not met. Required: " + req.getMinCgpa() + "+, Student: " + student.getCgpa());
            } else {
                result.reasons.add("CGPA requirement met.");
            }
        }

        if (req.getMaxBacklogs() != null && student.getBacklogs() != null) {
            if (student.getBacklogs() > req.getMaxBacklogs()) {
                eligible = false;
                result.reasons.add("Backlogs exceed maximum allowed. Allowed: " + req.getMaxBacklogs() + ", Student: " + student.getBacklogs());
            } else {
                result.reasons.add("Backlogs requirement met.");
            }
        }

        if (req.getGraduationYear() != null && student.getGraduationYear() != null) {
            if (!req.getGraduationYear().equals(student.getGraduationYear())) {
                eligible = false;
                result.reasons.add("Graduation year mismatch. Required: " + req.getGraduationYear() + ", Student: " + student.getGraduationYear());
            } else {
                result.reasons.add("Graduation year met.");
            }
        }

        if (req.getEligibleBranches() != null && !req.getEligibleBranches().isEmpty() && student.getBranch() != null) {
            if (!req.getEligibleBranches().contains(student.getBranch())) {
                eligible = false;
                result.reasons.add("Branch not eligible. Required one of: " + req.getEligibleBranches() + ", Student: " + student.getBranch());
            } else {
                result.reasons.add("Branch requirement met.");
            }
        }

        result.status = eligible ? EligibilityStatus.ELIGIBLE : EligibilityStatus.NOT_ELIGIBLE;
        return result;
    }
}

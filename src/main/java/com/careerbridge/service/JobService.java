package com.careerbridge.service;

import com.careerbridge.dto.response.JobResponse;
import com.careerbridge.service.engine.EligibilityEngine.EligibilityResult;
import com.careerbridge.service.engine.JobFitEngine.JobFitResult;
import com.careerbridge.service.engine.SkillGapAnalyzer.SkillGapResult;

import java.util.List;

import com.careerbridge.dto.request.JobRequest;

public interface JobService {
    List<JobResponse> getAllJobs();
    JobResponse createJob(String email, JobRequest request);
    EligibilityResult checkEligibility(String email, Long jobId);
    JobFitResult calculateJobFit(String email, Long jobId);
    SkillGapResult analyzeSkillGap(String email, Long jobId);
}

package com.careerbridge.service;

import com.careerbridge.dto.request.ApplicationRequest;
import com.careerbridge.entity.Job;
import com.careerbridge.repository.JobRepository;
import com.careerbridge.service.engine.EligibilityEngine;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Service orchestrating batch auto-application for eligible job postings.
 */
@Service
public class AutoApplyService {

    private final JobRepository jobRepository;
    private final JobService jobService;
    private final ApplicationService applicationService;

    public AutoApplyService(JobRepository jobRepository,
                            JobService jobService,
                            ApplicationService applicationService) {
        this.jobRepository = jobRepository;
        this.jobService = jobService;
        this.applicationService = applicationService;
    }

    public List<String> runAutoApply(String email, Long defaultResumeVersionId) {
        List<String> results = new ArrayList<>();
        List<Job> allJobs = jobRepository.findAll();
        
        for (Job job : allJobs) {
            try {
                EligibilityEngine.EligibilityResult eligibility = jobService.checkEligibility(email, job.getId());
                if (eligibility.status == EligibilityEngine.EligibilityStatus.ELIGIBLE) {
                    ApplicationRequest req = new ApplicationRequest();
                    req.setJobId(job.getId());
                    req.setResumeVersionId(defaultResumeVersionId);
                    applicationService.submitApplication(email, req);
                    results.add("Successfully auto-applied to job: " + job.getRole() + " at " + job.getCompany().getName());
                } else {
                    results.add("Skipped job " + job.getId() + " due to eligibility.");
                }
            } catch (Exception e) {
                results.add("Failed to auto-apply to job " + job.getId() + ": " + e.getMessage());
            }
        }
        return results;
    }

    @org.springframework.scheduling.annotation.Async("taskExecutor")
    public java.util.concurrent.CompletableFuture<List<String>> runAutoApplyAsync(String email, Long defaultResumeVersionId) {
        List<String> results = runAutoApply(email, defaultResumeVersionId);
        return java.util.concurrent.CompletableFuture.completedFuture(results);
    }
}

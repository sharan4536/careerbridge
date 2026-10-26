package com.careerbridge.service;

import com.careerbridge.dto.request.ApplicationRequest;
import com.careerbridge.entity.Job;
import com.careerbridge.repository.JobRepository;
import com.careerbridge.service.engine.EligibilityEngine;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Auto-Apply Automation Service.
 * 
 * Implements an intelligent job application workflow for students:
 * 1. Iterates across all active open jobs
 * 2. Evaluates candidate eligibility using {@link EligibilityEngine}
 * 3. Automatically submits applications for eligible roles with a specified resume version
 * 4. Generates an execution report summarizing successful applications and skipped postings
 */
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AutoApplyService {

    private final JobRepository jobRepository;
    private final JobService jobService;
    private final ApplicationService applicationService;

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

    /**
     * Multithreading Feature: Asynchronous Auto-Apply Execution.
     * 
     * Uses Spring's @Async annotation and Java's CompletableFuture to execute the batch
     * job matching and application submission on a background worker thread managed by taskExecutor.
     * 
     * Benefits:
     * 1. Non-blocking I/O: Web server threads remain free to handle other student requests.
     * 2. Concurrency: Multiple students can trigger auto-apply simultaneously across different worker threads.
     * 
     * @param email Candidate student email
     * @param defaultResumeVersionId Selected resume version
     * @return CompletableFuture holding the list of application results
     */
    @org.springframework.scheduling.annotation.Async("taskExecutor")
    public java.util.concurrent.CompletableFuture<List<String>> runAutoApplyAsync(String email, Long defaultResumeVersionId) {
        List<String> results = runAutoApply(email, defaultResumeVersionId);
        return java.util.concurrent.CompletableFuture.completedFuture(results);
    }
}

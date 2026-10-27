package com.careerbridge.controller;

import com.careerbridge.dto.request.JobRequest;
import com.careerbridge.dto.response.JobResponse;
import com.careerbridge.service.JobService;
import com.careerbridge.service.engine.EligibilityEngine;
import com.careerbridge.service.engine.JobFitEngine;
import com.careerbridge.service.engine.SkillGapAnalyzer;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/jobs")
public class JobController {

    private final JobService jobService;

    public JobController(JobService jobService) {
        this.jobService = jobService;
    }

    @GetMapping
    public ResponseEntity<List<JobResponse>> getAllJobs() {
        return ResponseEntity.ok(jobService.getAllJobs());
    }

    @PostMapping
    @PreAuthorize("hasRole('RECRUITER') or hasRole('ADMIN')")
    public ResponseEntity<JobResponse> createJob(Authentication authentication, @RequestBody JobRequest request) {
        String email = authentication.getName();
        return new ResponseEntity<>(jobService.createJob(email, request), HttpStatus.CREATED);
    }

    @PostMapping("/{id}/eligibility-check")
    public ResponseEntity<EligibilityEngine.EligibilityResult> checkEligibility(Authentication authentication, @PathVariable Long id) {
        String email = authentication.getName();
        return ResponseEntity.ok(jobService.checkEligibility(email, id));
    }

    @PostMapping("/{id}/jobfit")
    public ResponseEntity<JobFitEngine.JobFitResult> calculateJobFit(Authentication authentication, @PathVariable Long id) {
        String email = authentication.getName();
        return ResponseEntity.ok(jobService.calculateJobFit(email, id));
    }
    
    @PostMapping("/{id}/skill-gap")
    public ResponseEntity<SkillGapAnalyzer.SkillGapResult> analyzeSkillGap(Authentication authentication, @PathVariable Long id) {
        String email = authentication.getName();
        return ResponseEntity.ok(jobService.analyzeSkillGap(email, id));
    }
}

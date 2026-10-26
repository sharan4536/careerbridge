package com.careerbridge.controller;

import com.careerbridge.dto.response.JobResponse;
import com.careerbridge.service.JobService;
import com.careerbridge.service.engine.EligibilityEngine;
import com.careerbridge.service.engine.JobFitEngine;
import com.careerbridge.service.engine.SkillGapAnalyzer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/jobs")
@RequiredArgsConstructor
public class JobController {

    private final JobService jobService;

    @GetMapping
    public ResponseEntity<List<JobResponse>> getAllJobs() {
        return ResponseEntity.ok(jobService.getAllJobs());
    }

    @PostMapping
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('RECRUITER') or hasRole('ADMIN')")
    public ResponseEntity<JobResponse> createJob(Authentication authentication, @RequestBody com.careerbridge.dto.request.JobRequest request) {
        String email = authentication.getName();
        return ResponseEntity.ok(jobService.createJob(email, request));
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

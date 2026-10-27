package com.careerbridge.controller;

import com.careerbridge.dto.request.ApplicationRequest;
import com.careerbridge.dto.response.ApplicationEventResponse;
import com.careerbridge.dto.response.ApplicationResponse;
import com.careerbridge.service.ApplicationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/applications")
public class ApplicationController {

    private final ApplicationService applicationService;

    public ApplicationController(ApplicationService applicationService) {
        this.applicationService = applicationService;
    }

    @PostMapping
    public ResponseEntity<ApplicationResponse> submitApplication(Authentication authentication, @RequestBody ApplicationRequest request) {
        String email = authentication.getName();
        return new ResponseEntity<>(applicationService.submitApplication(email, request), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<ApplicationResponse>> getMyApplications(Authentication authentication) {
        String email = authentication.getName();
        return ResponseEntity.ok(applicationService.getMyApplications(email));
    }

    @GetMapping("/job/{jobId}")
    public ResponseEntity<List<ApplicationResponse>> getApplicationsByJob(Authentication authentication, @PathVariable Long jobId) {
        String email = authentication.getName();
        return ResponseEntity.ok(applicationService.getApplicationsByJobId(email, jobId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApplicationResponse> getApplicationDetails(Authentication authentication, @PathVariable Long id) {
        String email = authentication.getName();
        return ResponseEntity.ok(applicationService.getApplicationDetails(email, id));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<ApplicationResponse> updateStatus(Authentication authentication, @PathVariable Long id, @RequestParam String status) {
        String email = authentication.getName();
        return ResponseEntity.ok(applicationService.updateStatus(email, id, status));
    }

    @GetMapping("/{id}/timeline")
    public ResponseEntity<List<ApplicationEventResponse>> getApplicationTimeline(Authentication authentication, @PathVariable Long id) {
        String email = authentication.getName();
        return ResponseEntity.ok(applicationService.getApplicationTimeline(email, id));
    }
}

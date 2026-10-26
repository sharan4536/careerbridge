package com.careerbridge.controller;

import com.careerbridge.service.AIService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AIController {

    private final AIService aiService;

    @GetMapping("/resume/{versionId}/analyze")
    public ResponseEntity<Map<String, Object>> analyzeResume(Authentication authentication, @PathVariable Long versionId) {
        return ResponseEntity.ok(aiService.analyzeResume(versionId));
    }

    @GetMapping("/job/{jobId}/interview-prep")
    public ResponseEntity<Map<String, Object>> generateInterviewQuestions(Authentication authentication, @PathVariable Long jobId) {
        return ResponseEntity.ok(aiService.generateInterviewQuestions(jobId));
    }

    @PostMapping("/cover-letter")
    public ResponseEntity<Map<String, Object>> generateCoverLetter(
            Authentication authentication, 
            @RequestBody Map<String, String> request) {
        String username = authentication != null ? authentication.getName() : "Candidate";
        String description = request.getOrDefault("jobDescription", "");
        String role = request.getOrDefault("targetRole", "");
        return ResponseEntity.ok(aiService.generateCoverLetter(username, description, role));
    }
}

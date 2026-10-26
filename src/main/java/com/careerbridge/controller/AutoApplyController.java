package com.careerbridge.controller;

import com.careerbridge.service.AutoApplyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/auto-apply")
@RequiredArgsConstructor
public class AutoApplyController {

    private final AutoApplyService autoApplyService;

    @PostMapping("/run")
    public ResponseEntity<List<String>> runAutoApply(Authentication authentication, @RequestParam Long defaultResumeVersionId) {
        String email = authentication.getName();
        return ResponseEntity.ok(autoApplyService.runAutoApply(email, defaultResumeVersionId));
    }

    /**
     * Multithreading REST Endpoint:
     * Returns a CompletableFuture to execute the auto-apply batch asynchronously.
     */
    @PostMapping("/run-async")
    public java.util.concurrent.CompletableFuture<ResponseEntity<List<String>>> runAutoApplyAsync(
            Authentication authentication, 
            @RequestParam Long defaultResumeVersionId) {
        String email = authentication.getName();
        return autoApplyService.runAutoApplyAsync(email, defaultResumeVersionId)
                .thenApply(ResponseEntity::ok);
    }
}

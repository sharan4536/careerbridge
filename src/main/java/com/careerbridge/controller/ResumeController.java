package com.careerbridge.controller;

import com.careerbridge.dto.response.ResumeResponse;
import com.careerbridge.dto.response.ResumeVersionResponse;
import com.careerbridge.service.ResumeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/resumes")
@RequiredArgsConstructor
public class ResumeController {

    private final ResumeService resumeService;

    @PostMapping
    public ResponseEntity<ResumeResponse> createResume(Authentication authentication, @RequestParam String name) {
        String email = authentication.getName();
        return new ResponseEntity<>(resumeService.createResume(email, name), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<ResumeResponse>> getAllResumes(Authentication authentication) {
        String email = authentication.getName();
        return ResponseEntity.ok(resumeService.getAllResumes(email));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ResumeResponse> getResume(Authentication authentication, @PathVariable Long id) {
        String email = authentication.getName();
        return ResponseEntity.ok(resumeService.getResume(email, id));
    }

    @PostMapping("/{id}/versions")
    public ResponseEntity<ResumeVersionResponse> addVersion(
            Authentication authentication,
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file,
            @RequestParam(required = false) String targetRole,
            @RequestParam(required = false) String skillsRepresented,
            @RequestParam(required = false) String description) {
        
        String email = authentication.getName();
        return new ResponseEntity<>(resumeService.addVersion(email, id, file, targetRole, skillsRepresented, description), HttpStatus.CREATED);
    }

    @GetMapping("/{id}/versions")
    public ResponseEntity<List<ResumeVersionResponse>> getVersions(Authentication authentication, @PathVariable Long id) {
        String email = authentication.getName();
        return ResponseEntity.ok(resumeService.getResumeVersions(email, id));
    }

    @GetMapping("/versions/{versionId}/download")
    public ResponseEntity<byte[]> downloadVersion(Authentication authentication, @PathVariable Long versionId) {
        String email = authentication.getName();
        byte[] fileData = resumeService.downloadResumeVersionFile(email, versionId);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("attachment", "resume.pdf");
        
        return new ResponseEntity<>(fileData, headers, HttpStatus.OK);
    }
}

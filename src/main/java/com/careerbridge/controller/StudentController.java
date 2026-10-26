package com.careerbridge.controller;

import com.careerbridge.dto.request.StudentProfileUpdateRequest;
import com.careerbridge.dto.response.StudentProfileDto;
import com.careerbridge.service.StudentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/students/profile")
@RequiredArgsConstructor
public class StudentController {

    private final StudentService studentService;

    @GetMapping
    public ResponseEntity<StudentProfileDto> getProfile(Authentication authentication) {
        String email = authentication.getName();
        return ResponseEntity.ok(studentService.getProfile(email));
    }

    @PutMapping
    public ResponseEntity<StudentProfileDto> updateProfile(
            Authentication authentication, 
            @RequestBody StudentProfileUpdateRequest updateRequest) {
        String email = authentication.getName();
        return ResponseEntity.ok(studentService.updateProfile(email, updateRequest));
    }
}

package com.careerbridge.service;

import com.careerbridge.dto.request.StudentProfileUpdateRequest;
import com.careerbridge.dto.response.StudentProfileDto;

/**
 * Service contract for student profile retrieval and modification operations.
 */
public interface StudentService {
    StudentProfileDto getProfile(String email);
    StudentProfileDto updateProfile(String email, StudentProfileUpdateRequest updateRequest);
}

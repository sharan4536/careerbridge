package com.careerbridge.service;

import com.careerbridge.dto.request.StudentProfileUpdateRequest;
import com.careerbridge.dto.response.StudentProfileDto;

public interface StudentService {
    StudentProfileDto getProfile(String email);
    StudentProfileDto updateProfile(String email, StudentProfileUpdateRequest updateRequest);
}

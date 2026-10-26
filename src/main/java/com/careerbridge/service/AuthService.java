package com.careerbridge.service;

import com.careerbridge.dto.request.LoginRequest;
import com.careerbridge.dto.request.RegisterRequest;
import com.careerbridge.dto.response.JwtAuthenticationResponse;

public interface AuthService {
    JwtAuthenticationResponse login(LoginRequest loginRequest);
    String register(RegisterRequest registerRequest);
}

package com.careerbridge.service.impl;

import com.careerbridge.dto.request.LoginRequest;
import com.careerbridge.dto.request.RegisterRequest;
import com.careerbridge.dto.response.JwtAuthenticationResponse;
import com.careerbridge.entity.Role;
import com.careerbridge.entity.Student;
import com.careerbridge.entity.User;
import com.careerbridge.repository.StudentRepository;
import com.careerbridge.repository.UserRepository;
import com.careerbridge.security.JwtTokenProvider;
import com.careerbridge.service.AuthService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final StudentRepository studentRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;

    public AuthServiceImpl(AuthenticationManager authenticationManager,
                           UserRepository userRepository,
                           StudentRepository studentRepository,
                           PasswordEncoder passwordEncoder,
                           JwtTokenProvider tokenProvider) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.studentRepository = studentRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenProvider = tokenProvider;
    }

    @Override
    public JwtAuthenticationResponse login(LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getEmail(),
                        loginRequest.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = tokenProvider.generateToken(authentication);
        return new JwtAuthenticationResponse(jwt);
    }

    @Override
    @Transactional
    public String register(RegisterRequest registerRequest) {
        if (userRepository.existsByEmail(registerRequest.getEmail())) {
            throw new RuntimeException("Email is already taken!");
        }

        Role requestedRole = Role.STUDENT;
        if (registerRequest.getRole() != null) {
            try {
                requestedRole = Role.valueOf(registerRequest.getRole().toUpperCase());
            } catch (Exception e) {
                // Default to STUDENT if invalid
            }
        }

        // Creating user's account
        User user = User.builder()
                .email(registerRequest.getEmail())
                .password(passwordEncoder.encode(registerRequest.getPassword()))
                .role(requestedRole)
                .build();

        User savedUser = userRepository.save(user);

        // Creating student profile only if role is STUDENT
        if (requestedRole == Role.STUDENT) {
            Student student = Student.builder()
                    .user(savedUser)
                    .name(registerRequest.getName())
                    .college(registerRequest.getCollege())
                    .branch(registerRequest.getBranch())
                    .graduationYear(registerRequest.getGraduationYear())
                    .cgpa(registerRequest.getCgpa())
                    .build();
            studentRepository.save(student);
        }

        return "User registered successfully";
    }
}

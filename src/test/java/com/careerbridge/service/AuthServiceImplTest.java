package com.careerbridge.service;

import com.careerbridge.dto.request.RegisterRequest;
import com.careerbridge.entity.Role;
import com.careerbridge.entity.Student;
import com.careerbridge.entity.User;
import com.careerbridge.repository.StudentRepository;
import com.careerbridge.repository.UserRepository;
import com.careerbridge.security.JwtTokenProvider;
import com.careerbridge.service.impl.AuthServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Authentication & Registration Service Verification")
class AuthServiceImplTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private UserRepository userRepository;

    @Mock
    private StudentRepository studentRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider tokenProvider;

    private AuthServiceImpl authService;

    @BeforeEach
    void setUp() {
        authService = new AuthServiceImpl(
                authenticationManager,
                userRepository,
                studentRepository,
                passwordEncoder,
                tokenProvider
        );
    }

    @Test
    @DisplayName("Should throw RuntimeException if registration email already exists")
    void testRegisterDuplicateEmail() {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("student@careerbridge.com");
        request.setPassword("secret123");
        request.setName("Test Student");

        when(userRepository.existsByEmail("student@careerbridge.com")).thenReturn(true);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> authService.register(request));
        assertEquals("Email is already taken!", exception.getMessage());
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should save User and Student profile when role is STUDENT")
    void testRegisterStudentSuccess() {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("newstudent@careerbridge.com");
        request.setPassword("plainPassword");
        request.setName("Jane Doe");
        request.setRole("STUDENT");
        request.setCollege("MIT");
        request.setBranch("CS");
        request.setGraduationYear(2026);
        request.setCgpa(9.2);

        when(userRepository.existsByEmail("newstudent@careerbridge.com")).thenReturn(false);
        when(passwordEncoder.encode("plainPassword")).thenReturn("hashedPassword");

        User savedUser = User.builder()
                .id(100L)
                .email("newstudent@careerbridge.com")
                .password("hashedPassword")
                .role(Role.STUDENT)
                .build();

        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        String result = authService.register(request);

        assertEquals("User registered successfully", result);
        verify(userRepository, times(1)).save(any(User.class));
        verify(studentRepository, times(1)).save(any(Student.class));
    }
}

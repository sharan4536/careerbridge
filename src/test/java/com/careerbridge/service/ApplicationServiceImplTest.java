package com.careerbridge.service;

import com.careerbridge.dto.request.ApplicationRequest;
import com.careerbridge.dto.response.ApplicationResponse;
import com.careerbridge.entity.*;
import com.careerbridge.repository.*;
import com.careerbridge.service.engine.EligibilityEngine;
import com.careerbridge.service.engine.JobFitEngine;
import com.careerbridge.service.impl.ApplicationServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Application Service & Security Verification")
class ApplicationServiceImplTest {

    @Mock
    private ApplicationRepository applicationRepository;

    @Mock
    private ApplicationEventRepository applicationEventRepository;

    @Mock
    private JobRepository jobRepository;

    @Mock
    private ResumeVersionRepository resumeVersionRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private StudentRepository studentRepository;

    @Mock
    private JobService jobService;

    private ApplicationServiceImpl applicationService;

    @BeforeEach
    void setUp() {
        applicationService = new ApplicationServiceImpl(
                applicationRepository,
                applicationEventRepository,
                jobRepository,
                resumeVersionRepository,
                userRepository,
                studentRepository,
                jobService
        );
    }

    @Test
    @DisplayName("Should successfully submit application with immutable resume snapshot and timeline event")
    void testSubmitApplicationSuccess() {
        String email = "student@careerbridge.com";
        User user = User.builder().id(1L).email(email).role(Role.STUDENT).build();
        Student student = Student.builder().id(10L).user(user).name("Alex").build();
        Company company = Company.builder().id(5L).name("Google").build();
        Job job = Job.builder().id(20L).company(company).role("SWE").build();
        Resume resume = Resume.builder().id(30L).student(student).name("SWE Resume").build();
        ResumeVersion version = ResumeVersion.builder()
                .id(40L)
                .resume(resume)
                .versionNumber(2)
                .fileReference("resume_v2.pdf")
                .build();

        ApplicationRequest request = new ApplicationRequest();
        request.setJobId(20L);
        request.setResumeVersionId(40L);

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(studentRepository.findByUserId(1L)).thenReturn(Optional.of(student));
        when(jobRepository.findById(20L)).thenReturn(Optional.of(job));
        when(resumeVersionRepository.findById(40L)).thenReturn(Optional.of(version));

        EligibilityEngine.EligibilityResult elRes = new EligibilityEngine.EligibilityResult();
        elRes.status = EligibilityEngine.EligibilityStatus.ELIGIBLE;
        when(jobService.checkEligibility(email, 20L)).thenReturn(elRes);

        JobFitEngine.JobFitResult jfRes = new JobFitEngine.JobFitResult();
        jfRes.overallScore = 91.5;
        when(jobService.calculateJobFit(email, 20L)).thenReturn(jfRes);

        when(applicationRepository.save(any(Application.class))).thenAnswer(invocation -> {
            Application a = invocation.getArgument(0);
            a.setId(99L);
            return a;
        });

        ApplicationResponse response = applicationService.submitApplication(email, request);

        assertNotNull(response);
        assertEquals(99L, response.getId());
        assertEquals("SWE", response.getJobRole());
        assertEquals("Google", response.getCompanyName());
        assertEquals("APPLIED", response.getStatus());
        assertEquals("ELIGIBLE", response.getEligibilityResult());
        assertEquals(91.5, response.getJobFitScore());
        assertEquals("SWE Resume", response.getResumeName());
        assertEquals(2, response.getResumeVersion());

        verify(applicationEventRepository, times(1)).save(any(ApplicationEvent.class));
    }

    @Test
    @DisplayName("Should block access when unauthorized student attempts to view another student's application")
    void testIdorProtectionOnApplicationDetails() {
        String studentEmail = "student1@careerbridge.com";
        User user = User.builder().id(1L).email(studentEmail).role(Role.STUDENT).build();
        Student student = Student.builder().id(10L).user(user).build();

        when(userRepository.findByEmail(studentEmail)).thenReturn(Optional.of(user));
        when(studentRepository.findByUserId(1L)).thenReturn(Optional.of(student));
        when(applicationRepository.findByIdAndStudentId(999L, 10L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> applicationService.getApplicationDetails(studentEmail, 999L));
    }
}

package com.careerbridge.service.impl;

import com.careerbridge.dto.request.ApplicationRequest;
import com.careerbridge.dto.response.ApplicationEventResponse;
import com.careerbridge.dto.response.ApplicationResponse;
import com.careerbridge.entity.*;
import com.careerbridge.repository.*;
import com.careerbridge.service.ApplicationService;
import com.careerbridge.service.JobService;
import com.careerbridge.service.engine.EligibilityEngine;
import com.careerbridge.service.engine.JobFitEngine;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class ApplicationServiceImpl implements ApplicationService {

    private final ApplicationRepository applicationRepository;
    private final ApplicationEventRepository applicationEventRepository;
    private final JobRepository jobRepository;
    private final ResumeVersionRepository resumeVersionRepository;
    private final UserRepository userRepository;
    private final StudentRepository studentRepository;
    private final JobService jobService;

    public ApplicationServiceImpl(ApplicationRepository applicationRepository,
                                  ApplicationEventRepository applicationEventRepository,
                                  JobRepository jobRepository,
                                  ResumeVersionRepository resumeVersionRepository,
                                  UserRepository userRepository,
                                  StudentRepository studentRepository,
                                  JobService jobService) {
        this.applicationRepository = applicationRepository;
        this.applicationEventRepository = applicationEventRepository;
        this.jobRepository = jobRepository;
        this.resumeVersionRepository = resumeVersionRepository;
        this.userRepository = userRepository;
        this.studentRepository = studentRepository;
        this.jobService = jobService;
    }

    private Student getStudentByEmail(String email) {
        User user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));
        return studentRepository.findByUserId(user.getId()).orElseThrow(() -> new RuntimeException("Student not found"));
    }

    @Override
    @Transactional
    public ApplicationResponse submitApplication(String email, ApplicationRequest request) {
        Student student = getStudentByEmail(email);
        Job job = jobRepository.findById(request.getJobId()).orElseThrow(() -> new RuntimeException("Job not found"));
        ResumeVersion version = resumeVersionRepository.findById(request.getResumeVersionId())
                .orElseThrow(() -> new RuntimeException("Resume version not found"));

        if (!version.getResume().getStudent().getId().equals(student.getId())) {
            throw new RuntimeException("Unauthorized resume");
        }

        // Immutable Snapshot
        ApplicationResumeSnapshot snapshot = ApplicationResumeSnapshot.builder()
                .resumeName(version.getResume().getName())
                .resumeVersion(version.getVersionNumber())
                .resumeVersionId(version.getId())
                .fileReference(version.getFileReference())
                .contentHash("HASH-" + System.currentTimeMillis())
                .build();

        // Calculate current scores
        EligibilityEngine.EligibilityResult elRes = jobService.checkEligibility(email, job.getId());
        JobFitEngine.JobFitResult jfRes = jobService.calculateJobFit(email, job.getId());

        Application application = Application.builder()
                .student(student)
                .job(job)
                .resumeSnapshot(snapshot)
                .status("APPLIED")
                .eligibilityResult(elRes.status.name())
                .jobFitScore(jfRes.overallScore)
                .build();

        Application savedApp = applicationRepository.save(application);

        ApplicationEvent event = ApplicationEvent.builder()
                .application(savedApp)
                .eventType("APPLICATION_SUBMITTED")
                .description("Application submitted successfully with resume " + snapshot.getResumeName() + " v" + snapshot.getResumeVersion())
                .build();
        applicationEventRepository.save(event);

        return mapToResponse(savedApp);
    }

    @Override
    public List<ApplicationResponse> getMyApplications(String email) {
        Student student = getStudentByEmail(email);
        return applicationRepository.findByStudentId(student.getId()).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<ApplicationResponse> getApplicationsByJobId(String email, Long jobId) {
        return applicationRepository.findByJobId(jobId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public ApplicationResponse getApplicationDetails(String email, Long id) {
        User user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));
        Application app;
        if (user.getRole() == Role.RECRUITER || user.getRole() == Role.ADMIN) {
            app = applicationRepository.findById(id).orElseThrow(() -> new RuntimeException("Application not found"));
        } else {
            Student student = studentRepository.findByUserId(user.getId()).orElseThrow(() -> new RuntimeException("Student not found"));
            app = applicationRepository.findByIdAndStudentId(id, student.getId())
                    .orElseThrow(() -> new RuntimeException("Application not found"));
        }
        return mapToResponse(app);
    }

    @Override
    @Transactional
    public ApplicationResponse updateStatus(String email, Long id, String newStatus) {
        User user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));
        Application app;
        if (user.getRole() == Role.RECRUITER || user.getRole() == Role.ADMIN) {
            app = applicationRepository.findById(id).orElseThrow(() -> new RuntimeException("Application not found"));
        } else {
            Student student = studentRepository.findByUserId(user.getId()).orElseThrow(() -> new RuntimeException("Student not found"));
            app = applicationRepository.findByIdAndStudentId(id, student.getId())
                    .orElseThrow(() -> new RuntimeException("Application not found"));
        }

        app.setStatus(newStatus);
        Application saved = applicationRepository.save(app);

        ApplicationEvent event = ApplicationEvent.builder()
                .application(saved)
                .eventType("STATUS_UPDATED")
                .description("Application status updated to " + newStatus)
                .build();
        applicationEventRepository.save(event);

        return mapToResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ApplicationEventResponse> getApplicationTimeline(String email, Long id) {
        User user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));
        Application app;
        if (user.getRole() == Role.RECRUITER || user.getRole() == Role.ADMIN) {
            app = applicationRepository.findById(id).orElseThrow(() -> new RuntimeException("Application not found"));
        } else {
            Student student = studentRepository.findByUserId(user.getId()).orElseThrow(() -> new RuntimeException("Student not found"));
            app = applicationRepository.findByIdAndStudentId(id, student.getId())
                    .orElseThrow(() -> new RuntimeException("Application not found"));
        }

        return applicationEventRepository.findByApplicationIdOrderByTimestampDesc(app.getId()).stream()
                .map(event -> {
                    ApplicationEventResponse resp = new ApplicationEventResponse();
                    resp.setId(event.getId());
                    resp.setEventType(event.getEventType());
                    resp.setDescription(event.getDescription());
                    resp.setTimestamp(event.getTimestamp());
                    return resp;
                }).collect(Collectors.toList());
    }

    private ApplicationResponse mapToResponse(Application app) {
        ApplicationResponse resp = new ApplicationResponse();
        resp.setId(app.getId());
        resp.setJobId(app.getJob().getId());
        resp.setCompanyName(app.getJob().getCompany().getName());
        resp.setJobRole(app.getJob().getRole());
        resp.setStatus(app.getStatus());
        resp.setEligibilityResult(app.getEligibilityResult());
        resp.setJobFitScore(app.getJobFitScore());
        resp.setAppliedDate(app.getAppliedDate());
        
        if (app.getStudent() != null) {
            resp.setStudentId(app.getStudent().getId());
            resp.setStudentName(app.getStudent().getName());
            if (app.getStudent().getUser() != null) {
                resp.setStudentEmail(app.getStudent().getUser().getEmail());
            }
        }
        
        if (app.getResumeSnapshot() != null) {
            resp.setResumeName(app.getResumeSnapshot().getResumeName());
            resp.setResumeVersion(app.getResumeSnapshot().getResumeVersion());
            resp.setResumeVersionId(app.getResumeSnapshot().getResumeVersionId());
        }
        return resp;
    }
}

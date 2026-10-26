package com.careerbridge.service.impl;

import com.careerbridge.dto.response.JobResponse;
import com.careerbridge.entity.Job;
import com.careerbridge.entity.JobRequirement;
import com.careerbridge.entity.Student;
import com.careerbridge.entity.User;
import com.careerbridge.entity.StudentSkill;
import com.careerbridge.repository.JobRepository;
import com.careerbridge.repository.JobRequirementRepository;
import com.careerbridge.repository.StudentRepository;
import com.careerbridge.repository.StudentSkillRepository;
import com.careerbridge.repository.UserRepository;
import com.careerbridge.repository.CompanyRepository;
import com.careerbridge.service.JobService;
import com.careerbridge.service.engine.EligibilityEngine;
import com.careerbridge.service.engine.JobFitEngine;
import com.careerbridge.service.engine.SkillGapAnalyzer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class JobServiceImpl implements JobService {

    private final JobRepository jobRepository;
    private final JobRequirementRepository jobRequirementRepository;
    private final UserRepository userRepository;
    private final StudentRepository studentRepository;
    private final StudentSkillRepository studentSkillRepository;
    private final CompanyRepository companyRepository;
    private final EligibilityEngine eligibilityEngine;
    private final JobFitEngine jobFitEngine;
    private final SkillGapAnalyzer skillGapAnalyzer;

    private Student getStudentByEmail(String email) {
        User user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));
        return studentRepository.findByUserId(user.getId()).orElseGet(() -> 
            studentRepository.save(Student.builder().user(user).name(user.getEmail().split("@")[0]).build())
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<JobResponse> getAllJobs() {
        return jobRepository.findAll().stream().map(job -> {
            JobResponse resp = new JobResponse();
            resp.setId(job.getId());
            resp.setCompanyName(job.getCompany().getName());
            resp.setRole(job.getRole());
            resp.setLocation(job.getLocation());
            resp.setSalaryRange(job.getSalaryRange());
            resp.setPostedDate(job.getPostedDate());
            return resp;
        }).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public JobResponse createJob(String email, com.careerbridge.dto.request.JobRequest request) {
        // Mocking company for MVP based on recruiter email domain or just a default
        com.careerbridge.entity.Company company = companyRepository.findByName("Tech Corp");
        if(company == null) {
            company = com.careerbridge.entity.Company.builder().name("Tech Corp").website("techcorp.com").build();
            company = companyRepository.save(company);
        }

        Job job = Job.builder()
                .company(company)
                .role(request.getRole())
                .location(request.getLocation())
                .salaryRange(request.getSalaryRange())
                .description(request.getDescription())
                .status("OPEN")
                .build();
        
        job = jobRepository.save(job);
        
        jobRequirementRepository.save(JobRequirement.builder()
                .job(job)
                .minCgpa(6.5)
                .maxBacklogs(2)
                .graduationYear(2025)
                .eligibleBranches("Computer Science, Information Technology, Electronics, ECE, Data Science")
                .requiredSkills("Java, Spring Boot, React, SQL")
                .preferredSkills("Docker, AWS, Git")
                .build());
        
        JobResponse resp = new JobResponse();
        resp.setId(job.getId());
        resp.setCompanyName(company.getName());
        resp.setRole(job.getRole());
        resp.setLocation(job.getLocation());
        resp.setSalaryRange(job.getSalaryRange());
        resp.setPostedDate(job.getPostedDate());
        return resp;
    }

    @Override
    public EligibilityEngine.EligibilityResult checkEligibility(String email, Long jobId) {
        Student student = getStudentByEmail(email);
        JobRequirement req = jobRequirementRepository.findByJobId(jobId).orElse(null);
        return eligibilityEngine.checkEligibility(student, req);
    }

    @Override
    public JobFitEngine.JobFitResult calculateJobFit(String email, Long jobId) {
        Student student = getStudentByEmail(email);
        List<StudentSkill> skills = studentSkillRepository.findByStudentId(student.getId());
        JobRequirement req = jobRequirementRepository.findByJobId(jobId).orElse(null);
        return jobFitEngine.calculateJobFit(student, skills, req);
    }

    @Override
    public SkillGapAnalyzer.SkillGapResult analyzeSkillGap(String email, Long jobId) {
        Student student = getStudentByEmail(email);
        List<StudentSkill> skills = studentSkillRepository.findByStudentId(student.getId());
        JobRequirement req = jobRequirementRepository.findByJobId(jobId).orElse(null);
        return skillGapAnalyzer.analyze(skills, req);
    }
}

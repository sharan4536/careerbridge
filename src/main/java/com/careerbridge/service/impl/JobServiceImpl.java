package com.careerbridge.service.impl;

import com.careerbridge.dto.request.JobRequest;
import com.careerbridge.dto.response.JobResponse;
import com.careerbridge.entity.Company;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
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

    public JobServiceImpl(JobRepository jobRepository,
                          JobRequirementRepository jobRequirementRepository,
                          UserRepository userRepository,
                          StudentRepository studentRepository,
                          StudentSkillRepository studentSkillRepository,
                          CompanyRepository companyRepository,
                          EligibilityEngine eligibilityEngine,
                          JobFitEngine jobFitEngine,
                          SkillGapAnalyzer skillGapAnalyzer) {
        this.jobRepository = jobRepository;
        this.jobRequirementRepository = jobRequirementRepository;
        this.userRepository = userRepository;
        this.studentRepository = studentRepository;
        this.studentSkillRepository = studentSkillRepository;
        this.companyRepository = companyRepository;
        this.eligibilityEngine = eligibilityEngine;
        this.jobFitEngine = jobFitEngine;
        this.skillGapAnalyzer = skillGapAnalyzer;
    }

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
            resp.setCompanyName(job.getCompany() != null ? job.getCompany().getName() : "Enterprise Partner");
            resp.setRole(job.getRole());
            resp.setLocation(job.getLocation());
            resp.setSalaryRange(job.getSalaryRange());
            resp.setDescription(job.getDescription());
            resp.setPostedDate(job.getPostedDate());

            jobRequirementRepository.findByJobId(job.getId()).ifPresent(req -> {
                resp.setRequiredSkills(req.getRequiredSkills());
                resp.setPreferredSkills(req.getPreferredSkills());
                resp.setMinCgpa(req.getMinCgpa());
                resp.setMaxBacklogs(req.getMaxBacklogs());
                resp.setGraduationYear(req.getGraduationYear());
                resp.setEligibleBranches(req.getEligibleBranches());
            });

            return resp;
        }).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public JobResponse createJob(String email, JobRequest request) {
        String companyName = request.getCompanyName();
        if (companyName == null || companyName.trim().isEmpty()) {
            if (email != null && email.contains("@")) {
                String domain = email.substring(email.indexOf("@") + 1);
                int dot = domain.indexOf(".");
                String candidate = dot > 0 ? domain.substring(0, dot) : domain;
                if (!candidate.equalsIgnoreCase("campus") && !candidate.equalsIgnoreCase("student") && !candidate.equalsIgnoreCase("recruiter")) {
                    companyName = candidate.substring(0, 1).toUpperCase() + candidate.substring(1);
                } else {
                    companyName = "CareerBridge Partner";
                }
            } else {
                companyName = "CareerBridge Partner";
            }
        }
        companyName = companyName.trim();

        Company company = companyRepository.findByName(companyName);
        if (company == null) {
            String domainSlug = companyName.toLowerCase().replaceAll("[^a-z0-9]", "");
            company = Company.builder()
                    .name(companyName)
                    .website("www." + (domainSlug.isEmpty() ? "careerbridge" : domainSlug) + ".com")
                    .build();
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

        Double minCgpa = request.getMinCgpa();
        Integer maxBacklogs = request.getMaxBacklogs();
        Integer graduationYear = request.getGraduationYear();
        String eligibleBranches = request.getEligibleBranches();
        String requiredSkills = request.getRequiredSkills();
        String preferredSkills = request.getPreferredSkills();

        
        JobRequirement requirement = jobRequirementRepository.save(JobRequirement.builder()
                .job(job)
                .minCgpa(minCgpa)
                .maxBacklogs(maxBacklogs)
                .graduationYear(graduationYear)
                .eligibleBranches(eligibleBranches)
                .requiredSkills(requiredSkills)
                .preferredSkills(preferredSkills)
                .build());
        
        JobResponse resp = new JobResponse();
        resp.setId(job.getId());
        resp.setCompanyName(company.getName());
        resp.setRole(job.getRole());
        resp.setLocation(job.getLocation());
        resp.setSalaryRange(job.getSalaryRange());
        resp.setDescription(job.getDescription());
        resp.setPostedDate(job.getPostedDate());
        resp.setRequiredSkills(requirement.getRequiredSkills());
        resp.setPreferredSkills(requirement.getPreferredSkills());
        resp.setMinCgpa(requirement.getMinCgpa());
        resp.setMaxBacklogs(requirement.getMaxBacklogs());
        resp.setGraduationYear(requirement.getGraduationYear());
        resp.setEligibleBranches(requirement.getEligibleBranches());
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

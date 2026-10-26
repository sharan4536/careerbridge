package com.careerbridge;

import com.careerbridge.entity.Application;
import com.careerbridge.entity.ApplicationResumeSnapshot;
import com.careerbridge.entity.Company;
import com.careerbridge.entity.Job;
import com.careerbridge.entity.JobRequirement;
import com.careerbridge.entity.Resume;
import com.careerbridge.entity.ResumeVersion;
import com.careerbridge.entity.Role;
import com.careerbridge.entity.Skill;
import com.careerbridge.entity.Student;
import com.careerbridge.entity.StudentSkill;
import com.careerbridge.entity.User;
import com.careerbridge.repository.ApplicationRepository;
import com.careerbridge.repository.CompanyRepository;
import com.careerbridge.repository.JobRepository;
import com.careerbridge.repository.JobRequirementRepository;
import com.careerbridge.repository.ResumeRepository;
import com.careerbridge.repository.ResumeVersionRepository;
import com.careerbridge.repository.SkillRepository;
import com.careerbridge.repository.StudentRepository;
import com.careerbridge.repository.StudentSkillRepository;
import com.careerbridge.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Database Seeder for CareerBridge.
 * 
 * Implements Spring Boot's {@link CommandLineRunner} to automatically populate
 * demo data upon application startup:
 * 1. Default system skills (Java, React, Python, SQL, Spring Boot)
 * 2. Three pre-configured user personas:
 *    - Student (student@careerbridge.com / password123)
 *    - Recruiter (recruiter@careerbridge.com / password123)
 *    - Admin (admin@careerbridge.com / password123)
 * 3. Verified companies (Google, Meta, NextGen AI) and active job postings
 * 4. Job eligibility criteria and skill requirements
 * 5. Initial student resumes, version history, and submitted applications with immutable snapshots
 */
@Component
public class DataSeeder implements CommandLineRunner {

    @Autowired
    private CompanyRepository companyRepository;

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private JobRequirementRepository jobRequirementRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private SkillRepository skillRepository;

    @Autowired
    private StudentSkillRepository studentSkillRepository;

    @Autowired
    private ResumeRepository resumeRepository;

    @Autowired
    private ResumeVersionRepository resumeVersionRepository;

    @Autowired
    private ApplicationRepository applicationRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        // 1. Seed Default Skills
        Skill javaSkill = getOrCreateSkill("Java");
        Skill reactSkill = getOrCreateSkill("React");
        Skill pythonSkill = getOrCreateSkill("Python");
        Skill sqlSkill = getOrCreateSkill("SQL");
        Skill springSkill = getOrCreateSkill("Spring Boot");

        // 2. Seed Default User Personas (Student, Recruiter, Admin)
        User studentUser = userRepository.findByEmail("student@careerbridge.com").orElse(null);
        if (studentUser == null) {
            studentUser = userRepository.save(User.builder()
                    .email("student@careerbridge.com")
                    .password(passwordEncoder.encode("password123"))
                    .role(Role.STUDENT)
                    .build());
        } else {
            studentUser.setPassword(passwordEncoder.encode("password123"));
            studentUser = userRepository.save(studentUser);
        }

        Student student = studentRepository.findByUserId(studentUser.getId()).orElse(null);
        if (student == null) {
            student = studentRepository.save(Student.builder()
                    .user(studentUser)
                    .name("Alex Chen")
                    .college("Stanford University")
                    .degree("B.S. Computer Science")
                    .branch("Computer Science")
                    .graduationYear(2026)
                    .cgpa(8.8)
                    .backlogs(0)
                    .tenthPercentage(94.5)
                    .twelfthPercentage(92.0)
                    .preferredRoles("Software Engineer, Full-Stack Developer")
                    .preferredLocations("Bangalore, Hyderabad, Remote")
                    .salaryExpectations("₹18 LPA - ₹24 LPA")
                    .build());
        }

        if (studentSkillRepository.findByStudentId(student.getId()).isEmpty()) {
            studentSkillRepository.save(StudentSkill.builder().student(student).skill(javaSkill).proficiencyLevel("ADVANCED").build());
            studentSkillRepository.save(StudentSkill.builder().student(student).skill(reactSkill).proficiencyLevel("INTERMEDIATE").build());
            studentSkillRepository.save(StudentSkill.builder().student(student).skill(sqlSkill).proficiencyLevel("INTERMEDIATE").build());
            studentSkillRepository.save(StudentSkill.builder().student(student).skill(springSkill).proficiencyLevel("ADVANCED").build());
        }

        List<Resume> studentResumes = resumeRepository.findByStudentId(student.getId());
        Resume resume;
        if (studentResumes.isEmpty()) {
            resume = resumeRepository.save(Resume.builder()
                    .student(student)
                    .name("Full-Stack Software Engineer")
                    .build());
        } else {
            resume = studentResumes.get(0);
        }

        List<ResumeVersion> versions = resumeVersionRepository.findByResumeIdOrderByVersionNumberDesc(resume.getId());
        ResumeVersion version;
        if (versions.isEmpty()) {
            version = resumeVersionRepository.save(ResumeVersion.builder()
                    .resume(resume)
                    .versionNumber(1)
                    .targetRole("Full-Stack Developer")
                    .skillsRepresented("Java, Spring Boot, React, SQL")
                    .description("Tailored for Tech Giants & Scale-ups")
                    .fileReference("sample_resume.pdf")
                    .build());
        } else {
            version = versions.get(0);
        }

        User recruiterUser = userRepository.findByEmail("recruiter@careerbridge.com").orElse(null);
        if (recruiterUser == null) {
            userRepository.save(User.builder()
                    .email("recruiter@careerbridge.com")
                    .password(passwordEncoder.encode("password123"))
                    .role(Role.RECRUITER)
                    .build());
        } else {
            recruiterUser.setPassword(passwordEncoder.encode("password123"));
            userRepository.save(recruiterUser);
        }

        User adminUser = userRepository.findByEmail("admin@careerbridge.com").orElse(null);
        if (adminUser == null) {
            userRepository.save(User.builder()
                    .email("admin@careerbridge.com")
                    .password(passwordEncoder.encode("password123"))
                    .role(Role.ADMIN)
                    .build());
        } else {
            adminUser.setPassword(passwordEncoder.encode("password123"));
            userRepository.save(adminUser);
        }

        // 3. Seed Companies, Jobs, Requirements
        if (jobRepository.count() == 0) {
            Company google = companyRepository.save(Company.builder().name("Google").website("google.com").build());
            Company meta = companyRepository.save(Company.builder().name("Meta").website("meta.com").build());
            Company startup = companyRepository.save(Company.builder().name("NextGen AI").website("nextgen.ai").build());

            Job job1 = jobRepository.save(Job.builder()
                    .company(google)
                    .role("Software Engineer III")
                    .location("Bangalore, India")
                    .salaryRange("₹24 LPA - ₹32 LPA")
                    .description("Build the future of web scale search and distributed infrastructure.")
                    .status("OPEN")
                    .build());

            jobRequirementRepository.save(JobRequirement.builder()
                    .job(job1)
                    .minCgpa(7.5)
                    .maxBacklogs(0)
                    .eligibleBranches("Computer Science, IT, Software Engineering")
                    .graduationYear(2026)
                    .requiredSkills("Java, Spring Boot, Distributed Systems")
                    .preferredSkills("GCP, Kubernetes")
                    .experienceRequirement(1)
                    .build());

            Job job2 = jobRepository.save(Job.builder()
                    .company(meta)
                    .role("Frontend Developer")
                    .location("Remote / Hyderabad")
                    .salaryRange("₹16 LPA - ₹22 LPA")
                    .description("Create amazing user interfaces with modern React and web standards.")
                    .status("OPEN")
                    .build());

            jobRequirementRepository.save(JobRequirement.builder()
                    .job(job2)
                    .minCgpa(7.0)
                    .maxBacklogs(1)
                    .eligibleBranches("Computer Science, IT, Electronics")
                    .graduationYear(2026)
                    .requiredSkills("React, JavaScript, CSS")
                    .preferredSkills("TypeScript, Next.js")
                    .experienceRequirement(0)
                    .build());

            Job job3 = jobRepository.save(Job.builder()
                    .company(startup)
                    .role("Machine Learning Engineer")
                    .location("Bangalore / Hybrid")
                    .salaryRange("₹20 LPA - ₹28 LPA")
                    .description("Train and deploy cutting-edge generative AI models.")
                    .status("OPEN")
                    .build());

            jobRequirementRepository.save(JobRequirement.builder()
                    .job(job3)
                    .minCgpa(8.0)
                    .maxBacklogs(0)
                    .eligibleBranches("Computer Science, Artificial Intelligence, Data Science")
                    .graduationYear(2026)
                    .requiredSkills("Python, PyTorch, Machine Learning")
                    .preferredSkills("LLMs, Transformers")
                    .experienceRequirement(1)
                    .build());

            // 4. Seed an initial application for demo student if both exist
            if (student != null && !versions.isEmpty()) {
                ResumeVersion ver = versions.get(0);
                        ApplicationResumeSnapshot snapshot = ApplicationResumeSnapshot.builder()
                                .resumeName(resume.getName())
                                .resumeVersion(ver.getVersionNumber())
                                .resumeVersionId(ver.getId())
                                .fileReference(ver.getFileReference())
                                .contentHash("HASH-DEMO-001")
                                .build();

                        applicationRepository.save(Application.builder()
                                .student(student)
                                .job(job1)
                                .resumeSnapshot(snapshot)
                                .status("INTERVIEW_SCHEDULED")
                                .eligibilityResult("ELIGIBLE")
                                .jobFitScore(88.0)
                                .notes("Passed preliminary screening.")
                                .build());

                        ApplicationResumeSnapshot snapshot2 = ApplicationResumeSnapshot.builder()
                                .resumeName(resume.getName())
                                .resumeVersion(ver.getVersionNumber())
                                .resumeVersionId(ver.getId())
                                .fileReference(ver.getFileReference())
                                .contentHash("HASH-DEMO-002")
                                .build();

                        applicationRepository.save(Application.builder()
                                .student(student)
                                .job(job2)
                                .resumeSnapshot(snapshot2)
                                .status("UNDER_REVIEW")
                                .eligibilityResult("ELIGIBLE")
                                .jobFitScore(92.0)
                                .notes("Applied via portal.")
                                .build());
            }

            System.out.println("DataSeeder: Injected complete mock database state!");
        } else {
            // Ensure job requirements exist for existing jobs if they were created earlier
            List<Job> allJobs = jobRepository.findAll();
            for (Job j : allJobs) {
                if (jobRequirementRepository.findByJobId(j.getId()).isEmpty()) {
                    jobRequirementRepository.save(JobRequirement.builder()
                            .job(j)
                            .minCgpa(7.0)
                            .maxBacklogs(1)
                            .eligibleBranches("Computer Science, IT, Software Engineering")
                            .graduationYear(2026)
                            .requiredSkills("Java, React, SQL")
                            .experienceRequirement(0)
                            .build());
                }
            }

            if (applicationRepository.count() == 0 && !allJobs.isEmpty()) {
                User sUser = userRepository.findByEmail("student@careerbridge.com").orElse(null);
                if (sUser != null) {
                    Student s = studentRepository.findByUserId(sUser.getId()).orElse(null);
                    List<Resume> resList = resumeRepository.findByStudentId(s.getId());
                    if (!resList.isEmpty()) {
                        List<ResumeVersion> vList = resumeVersionRepository.findByResumeIdOrderByVersionNumberDesc(resList.get(0).getId());
                        if (!vList.isEmpty()) {
                            ResumeVersion rv = vList.get(0);
                            applicationRepository.save(Application.builder()
                                    .student(s)
                                    .job(allJobs.get(0))
                                    .resumeSnapshot(ApplicationResumeSnapshot.builder()
                                            .resumeName(resList.get(0).getName())
                                            .resumeVersion(rv.getVersionNumber())
                                            .resumeVersionId(rv.getId())
                                            .fileReference(rv.getFileReference())
                                            .contentHash("HASH-DEMO-001")
                                            .build())
                                    .status("INTERVIEW_SCHEDULED")
                                    .eligibilityResult("ELIGIBLE")
                                    .jobFitScore(88.0)
                                    .notes("Shortlisted for tech round")
                                    .build());

                            if (allJobs.size() > 1) {
                                applicationRepository.save(Application.builder()
                                        .student(s)
                                        .job(allJobs.get(1))
                                        .resumeSnapshot(ApplicationResumeSnapshot.builder()
                                                .resumeName(resList.get(0).getName())
                                                .resumeVersion(rv.getVersionNumber())
                                                .resumeVersionId(rv.getId())
                                                .fileReference(rv.getFileReference())
                                                .contentHash("HASH-DEMO-002")
                                                .build())
                                        .status("UNDER_REVIEW")
                                        .eligibilityResult("ELIGIBLE")
                                        .jobFitScore(92.0)
                                        .notes("Application submitted")
                                        .build());
                            }
                        }
                    }
                }
            }
        }

        // Auto-migrate all existing jobs and student profiles in DB to INR (₹)
        for (Job j : jobRepository.findAll()) {
            if (j.getSalaryRange() != null && (j.getSalaryRange().contains("$") || j.getSalaryRange().contains("k") || j.getSalaryRange().contains("K"))) {
                String updated = j.getSalaryRange()
                        .replace("$150k - $200k", "₹24 LPA - ₹32 LPA")
                        .replace("$120k - $160k", "₹16 LPA - ₹22 LPA")
                        .replace("$130k - $180k", "₹20 LPA - ₹28 LPA")
                        .replace("$110,000 - $130,000", "₹14 LPA - ₹18 LPA")
                        .replaceAll("\\$[0-9,kK -]+", "₹18 LPA - ₹25 LPA");
                j.setSalaryRange(updated);
                jobRepository.save(j);
            }
        }
        for (Student st : studentRepository.findAll()) {
            if (st.getSalaryExpectations() != null && st.getSalaryExpectations().contains("$")) {
                st.setSalaryExpectations("₹18 LPA - ₹24 LPA");
                studentRepository.save(st);
            }
        }
    }

    private Skill getOrCreateSkill(String name) {
        return skillRepository.findByName(name)
                .orElseGet(() -> skillRepository.save(Skill.builder().name(name).build()));
    }
}

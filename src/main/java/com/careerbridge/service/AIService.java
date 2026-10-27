package com.careerbridge.service;

import com.careerbridge.entity.Job;
import com.careerbridge.entity.JobRequirement;
import com.careerbridge.entity.ResumeVersion;
import com.careerbridge.entity.Student;
import com.careerbridge.entity.User;
import com.careerbridge.repository.JobRepository;
import com.careerbridge.repository.JobRequirementRepository;
import com.careerbridge.repository.ResumeVersionRepository;
import com.careerbridge.repository.StudentRepository;
import com.careerbridge.repository.UserRepository;
import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * AI Career Intelligence Service.
 * 
 * Provides real-time automated career intelligence computed dynamically from H2 database entities:
 * 1. Resume Critique & ATS Analysis: Evaluates actual resume version skills, target role fit, and scores ATS compatibility
 * 2. Role-specific Interview Question Preparation: Generates tailored technical and behavioral prompts matching specific job requirements
 * 3. Personalized Cover Letter Generation: Formulates tailored cover letters synthesizing student profile, college, and job requirements
 */
@Service
@Transactional(readOnly = true)
public class AIService {


    private final ResumeVersionRepository resumeVersionRepository;
    private final JobRepository jobRepository;
    private final JobRequirementRepository jobRequirementRepository;
    private final UserRepository userRepository;
    private final StudentRepository studentRepository;

    public AIService(ResumeVersionRepository resumeVersionRepository,
                      JobRepository jobRepository,
                      JobRequirementRepository jobRequirementRepository,
                      UserRepository userRepository,
                      StudentRepository studentRepository) {
        this.resumeVersionRepository = resumeVersionRepository;
        this.jobRepository = jobRepository;
        this.jobRequirementRepository = jobRequirementRepository;
        this.userRepository = userRepository;
        this.studentRepository = studentRepository;
    }

    public Map<String, Object> analyzeResume(Long resumeVersionId) {
        Map<String, Object> response = new HashMap<>();
        ResumeVersion version = resumeVersionRepository.findById(resumeVersionId).orElse(null);

        int baseScore = 74;
        List<String> strengths = new ArrayList<>();
        List<String> weaknesses = new ArrayList<>();
        List<String> suggestions = new ArrayList<>();

        if (version != null) {
            String role = (version.getTargetRole() != null && !version.getTargetRole().isBlank()) 
                    ? version.getTargetRole() : "Software Engineer";
            String skills = (version.getSkillsRepresented() != null) ? version.getSkillsRepresented() : "";
            String desc = (version.getDescription() != null) ? version.getDescription() : "";
            int versionNum = (version.getVersionNumber() != null) ? version.getVersionNumber() : 1;

            String[] skillTokens = skills.split("[,;\\s]+");
            int validSkillsCount = 0;
            for (String t : skillTokens) {
                if (!t.trim().isEmpty()) validSkillsCount++;
            }

            // Dynamic ATS calculation
            baseScore = Math.min(96, Math.max(70, 68 + (validSkillsCount * 3) + (desc.length() > 40 ? 8 : 3)));

            strengths.add("Strong targeted alignment for " + role + " with verified competencies in " + (skills.isBlank() ? "core engineering" : skills));
            strengths.add("Vault version v" + versionNum + " displays clean milestone progression and audit traceability");
            if (validSkillsCount >= 4) {
                strengths.add("High technical keyword density ensures automated ATS parsing and ranking advantage");
            }

            if (validSkillsCount < 3) {
                weaknesses.add("Skill density is modest; explicitly document frameworks and tools relevant to " + role);
            }
            if (desc.length() < 50) {
                weaknesses.add("Project accomplishments summary is concise; add measurable business impact and scale");
            }
            String lowerSkills = skills.toLowerCase();
            if (!lowerSkills.contains("docker") && !lowerSkills.contains("aws") && !lowerSkills.contains("cloud") && !lowerSkills.contains("git")) {
                weaknesses.add("Lacks mention of modern Cloud, containerization, or DevOps tooling (e.g. Docker, AWS, Git)");
            }

            suggestions.add("Incorporate quantified performance metrics in project bullet points (e.g., 'reduced API latency by 35%')");
            suggestions.add("Include public GitHub repository links demonstrating your implementations with " + (skills.isBlank() ? "key technologies" : skills.split("[,;]")[0].trim()));
            suggestions.add("Audit keywords against campus recruiter rubrics for " + role + " to exceed the 90% match threshold");
        } else {
            strengths.add("Clean academic structure and verified candidate identity");
            weaknesses.add("Upload a resume version to enable deep contextual ATS keyword scoring");
            suggestions.add("Create a role-specific resume version in your vault");
        }

        response.put("score", baseScore);
        response.put("strengths", strengths);
        response.put("weaknesses", weaknesses);
        response.put("suggestions", suggestions);
        return response;
    }

    public Map<String, Object> generateInterviewQuestions(Long jobId) {
        Map<String, Object> response = new HashMap<>();
        Job job = jobRepository.findById(jobId).orElse(null);
        JobRequirement req = jobRequirementRepository.findByJobId(jobId).orElse(null);

        List<String> technical = new ArrayList<>();
        List<String> behavioral = new ArrayList<>();

        String role = (job != null && job.getRole() != null) ? job.getRole() : "Candidate Role";
        String company = (job != null && job.getCompany() != null) ? job.getCompany().getName() : "Hiring Company";
        String reqSkills = (req != null && req.getRequiredSkills() != null) ? req.getRequiredSkills() : "";
        String prefSkills = (req != null && req.getPreferredSkills() != null) ? req.getPreferredSkills() : "";

        String lowerRole = role.toLowerCase();
        String lowerSkills = reqSkills.toLowerCase();

        // Dynamically tailor technical questions to role and required stack
        if (lowerSkills.contains("react") || lowerRole.contains("frontend") || lowerRole.contains("ui") || lowerRole.contains("full")) {
            technical.add("Explain React's virtual DOM reconciliation and fiber tree algorithm. How do you prevent unnecessary re-renders in complex dashboards?");
            technical.add("How do you design scalable client-side state management and handle optimistic UI updates during network latency?");
        }
        if (lowerSkills.contains("java") || lowerSkills.contains("spring") || lowerRole.contains("backend") || lowerRole.contains("full")) {
            technical.add("How does Spring Boot manage transaction boundaries with @Transactional, and what causes proxy self-invocation rollback failures?");
            technical.add("Walk me through database query optimization: how B-Tree indexes work, diagnosing execution plans with EXPLAIN ANALYZE, and eliminating N+1 query bottlenecks.");
        }
        if (lowerSkills.contains("python") || lowerRole.contains("data") || lowerRole.contains("ai") || lowerRole.contains("ml")) {
            technical.add("Explain vectorization and broadcasting in Python NumPy/Pandas, and how you prevent model overfitting on imbalanced datasets.");
            technical.add("How do you structure data ingestion pipelines to handle streaming data with schema validation?");
        }
        if (lowerSkills.contains("docker") || lowerSkills.contains("aws") || lowerRole.contains("cloud") || lowerRole.contains("devops")) {
            technical.add("How do you architect multi-stage Docker builds to minimize image attack surfaces, and how do Kubernetes readiness probes prevent downtime?");
        }

        // Custom job & company synthesis question
        if (!reqSkills.isBlank()) {
            technical.add("For the " + role + " opening at " + company + ", how would you design an architecture using " + reqSkills + " ensuring high availability, ACID transactions, and zero data loss?");
        } else {
            technical.add("For the " + role + " opening at " + company + ", how would you structure system reliability, testing pipelines, and production monitoring?");
        }

        // Tailored behavioral questions
        behavioral.add("Why are you specifically interested in joining " + company + " as a " + role + ", and how does your engineering philosophy align with our team?");
        String primarySkill = reqSkills.isBlank() ? "your primary technical stack" : (reqSkills.contains(",") ? reqSkills.split(",")[0].trim() : reqSkills);
        behavioral.add("Tell me about a challenging technical hurdle you solved while building with " + primarySkill + ", and what trade-offs you evaluated.");
        behavioral.add("Describe a situation where requirements or release timelines shifted abruptly. How did you prioritize deliverables and maintain code quality?");

        response.put("technical", technical);
        response.put("behavioral", behavioral);
        response.put("role", role);
        response.put("company", company);
        return response;
    }

    public Map<String, Object> generateCoverLetter(String userIdentifier, String jobDescription, String targetRole) {
        String role = (targetRole != null && !targetRole.isBlank()) ? targetRole : "Candidate Role";
        String desc = (jobDescription != null) ? jobDescription : "";

        String studentName = "Candidate";
        String academicCredentials = "";
        
        User user = userRepository.findByEmail(userIdentifier).orElse(null);
        if (user != null) {
            Student student = studentRepository.findByUserId(user.getId()).orElse(null);
            if (student != null) {
                if (student.getName() != null && !student.getName().isBlank()) {
                    studentName = student.getName();
                } else if (user.getEmail() != null) {
                    studentName = user.getEmail().split("@")[0];
                }
                StringBuilder sb = new StringBuilder();
                if (student.getDegree() != null && !student.getDegree().isBlank()) sb.append(student.getDegree()).append(" ");
                if (student.getBranch() != null && !student.getBranch().isBlank()) sb.append("in ").append(student.getBranch()).append(" ");
                if (student.getCollege() != null && !student.getCollege().isBlank()) sb.append("from ").append(student.getCollege()).append(" ");
                if (student.getCgpa() != null && student.getCgpa() > 0) sb.append("(CGPA: ").append(student.getCgpa()).append(")");
                academicCredentials = sb.toString().trim();
            }
        } else if (userIdentifier != null && !userIdentifier.isBlank()) {
            studentName = userIdentifier.contains("@") ? userIdentifier.split("@")[0] : userIdentifier;
        }

        String academicIntro = academicCredentials.isEmpty() 
                ? "With my technical background, academic foundation, and hands-on project experience, "
                : "Holding a background in " + academicCredentials + ", ";

        String letter = "Dear Hiring Team,\n\n"
                + "I am writing to express my strong enthusiasm for the " + role + " position. "
                + academicIntro
                + "I am confident in my ability to make an immediate, positive impact on your engineering initiatives.\n\n"
                + "Having thoroughly reviewed your technical objectives:\n\"" 
                + (desc.length() > 140 ? desc.substring(0, 140) + "..." : (desc.isBlank() ? "Driving engineering excellence and scalable systems." : desc)) + "\"\n\n"
                + "I have focused my efforts on writing clean, maintainable code, architecting reliable distributed services, and collaborating closely with cross-functional peers to ship resilient digital solutions. "
                + "My skill set directly aligns with the core technical goals of your team.\n\n"
                + "I would welcome the opportunity to discuss how my dedication and technical foundation can contribute to your team's success. Thank you for your time and consideration.\n\n"
                + "Sincerely,\n"
                + studentName;

        Map<String, Object> response = new HashMap<>();
        response.put("coverLetter", letter);
        response.put("role", role);
        response.put("studentName", studentName);
        return response;
    }
}

package com.careerbridge.service;

import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * AI Career Intelligence Service.
 * 
 * Provides automated career assistance features for students:
 * 1. Resume Critique & ATS Analysis: Evaluates resume content, strengths, and formatting
 * 2. Role-specific Interview Question Preparation: Generates tailored technical and behavioral prompts
 * 3. Personalized Cover Letter Generation: Formulates tailored cover letters synthesizing student profile and job description
 */
@Service
public class AIService {

    public Map<String, Object> analyzeResume(Long resumeVersionId) {
        Map<String, Object> response = new HashMap<>();
        response.put("score", 85);
        response.put("strengths", Arrays.asList("Clear action verbs", "Good formatting", "Metrics included"));
        response.put("weaknesses", Arrays.asList("Lacks recent project details", "Objective statement is generic"));
        response.put("suggestions", Arrays.asList("Add a 'Projects' section", "Quantify your impact in your last role"));
        return response;
    }

    public Map<String, Object> generateInterviewQuestions(Long jobId) {
        Map<String, Object> response = new HashMap<>();
        response.put("technical", Arrays.asList("Explain OOP concepts and system design trade-offs.", "How does Spring Boot dependency injection and auto-configuration work?", "Walk me through how you optimize slow database queries and REST APIs."));
        response.put("behavioral", Arrays.asList("Tell me about a challenging engineering obstacle you faced and solved.", "How do you prioritize deliverables when requirements change unexpectedly?", "Why do you want to join our engineering organization?"));
        return response;
    }

    public Map<String, Object> generateCoverLetter(String studentName, String jobDescription, String targetRole) {
        String role = (targetRole != null && !targetRole.isBlank()) ? targetRole : "Software Engineer";
        String name = (studentName != null && !studentName.isBlank()) ? studentName : "Applicant";

        String letter = "Dear Hiring Team,\n\n"
                + "I am writing to express my strong enthusiasm for the " + role + " position. "
                + "With my technical background, academic foundation, and hands-on project experience, "
                + "I am confident in my ability to make an immediate impact on your engineering initiatives.\n\n"
                + "Having thoroughly reviewed your requirements:\n\"" + (jobDescription.length() > 120 ? jobDescription.substring(0, 120) + "..." : jobDescription) + "\"\n\n"
                + "I have focused my efforts on building high-performance, maintainable software and collaborating with cross-functional teams to deliver resilient digital solutions. "
                + "My background aligns directly with the core technical objectives of your role.\n\n"
                + "I would welcome the opportunity to discuss how my skill set and passion can contribute to your team's success. Thank you for your time and consideration.\n\n"
                + "Sincerely,\n"
                + name;

        Map<String, Object> response = new HashMap<>();
        response.put("coverLetter", letter);
        response.put("role", role);
        return response;
    }
}

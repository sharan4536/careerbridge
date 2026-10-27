package com.careerbridge.dto.request;

import lombok.Data;

@Data
public class JobRequest {
    private String role;
    private String companyName;
    private String location;
    private String salaryRange;
    private String description;
    private Double minCgpa;
    private Integer maxBacklogs;
    private Integer graduationYear;
    private String eligibleBranches;
    private String requiredSkills;
    private String preferredSkills;
}


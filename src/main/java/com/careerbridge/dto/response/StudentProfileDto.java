package com.careerbridge.dto.response;

import lombok.Data;

@Data
public class StudentProfileDto {
    private String name;
    private String email;
    private String phone;
    private String degree;
    private String branch;
    private String college;
    private Integer graduationYear;
    private Double cgpa;
    private Double tenthPercentage;
    private Double twelfthPercentage;
    private Integer backlogs;
    private String preferredRoles;
    private String preferredLocations;
    private String salaryExpectations;
}

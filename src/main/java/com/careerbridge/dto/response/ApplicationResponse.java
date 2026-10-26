package com.careerbridge.dto.response;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ApplicationResponse {
    private Long id;
    private String companyName;
    private String jobRole;
    private String status;
    private String eligibilityResult;
    private Double jobFitScore;
    private String resumeName;
    private Integer resumeVersion;
    private Long jobId;
    private Long studentId;
    private String studentName;
    private String studentEmail;
    private Long resumeVersionId;
    private LocalDateTime appliedDate;
}

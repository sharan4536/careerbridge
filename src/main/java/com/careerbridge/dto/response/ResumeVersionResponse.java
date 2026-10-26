package com.careerbridge.dto.response;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ResumeVersionResponse {
    private Long id;
    private Integer versionNumber;
    private String targetRole;
    private String skillsRepresented;
    private String description;
    private LocalDateTime createdAt;
}

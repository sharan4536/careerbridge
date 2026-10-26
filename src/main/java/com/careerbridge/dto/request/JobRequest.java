package com.careerbridge.dto.request;

import lombok.Data;

@Data
public class JobRequest {
    private String role;
    private String location;
    private String salaryRange;
    private String description;
}

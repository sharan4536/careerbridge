package com.careerbridge.dto.response;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class JobResponse {
    private Long id;
    private String companyName;
    private String role;
    private String location;
    private String salaryRange;
    private LocalDateTime postedDate;
}

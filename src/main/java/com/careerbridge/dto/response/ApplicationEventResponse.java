package com.careerbridge.dto.response;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ApplicationEventResponse {
    private Long id;
    private String eventType;
    private String description;
    private LocalDateTime timestamp;
}

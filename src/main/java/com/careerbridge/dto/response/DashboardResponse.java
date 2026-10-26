package com.careerbridge.dto.response;

import lombok.Data;
import java.util.Map;

@Data
public class DashboardResponse {
    private int totalApplications;
    private int interviewsScheduled;
    private int rejections;
    private int offers;
    private Map<String, Integer> resumePerformance;
    private Map<String, Double> averageJobFit;
}

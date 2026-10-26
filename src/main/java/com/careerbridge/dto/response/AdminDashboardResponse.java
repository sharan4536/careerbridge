package com.careerbridge.dto.response;

import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
public class AdminDashboardResponse {
    private long totalUsers;
    private long totalStudents;
    private long totalJobs;
    private long totalApplications;
    private List<Map<String, Object>> recentApplications;
}

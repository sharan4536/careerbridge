package com.careerbridge.service;

import com.careerbridge.dto.request.ApplicationRequest;
import com.careerbridge.dto.response.ApplicationEventResponse;
import com.careerbridge.dto.response.ApplicationResponse;

import java.util.List;

public interface ApplicationService {
    ApplicationResponse submitApplication(String email, ApplicationRequest request);
    List<ApplicationResponse> getMyApplications(String email);
    List<ApplicationResponse> getApplicationsByJobId(String email, Long jobId);
    ApplicationResponse getApplicationDetails(String email, Long id);
    ApplicationResponse updateStatus(String email, Long id, String newStatus);
    List<ApplicationEventResponse> getApplicationTimeline(String email, Long id);
}

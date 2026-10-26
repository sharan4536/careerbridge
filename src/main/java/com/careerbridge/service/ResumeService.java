package com.careerbridge.service;

import com.careerbridge.dto.response.ResumeResponse;
import com.careerbridge.dto.response.ResumeVersionResponse;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

public interface ResumeService {
    ResumeResponse createResume(String email, String resumeName);
    List<ResumeResponse> getAllResumes(String email);
    ResumeResponse getResume(String email, Long resumeId);
    ResumeVersionResponse addVersion(String email, Long resumeId, MultipartFile file, String targetRole, String skillsRepresented, String description);
    List<ResumeVersionResponse> getResumeVersions(String email, Long resumeId);
    byte[] downloadResumeVersionFile(String email, Long versionId);
}

package com.careerbridge.service.impl;

import com.careerbridge.dto.response.ResumeResponse;
import com.careerbridge.dto.response.ResumeVersionResponse;
import com.careerbridge.entity.Resume;
import com.careerbridge.entity.ResumeVersion;
import com.careerbridge.entity.Student;
import com.careerbridge.entity.User;
import com.careerbridge.repository.ResumeRepository;
import com.careerbridge.repository.ResumeVersionRepository;
import com.careerbridge.repository.StudentRepository;
import com.careerbridge.repository.UserRepository;
import com.careerbridge.service.FileStorageService;
import com.careerbridge.service.ResumeService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.careerbridge.entity.Role;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class ResumeServiceImpl implements ResumeService {

    private final ResumeRepository resumeRepository;
    private final ResumeVersionRepository versionRepository;
    private final UserRepository userRepository;
    private final StudentRepository studentRepository;
    private final FileStorageService fileStorageService;

    public ResumeServiceImpl(ResumeRepository resumeRepository,
                             ResumeVersionRepository versionRepository,
                             UserRepository userRepository,
                             StudentRepository studentRepository,
                             FileStorageService fileStorageService) {
        this.resumeRepository = resumeRepository;
        this.versionRepository = versionRepository;
        this.userRepository = userRepository;
        this.studentRepository = studentRepository;
        this.fileStorageService = fileStorageService;
    }

    private Student getStudentByEmail(String email) {
        User user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));
        return studentRepository.findByUserId(user.getId()).orElseGet(() -> 
            studentRepository.save(Student.builder().user(user).name(user.getEmail().split("@")[0]).build())
        );
    }

    @Override
    @Transactional
    public ResumeResponse createResume(String email, String resumeName) {
        Student student = getStudentByEmail(email);
        Resume resume = Resume.builder()
                .student(student)
                .name(resumeName)
                .build();
        return mapToResumeResponse(resumeRepository.save(resume));
    }

    @Override
    public List<ResumeResponse> getAllResumes(String email) {
        Student student = getStudentByEmail(email);
        return resumeRepository.findByStudentId(student.getId()).stream()
                .map(this::mapToResumeResponse)
                .collect(Collectors.toList());
    }

    @Override
    public ResumeResponse getResume(String email, Long resumeId) {
        Student student = getStudentByEmail(email);
        Resume resume = resumeRepository.findByIdAndStudentId(resumeId, student.getId())
                .orElseThrow(() -> new RuntimeException("Resume not found"));
        return mapToResumeResponse(resume);
    }

    @Override
    @Transactional
    public ResumeVersionResponse addVersion(String email, Long resumeId, MultipartFile file, String targetRole, String skillsRepresented, String description) {
        Student student = getStudentByEmail(email);
        Resume resume = resumeRepository.findByIdAndStudentId(resumeId, student.getId())
                .orElseThrow(() -> new RuntimeException("Resume not found"));
        
        String fileReference = fileStorageService.storeFile(file, student.getId());
        
        List<ResumeVersion> existingVersions = versionRepository.findByResumeIdOrderByVersionNumberDesc(resumeId);
        int nextVersionNumber = existingVersions.isEmpty() ? 1 : existingVersions.get(0).getVersionNumber() + 1;
        
        ResumeVersion version = ResumeVersion.builder()
                .resume(resume)
                .versionNumber(nextVersionNumber)
                .fileReference(fileReference)
                .targetRole(targetRole)
                .skillsRepresented(skillsRepresented)
                .description(description)
                .build();
                
        return mapToVersionResponse(versionRepository.save(version));
    }

    @Override
    public List<ResumeVersionResponse> getResumeVersions(String email, Long resumeId) {
        Student student = getStudentByEmail(email);
        Resume resume = resumeRepository.findByIdAndStudentId(resumeId, student.getId())
                .orElseThrow(() -> new RuntimeException("Resume not found"));
        
        return versionRepository.findByResumeIdOrderByVersionNumberDesc(resumeId).stream()
                .map(this::mapToVersionResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] downloadResumeVersionFile(String email, Long versionId) {
        User user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));
        ResumeVersion version = versionRepository.findById(versionId)
                .orElseThrow(() -> new RuntimeException("Version not found"));

        if (user.getRole() == Role.RECRUITER || user.getRole() == Role.ADMIN) {
            return fileStorageService.loadFile(version.getFileReference());
        }

        Student student = studentRepository.findByUserId(user.getId())
                .orElseThrow(() -> new RuntimeException("Student profile not found"));
        if (!version.getResume().getStudent().getId().equals(student.getId())) {
            throw new RuntimeException("Unauthorized to access this resume");
        }

        return fileStorageService.loadFile(version.getFileReference());
    }

    private ResumeResponse mapToResumeResponse(Resume resume) {
        ResumeResponse response = new ResumeResponse();
        response.setId(resume.getId());
        response.setName(resume.getName());
        response.setCreatedAt(resume.getCreatedAt());
        response.setUpdatedAt(resume.getUpdatedAt());
        if (resume.getId() != null) {
            List<ResumeVersionResponse> versions = versionRepository.findByResumeIdOrderByVersionNumberDesc(resume.getId())
                    .stream()
                    .map(this::mapToVersionResponse)
                    .collect(Collectors.toList());
            response.setVersions(versions);
        }
        return response;
    }

    private ResumeVersionResponse mapToVersionResponse(ResumeVersion version) {
        ResumeVersionResponse response = new ResumeVersionResponse();
        response.setId(version.getId());
        response.setVersionNumber(version.getVersionNumber());
        response.setTargetRole(version.getTargetRole());
        response.setSkillsRepresented(version.getSkillsRepresented());
        response.setDescription(version.getDescription());
        response.setCreatedAt(version.getCreatedAt());
        return response;
    }
}

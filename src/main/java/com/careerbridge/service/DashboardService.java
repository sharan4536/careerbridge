package com.careerbridge.service;

import com.careerbridge.dto.response.DashboardResponse;
import com.careerbridge.entity.Application;
import com.careerbridge.entity.Student;
import com.careerbridge.entity.User;
import com.careerbridge.repository.ApplicationRepository;
import com.careerbridge.repository.StudentRepository;
import com.careerbridge.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional(readOnly = true)
public class DashboardService {

    private final ApplicationRepository applicationRepository;
    private final UserRepository userRepository;
    private final StudentRepository studentRepository;

    public DashboardService(ApplicationRepository applicationRepository,
                            UserRepository userRepository,
                            StudentRepository studentRepository) {
        this.applicationRepository = applicationRepository;
        this.userRepository = userRepository;
        this.studentRepository = studentRepository;
    }

    public DashboardResponse getDashboardStats(String email) {
        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null) {
            return new DashboardResponse();
        }

        Student student = studentRepository.findByUserId(user.getId()).orElse(null);
        if (student == null) {
            if (user.getRole() == com.careerbridge.entity.Role.STUDENT) {
                student = studentRepository.save(Student.builder().user(user).name(user.getEmail().split("@")[0]).build());
            } else {
                DashboardResponse resp = new DashboardResponse();
                resp.setTotalApplications((int) applicationRepository.count());
                resp.setResumePerformance(new HashMap<>());
                resp.setAverageJobFit(Map.of("Overall", 0.0));
                return resp;
            }
        }

        List<Application> apps = applicationRepository.findByStudentId(student.getId());

        DashboardResponse resp = new DashboardResponse();
        resp.setTotalApplications(apps.size());
        
        int interviews = 0;
        int rejections = 0;
        int offers = 0;
        
        Map<String, Integer> resumeStats = new HashMap<>();
        
        for (Application app : apps) {
            String status = app.getStatus().toUpperCase();
            if (status.contains("INTERVIEW")) interviews++;
            else if (status.contains("REJECT")) rejections++;
            else if (status.contains("OFFER")) offers++;
            
            if (app.getResumeSnapshot() != null) {
                String resumeName = app.getResumeSnapshot().getResumeName() + " v" + app.getResumeSnapshot().getResumeVersion();
                resumeStats.put(resumeName, resumeStats.getOrDefault(resumeName, 0) + 1);
            }
        }
        
        resp.setInterviewsScheduled(interviews);
        resp.setRejections(rejections);
        resp.setOffers(offers);
        resp.setResumePerformance(resumeStats);
        
        Map<String, Double> jobFit = new HashMap<>();
        jobFit.put("Overall", apps.stream().mapToDouble(a -> a.getJobFitScore() != null ? a.getJobFitScore() : 0.0).average().orElse(0.0));
        resp.setAverageJobFit(jobFit);
        
        return resp;
    }
}

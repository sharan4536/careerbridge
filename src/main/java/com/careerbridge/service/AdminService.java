package com.careerbridge.service;

import com.careerbridge.dto.response.AdminDashboardResponse;
import com.careerbridge.entity.Application;
import com.careerbridge.entity.Role;
import com.careerbridge.repository.ApplicationRepository;
import com.careerbridge.repository.JobRepository;
import com.careerbridge.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AdminService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private ApplicationRepository applicationRepository;

    public AdminDashboardResponse getAdminDashboardStats() {
        AdminDashboardResponse resp = new AdminDashboardResponse();
        
        List<com.careerbridge.entity.User> allUsers = userRepository.findAll();
        resp.setTotalUsers(allUsers.size());
        resp.setTotalStudents(allUsers.stream().filter(u -> u.getRole() == Role.STUDENT).count());
        resp.setTotalJobs(jobRepository.count());
        
        List<Application> apps = applicationRepository.findAll();
        resp.setTotalApplications(apps.size());
        
        List<Map<String, Object>> recent = new ArrayList<>();
        for(int i = 0; i < Math.min(5, apps.size()); i++) {
            Application app = apps.get(i);
            Map<String, Object> map = new HashMap<>();
            map.put("jobRole", app.getJob().getRole());
            map.put("studentName", app.getStudent().getName());
            map.put("status", app.getStatus());
            recent.add(map);
        }
        resp.setRecentApplications(recent);
        
        return resp;
    }
}

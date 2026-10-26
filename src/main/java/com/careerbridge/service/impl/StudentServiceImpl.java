package com.careerbridge.service.impl;

import com.careerbridge.dto.request.StudentProfileUpdateRequest;
import com.careerbridge.dto.response.StudentProfileDto;
import com.careerbridge.entity.Student;
import com.careerbridge.entity.User;
import com.careerbridge.repository.StudentRepository;
import com.careerbridge.repository.UserRepository;
import com.careerbridge.service.StudentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class StudentServiceImpl implements StudentService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Override
    public StudentProfileDto getProfile(String email) {
        User user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));
        Student student = studentRepository.findByUserId(user.getId()).orElseGet(() -> 
            studentRepository.save(Student.builder().user(user).name(user.getEmail().split("@")[0]).build())
        );
        
        return mapToDto(student, user.getEmail());
    }

    @Override
    public StudentProfileDto updateProfile(String email, StudentProfileUpdateRequest updateRequest) {
        User user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));
        Student student = studentRepository.findByUserId(user.getId()).orElseGet(() -> 
            studentRepository.save(Student.builder().user(user).name(user.getEmail().split("@")[0]).build())
        );

        student.setPhone(updateRequest.getPhone());
        student.setDegree(updateRequest.getDegree());
        student.setBranch(updateRequest.getBranch());
        student.setCollege(updateRequest.getCollege());
        student.setGraduationYear(updateRequest.getGraduationYear());
        student.setCgpa(updateRequest.getCgpa());
        student.setTenthPercentage(updateRequest.getTenthPercentage());
        student.setTwelfthPercentage(updateRequest.getTwelfthPercentage());
        student.setBacklogs(updateRequest.getBacklogs());
        student.setPreferredRoles(updateRequest.getPreferredRoles());
        student.setPreferredLocations(updateRequest.getPreferredLocations());
        student.setSalaryExpectations(updateRequest.getSalaryExpectations());

        Student updatedStudent = studentRepository.save(student);

        return mapToDto(updatedStudent, user.getEmail());
    }

    private StudentProfileDto mapToDto(Student student, String email) {
        StudentProfileDto dto = new StudentProfileDto();
        dto.setName(student.getName());
        dto.setEmail(email);
        dto.setPhone(student.getPhone());
        dto.setDegree(student.getDegree());
        dto.setBranch(student.getBranch());
        dto.setCollege(student.getCollege());
        dto.setGraduationYear(student.getGraduationYear());
        dto.setCgpa(student.getCgpa());
        dto.setTenthPercentage(student.getTenthPercentage());
        dto.setTwelfthPercentage(student.getTwelfthPercentage());
        dto.setBacklogs(student.getBacklogs());
        dto.setPreferredRoles(student.getPreferredRoles());
        dto.setPreferredLocations(student.getPreferredLocations());
        dto.setSalaryExpectations(student.getSalaryExpectations());
        return dto;
    }
}

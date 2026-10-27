package com.careerbridge.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * Student entity representing academic background, grades, and profile attributes.
 */
@Entity
@Table(name = "students")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Student {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    private String name;
    private String phone;
    private String degree;
    private String branch;
    private String college;
    private Integer graduationYear;
    private Double cgpa;
    
    @Column(name = "tenth_percentage")
    private Double tenthPercentage;
    
    @Column(name = "twelfth_percentage")
    private Double twelfthPercentage;
    
    private Integer backlogs;

    @Column(columnDefinition = "TEXT")
    private String preferredRoles;

    @Column(columnDefinition = "TEXT")
    private String preferredLocations;

    private String salaryExpectations;
}

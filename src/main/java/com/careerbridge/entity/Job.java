package com.careerbridge.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "jobs")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Job {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    private String role;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    private String location;
    private String salaryRange;
    private String jobUrl;
    private String source;
    
    @CreationTimestamp
    private LocalDateTime postedDate;
    
    private LocalDateTime applicationDeadline;
    private String status;
}

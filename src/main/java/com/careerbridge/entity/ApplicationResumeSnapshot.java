package com.careerbridge.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "application_resume_snapshots")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApplicationResumeSnapshot {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String resumeName;
    private Integer resumeVersion;
    private Long resumeVersionId;
    private String fileReference;
    private String contentHash;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime submissionTimestamp;
}

package com.careerbridge.repository;

import com.careerbridge.entity.Resume;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ResumeRepository extends JpaRepository<Resume, Long> {
    List<Resume> findByStudentId(Long studentId);
    Optional<Resume> findByIdAndStudentId(Long id, Long studentId);
}

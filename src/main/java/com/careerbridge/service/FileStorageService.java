package com.careerbridge.service;

import org.springframework.web.multipart.MultipartFile;

public interface FileStorageService {
    String storeFile(MultipartFile file, Long studentId);
    byte[] loadFile(String fileReference);
}

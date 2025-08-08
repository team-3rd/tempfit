package com.example.tempfit.service;

import org.springframework.web.multipart.MultipartFile;

public interface FileStorageService {
    String save(MultipartFile file, String subDir);
}

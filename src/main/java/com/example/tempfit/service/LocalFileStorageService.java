package com.example.tempfit.service;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class LocalFileStorageService implements FileStorageService {
    @Value("${upload.path}")
    private String uploadDir; // application.yml 또는 properties에서 설정

    @Value("${upload.access-url-prefix}")
    private String accessUrlPrefix; // 예: http://localhost:8080/images/

    @Override
    public String save(MultipartFile file, String subDir) {
        try {
            // 저장할 디렉토리 경로
            String targetDir = uploadDir + File.separator + subDir;
            File dir = new File(targetDir);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            // 고유한 파일 이름 생성
            String originalFilename = file.getOriginalFilename();
            String ext = originalFilename.substring(originalFilename.lastIndexOf("."));
            String fileName = UUID.randomUUID().toString() + ext;

            // 실제 저장 경로
            File savedFile = new File(dir, fileName);
            file.transferTo(savedFile);

            // 접근 가능한 URL 반환
            return accessUrlPrefix + subDir + "/" + fileName;

        } catch (IOException e) {
            throw new RuntimeException("파일 저장 실패", e);
        }
    }
}

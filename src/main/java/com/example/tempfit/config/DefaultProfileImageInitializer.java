package com.example.tempfit.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

@Component
public class DefaultProfileImageInitializer {

    @Value("${upload.path}")
    private String uploadPath;

    @PostConstruct
    public void initDefaultProfileImage() {
        String profileDirPath = uploadPath + File.separator + "profile";
        String defaultImagePath = profileDirPath + File.separator + "default.png";

        File defaultImageFile = new File(defaultImagePath);

        if (!defaultImageFile.exists()) {
            try {
                File profileDir = new File(profileDirPath);
                if (!profileDir.exists()) {
                    profileDir.mkdirs();
                }

                // 기본 이미지 리소스를 classpath에서 읽어옴
                ClassPathResource resource = new ClassPathResource("default.png");

                try (InputStream is = resource.getInputStream();
                     FileOutputStream fos = new FileOutputStream(defaultImageFile)) {

                    byte[] buffer = new byte[1024];
                    int bytesRead;
                    while ((bytesRead = is.read(buffer)) != -1) {
                        fos.write(buffer, 0, bytesRead);
                    }

                    System.out.println("[INFO] default.png 자동 생성 완료");

                }

            } catch (IOException e) {
                throw new RuntimeException("기본 프로필 이미지 생성 실패", e);
            }
        } else {
            System.out.println("[INFO] default.png 이미 존재함");
        }
    }
}

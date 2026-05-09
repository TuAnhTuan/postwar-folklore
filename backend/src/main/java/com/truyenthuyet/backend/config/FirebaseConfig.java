package com.truyenthuyet.backend.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Configuration
@Slf4j
public class FirebaseConfig {

    @Value("${firebase.service-account-path:}")
    private String serviceAccountPath;

    @Value("${firebase.service-account-json:}")
    private String serviceAccountJson;

    @PostConstruct
    public void initFirebase() throws IOException {
        if (!FirebaseApp.getApps().isEmpty()) return;

        GoogleCredentials credentials;

        if (serviceAccountJson != null && !serviceAccountJson.isBlank()) {
            // Render: đọc từ biến môi trường JSON string
            log.info("Firebase: loading credentials from environment variable");
            credentials = GoogleCredentials.fromStream(
                new ByteArrayInputStream(serviceAccountJson.getBytes(StandardCharsets.UTF_8))
            );
        } else if (serviceAccountPath != null && !serviceAccountPath.isBlank()) {
            // Local dev: đọc từ file JSON
            log.info("Firebase: loading credentials from file {}", serviceAccountPath);
            credentials = GoogleCredentials.fromStream(new FileInputStream(serviceAccountPath));
        } else {
            log.warn("Firebase: no credentials configured — authentication will not work");
            return;
        }

        FirebaseOptions options = FirebaseOptions.builder()
            .setCredentials(credentials)
            .build();

        FirebaseApp.initializeApp(options);
        log.info("Firebase initialized successfully");
    }
}

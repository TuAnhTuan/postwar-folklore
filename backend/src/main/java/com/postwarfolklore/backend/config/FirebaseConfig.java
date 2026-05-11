package com.postwarfolklore.backend.config;

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
    public void initFirebase() {
        if (!FirebaseApp.getApps().isEmpty()) return;

        try {
            GoogleCredentials credentials;

            if (serviceAccountJson != null && !serviceAccountJson.isBlank()
                    && serviceAccountJson.contains("private_key")) {
                // Render / Docker: đọc từ biến môi trường JSON string
                log.info("Firebase: loading credentials from environment variable");
                credentials = GoogleCredentials.fromStream(
                    new ByteArrayInputStream(serviceAccountJson.getBytes(StandardCharsets.UTF_8))
                );
            } else if (serviceAccountPath != null && !serviceAccountPath.isBlank()) {
                // Local dev: đọc từ file JSON
                log.info("Firebase: loading credentials from file {}", serviceAccountPath);
                credentials = GoogleCredentials.fromStream(new FileInputStream(serviceAccountPath));
            } else {
                log.warn("Firebase: no valid credentials configured — authentication disabled");
                return;
            }

            FirebaseOptions options = FirebaseOptions.builder()
                .setCredentials(credentials)
                .build();

            FirebaseApp.initializeApp(options);
            log.info("Firebase initialized successfully");

        } catch (Exception e) {
            // Không crash app — chỉ warn và chạy tiếp không có Firebase
            log.warn("Firebase: failed to initialize ({}). Authentication will be disabled. " +
                     "Set FIREBASE_SERVICE_ACCOUNT_JSON or FIREBASE_SERVICE_ACCOUNT_PATH to enable.",
                     e.getMessage());
        }
    }
}

package com.postwarfolklore.backend.controller;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Endpoint one-time để cấp quyền admin cho một Firebase UID.
 * Bảo vệ bằng SETUP_SECRET — chỉ dùng khi setup lần đầu.
 *
 * Cách dùng:
 *   curl -X POST http://localhost:8080/api/setup/admin \
 *     -H "Content-Type: application/json" \
 *     -d '{"uid":"<firebase-uid>","secret":"<SETUP_SECRET>"}'
 */
@RestController
@RequestMapping("/api/setup")
@Slf4j
public class AdminSetupController {

    @Value("${app.setup-secret:CHANGE_ME_BEFORE_USE}")
    private String setupSecret;

    @PostMapping("/admin")
    public ResponseEntity<?> grantAdmin(@RequestBody Map<String, String> body) {
        String secret = body.getOrDefault("secret", "");
        String uid    = body.getOrDefault("uid", "");

        if (!setupSecret.equals(secret)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(Map.of("error", "Invalid setup secret"));
        }

        if (uid.isBlank()) {
            return ResponseEntity.badRequest()
                .body(Map.of("error", "uid is required"));
        }

        if (FirebaseApp.getApps().isEmpty()) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(Map.of("error", "Firebase not initialized — check FIREBASE_SERVICE_ACCOUNT_JSON"));
        }

        try {
            FirebaseAuth.getInstance().setCustomUserClaims(uid, Map.of("admin", true));
            log.info("Admin claim granted to UID: {}", uid);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Admin claim set for UID: " + uid
            ));
        } catch (Exception e) {
            log.error("Failed to set admin claim: {}", e.getMessage());
            return ResponseEntity.internalServerError()
                .body(Map.of("error", e.getMessage()));
        }
    }
}

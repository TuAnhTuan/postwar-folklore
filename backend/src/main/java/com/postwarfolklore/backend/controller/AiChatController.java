package com.postwarfolklore.backend.controller;

import com.postwarfolklore.backend.service.AiChatService;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AiChatController {

    private final AiChatService aiChatService;

    // In-memory rate limit buckets (key = IP)
    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

    @GetMapping("/prompt")
    public ResponseEntity<Map<String, String>> getSystemPrompt() {
        String prompt = aiChatService.getActiveSystemPrompt();
        return ResponseEntity.ok(Map.of("prompt", prompt));
    }

    @PostMapping("/chat")
    public ResponseEntity<Map<String, String>> chat(
        @RequestBody Map<String, String> body,
        HttpServletRequest request
    ) {
        String ip = getClientIp(request);
        Bucket bucket = buckets.computeIfAbsent(ip, this::newBucket);

        if (!bucket.tryConsume(1)) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                .body(Map.of("error", "Bạn đã gửi quá nhiều tin nhắn. Vui lòng thử lại sau."));
        }

        String userMessage = body.getOrDefault("message", "");
        if (userMessage.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Tin nhắn không được để trống."));
        }

        String reply = aiChatService.chat(userMessage);
        return ResponseEntity.ok(Map.of("reply", reply));
    }

    // Admin: cập nhật system prompt
    @PutMapping("/admin/prompt")
    public ResponseEntity<Void> updatePrompt(@RequestBody Map<String, String> body) {
        aiChatService.updateSystemPrompt(body.get("promptContent"));
        return ResponseEntity.ok().build();
    }

    private Bucket newBucket(String ip) {
        Bandwidth limit = Bandwidth.classic(10, Refill.greedy(10, Duration.ofHours(1)));
        return Bucket.builder().addLimit(limit).build();
    }

    private String getClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        return (forwarded != null) ? forwarded.split(",")[0].trim() : request.getRemoteAddr();
    }
}

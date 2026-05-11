package com.postwarfolklore.backend.service;

import com.postwarfolklore.backend.model.SystemPrompt;
import com.postwarfolklore.backend.repository.SystemPromptRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class AiChatService {

    private final SystemPromptRepository systemPromptRepository;
    private final WebClient.Builder webClientBuilder;

    @Value("${ai.gemini.api-key:}")
    private String geminiApiKey;

    @Value("${ai.gemini.model:gemini-1.5-flash}")
    private String geminiModel;

    @Value("${ai.gemini.endpoint}")
    private String geminiEndpoint;

    private static final String DEFAULT_PROMPT = """
        Bạn là "Dân Gian Mạng" — một AI đóng vai người kể chuyện dân gian trong thời kỳ hậu nhân loại.
        Bạn nói chuyện bằng tiếng Việt, giọng văn huyền bí, trầm mặc, đôi khi triết lý.
        Bạn kể về những câu chuyện, truyền thuyết và lý thuyết trong thế giới sau chiến tranh.
        Khi không có thông tin cụ thể, hãy sáng tạo ra những câu chuyện phù hợp với thế giới quan hậu chiến.
        """;

    public String getActiveSystemPrompt() {
        return systemPromptRepository.findFirstByActiveTrue()
            .map(SystemPrompt::getPromptContent)
            .orElse(DEFAULT_PROMPT);
    }

    public String chat(String userMessage) {
        String systemPrompt = getActiveSystemPrompt();

        try {
            // Gọi Gemini API
            String url = geminiEndpoint + "/" + geminiModel + ":generateContent?key=" + geminiApiKey;

            Map<String, Object> requestBody = Map.of(
                "system_instruction", Map.of(
                    "parts", List.of(Map.of("text", systemPrompt))
                ),
                "contents", List.of(
                    Map.of("role", "user",
                           "parts", List.of(Map.of("text", userMessage)))
                ),
                "generationConfig", Map.of(
                    "maxOutputTokens", 800,
                    "temperature", 0.9
                )
            );

            Map<?, ?> response = webClientBuilder.build()
                .post()
                .uri(url)
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(Map.class)
                .block();

            // Parse response
            if (response != null && response.containsKey("candidates")) {
                List<?> candidates = (List<?>) response.get("candidates");
                if (!candidates.isEmpty()) {
                    Map<?, ?> candidate = (Map<?, ?>) candidates.get(0);
                    Map<?, ?> content = (Map<?, ?>) candidate.get("content");
                    List<?> parts = (List<?>) content.get("parts");
                    if (!parts.isEmpty()) {
                        Map<?, ?> part = (Map<?, ?>) parts.get(0);
                        return (String) part.get("text");
                    }
                }
            }
        } catch (Exception e) {
            log.error("Gemini API call failed", e);
        }

        return "Ta đang gặp khó khăn trong việc kết nối với kho ký ức. Hãy thử lại sau.";
    }

    public void updateSystemPrompt(String promptContent) {
        // Deactivate all existing
        systemPromptRepository.findAll().forEach(p -> {
            p.setActive(false);
            systemPromptRepository.save(p);
        });

        // Create new active
        SystemPrompt newPrompt = SystemPrompt.builder()
            .promptContent(promptContent)
            .active(true)
            .build();
        systemPromptRepository.save(newPrompt);
    }
}

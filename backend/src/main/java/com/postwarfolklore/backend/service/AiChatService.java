package com.postwarfolklore.backend.service;

import com.postwarfolklore.backend.dto.ChatResponse;
import com.postwarfolklore.backend.model.Post;
import com.postwarfolklore.backend.model.SystemPrompt;
import com.postwarfolklore.backend.repository.PostRepository;
import com.postwarfolklore.backend.repository.SystemPromptRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AiChatService {

    private final SystemPromptRepository systemPromptRepository;
    private final PostRepository postRepository;
    private final WebClient.Builder webClientBuilder;

    @Value("${ai.gemini.api-key:}")
    private String geminiApiKey;

    @Value("${ai.gemini.model:gemini-1.5-flash}")
    private String geminiModel;

    @Value("${ai.gemini.endpoint}")
    private String geminiEndpoint;

    private static final String NOT_FOUND_REPLY =
        "Mình chưa có tư liệu về điều này trong kho lưu trữ. " +
        "Hãy thử hỏi về các câu chuyện và lý thuyết đã được lưu giữ tại đây.";

    // ── PUBLIC API ──────────────────────────────────────────────

    public ChatResponse chat(String userMessage) {
        List<Post> allPosts = postRepository.findAllByOrderByCreatedAtDesc(Pageable.unpaged()).getContent();

        if (allPosts.isEmpty()) {
            return ChatResponse.builder()
                .reply("Kho lưu trữ hiện đang trống. Chưa có tư liệu nào được ghi lại.")
                .found(false)
                .build();
        }

        boolean hasGemini = geminiApiKey != null && !geminiApiKey.isBlank();

        // Bước 1 — Retrieve: tìm bài viết phù hợp nhất
        Optional<Post> matched = hasGemini
            ? findByGemini(userMessage, allPosts)
            : findByKeyword(userMessage, allPosts);

        if (matched.isEmpty()) {
            return buildNotFoundResponse();
        }

        Post post = matched.get();

        // Bước 2 — Extract: tìm đoạn liên quan nhất trong bài, trả về nội dung thật
        String excerpt = extractRelevantExcerpt(userMessage, post.getContent());
        String reply = "📜 " + post.getTitle() + "\n\n" + excerpt;

        return ChatResponse.builder()
            .reply(reply)
            .postId(post.getId())
            .postTitle(post.getTitle())
            .found(true)
            .build();
    }

    public String getActiveSystemPrompt() {
        return systemPromptRepository.findFirstByActiveTrue()
            .map(SystemPrompt::getPromptContent)
            .orElse("Bạn là Dân Gian Mạng — người kể chuyện dân gian trong thời kỳ hậu nhân loại.");
    }

    public void updateSystemPrompt(String promptContent) {
        systemPromptRepository.findAll().forEach(p -> {
            p.setActive(false);
            systemPromptRepository.save(p);
        });
        SystemPrompt newPrompt = SystemPrompt.builder()
            .promptContent(promptContent)
            .active(true)
            .build();
        systemPromptRepository.save(newPrompt);
    }

    // ── BƯỚC 1: RETRIEVE — tìm bài phù hợp ────────────────────

    /**
     * Gửi danh sách tiêu đề lên Gemini, nhận về UUID của bài phù hợp nhất.
     */
    private Optional<Post> findByGemini(String userMessage, List<Post> posts) {
        String catalog = posts.stream()
            .map(p -> "ID:" + p.getId() + " | Tiêu đề: " + p.getTitle()
                    + " | Loại: " + p.getType().name())
            .collect(Collectors.joining("\n"));

        String matcherPrompt = """
            Bạn là công cụ tìm kiếm nội dung. Nhiệm vụ duy nhất của bạn là tìm bài viết phù hợp nhất với câu hỏi.

            DANH SÁCH BÀI VIẾT:
            """ + catalog + """

            CÂU HỎI NGƯỜI DÙNG: """ + userMessage + """

            QUY TẮC:
            - Chỉ trả về DUY NHẤT UUID của bài viết phù hợp nhất (ví dụ: 550e8400-e29b-41d4-a716-446655440000)
            - Nếu không có bài nào liên quan, trả về đúng chữ: NO_MATCH
            - Không giải thích, không thêm bất kỳ chữ nào khác
            """;

        try {
            String url = geminiEndpoint + "/" + geminiModel + ":generateContent?key=" + geminiApiKey;
            Map<String, Object> requestBody = Map.of(
                "contents", List.of(Map.of("role", "user", "parts", List.of(Map.of("text", matcherPrompt)))),
                "generationConfig", Map.of("maxOutputTokens", 50, "temperature", 0.0)
            );
            Map<?, ?> response = webClientBuilder.build()
                .post().uri(url).bodyValue(requestBody)
                .retrieve().bodyToMono(Map.class).block();

            String rawId = extractText(response).trim();
            log.debug("Gemini matched post ID: {}", rawId);

            if (rawId.isBlank() || rawId.equals("NO_MATCH")) return Optional.empty();

            UUID matchedId = UUID.fromString(rawId);
            return posts.stream().filter(p -> p.getId().equals(matchedId)).findFirst();

        } catch (IllegalArgumentException e) {
            log.warn("Gemini returned non-UUID, falling back to keyword search");
            return findByKeyword(userMessage, posts);
        } catch (Exception e) {
            String msg = e.getMessage();
            if (msg != null && msg.contains("429")) {
                log.warn("Gemini rate limit hit, falling back to keyword search");
            } else {
                log.warn("Gemini matcher error: {}, falling back to keyword search", msg);
            }
            return findByKeyword(userMessage, posts);
        }
    }

    // ── BƯỚC 2: EXTRACT — tìm đoạn liên quan nhất ─────────────

    /**
     * Tìm đoạn văn trong nội dung bài chứa từ khóa liên quan đến câu hỏi.
     * Trả về nội dung thật từ bài viết, không tự sinh.
     */
    private String extractRelevantExcerpt(String userMessage, String content) {
        // Tách câu hỏi thành các từ khóa (bỏ từ ngắn < 3 ký tự)
        String[] keywords = userMessage.toLowerCase()
            .split("[\\s,?.!]+");

        // Tách content thành các đoạn (theo dòng trống hoặc xuống dòng)
        String[] paragraphs = content.split("\n+");

        // Tìm đoạn có điểm số cao nhất (nhiều từ khóa khớp nhất)
        String bestParagraph = null;
        int bestScore = -1;

        for (String para : paragraphs) {
            if (para.trim().length() < 20) continue; // bỏ đoạn quá ngắn
            String paraLower = para.toLowerCase();
            int score = 0;
            for (String kw : keywords) {
                if (kw.length() >= 2 && paraLower.contains(kw)) score++;
            }
            if (score > bestScore) {
                bestScore = score;
                bestParagraph = para.trim();
            }
        }

        // Nếu không tìm được đoạn liên quan → trả về 500 ký tự đầu
        if (bestParagraph == null || bestScore == 0) {
            return content.length() > 500
                ? content.substring(0, 500) + "..."
                : content;
        }

        // Trả về đoạn tìm được, tối đa 600 ký tự
        return bestParagraph.length() > 600
            ? bestParagraph.substring(0, 600) + "..."
            : bestParagraph;
    }

    // ── KEYWORD FALLBACK ────────────────────────────────────────

    private Optional<Post> findByKeyword(String userMessage, List<Post> posts) {
        String keyword = userMessage.toLowerCase().trim();

        Optional<Post> byTitle = posts.stream()
            .filter(p -> p.getTitle().toLowerCase().contains(keyword))
            .findFirst();
        if (byTitle.isPresent()) return byTitle;

        return posts.stream()
            .filter(p -> p.getContent().toLowerCase().contains(keyword))
            .findFirst();
    }

    // ── HELPERS ─────────────────────────────────────────────────

    private ChatResponse buildNotFoundResponse() {
        return ChatResponse.builder()
            .reply(NOT_FOUND_REPLY)
            .found(false)
            .build();
    }

    private String extractText(Map<?, ?> response) {
        if (response == null) return "";
        try {
            List<?> candidates = (List<?>) response.get("candidates");
            if (candidates == null || candidates.isEmpty()) return "";
            Map<?, ?> candidate = (Map<?, ?>) candidates.get(0);
            Map<?, ?> content   = (Map<?, ?>) candidate.get("content");
            List<?> parts       = (List<?>) content.get("parts");
            if (parts == null || parts.isEmpty()) return "";
            return (String) ((Map<?, ?>) parts.get(0)).get("text");
        } catch (Exception e) {
            return "";
        }
    }
}

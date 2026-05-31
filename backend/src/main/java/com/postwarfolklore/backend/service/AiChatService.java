package com.postwarfolklore.backend.service;

import com.postwarfolklore.backend.dto.ChatResponse;
import com.postwarfolklore.backend.model.Comment;
import com.postwarfolklore.backend.model.Post;
import com.postwarfolklore.backend.model.SystemPrompt;
import com.postwarfolklore.backend.repository.CommentRepository;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AiChatService {

    private final SystemPromptRepository systemPromptRepository;
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final WebClient.Builder webClientBuilder;

    @Value("${ai.groq.api-key:}")
    private String groqApiKey;

    @Value("${ai.groq.model:llama-3.3-70b-versatile}")
    private String groqModel;

    @Value("${ai.groq.endpoint:https://api.groq.com/openai/v1/chat/completions}")
    private String groqEndpoint;

    private static final Pattern UUID_PATTERN =
        Pattern.compile("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}",
            Pattern.CASE_INSENSITIVE);

    private static final String NOT_FOUND_REPLY =
        "Mình chưa có tư liệu về điều này trong kho lưu trữ. " +
        "Hãy thử hỏi về các câu chuyện và lý thuyết đã được lưu giữ tại đây.";

    // ── SELF-INTRODUCTION KEYWORDS (hỏi về chatbot) ─────────────
    private static final List<String> SELF_KEYWORDS = List.of(
        "bạn là ai", "mày là ai", "mình là ai", "bạn tên gì",
        "bạn làm gì", "bạn có thể làm gì", "bạn giúp được gì",
        "bạn biết gì", "bạn có biết", "bạn là gì",
        "ai tạo ra bạn", "bạn được tạo ra", "chatbot này",
        "trợ lý này", "dân gian mạng là ai", "dân gian mạng là gì"
    );

    // ── OFF-TOPIC GUARD — từ khóa chắc chắn ngoài phạm vi ───────
    private static final List<String> OFF_TOPIC_KEYWORDS = List.of(
        "dự án này", "trong dự án", "lập trình", "code", "github",
        "deploy", "server", "database", "frontend", "backend",
        "thời tiết", "bóng đá", "nấu ăn", "du lịch", "kinh tế",
        "chính trị", "giá vàng", "chứng khoán", "tin tức",
        "bài tập", "trường học", "toán học", "hóa học"
    );

    // ── META-QUESTION KEYWORDS (hỏi về kho lưu trữ nói chung) ──
    // Chỉ match khi rõ ràng hỏi tổng quan, không phải câu hỏi phân tích cụ thể
    private static final List<String> META_KEYWORDS = List.of(
        "có những loại", "những loại truyện", "có những câu",
        "bao nhiêu câu", "bao nhiêu bài", "liệt kê tất cả",
        "danh sách tất cả", "kho lưu có gì", "gồm những gì",
        "có gì trong kho", "giới thiệu kho", "có bao nhiêu truyện",
        "tất cả truyện", "tất cả bài viết"
    );

    // ── ANALYTICS KEYWORDS (hỏi thống kê, xếp hạng) ────────────
    private static final List<String> ANALYTICS_KEYWORDS = List.of(
        "nhiều bình luận", "được bình luận nhiều", "phổ biến nhất",
        "nhiều người đọc", "nhiều lượt", "xếp hạng", "top", "hạng nhất",
        "được xem nhiều", "nhiều nhất", "ít nhất", "ít bình luận"
    );

    // ── CONCEPT QUESTION KEYWORDS (hỏi về khái niệm trang web) ──
    private static final List<String> CONCEPT_THEORY_KEYWORDS = List.of(
        "lý thuyết là gì", "nếp nghĩ là gì", "nếp nghĩ",
        "lý thuyết có nghĩa", "lý thuyết ở đây", "lý thuyết trong trang"
    );
    private static final List<String> CONCEPT_LEGEND_KEYWORDS = List.of(
        "lời kể là gì", "lời kể có nghĩa", "lời kể ở đây",
        "truyền thuyết là gì", "lời kể trong trang"
    );
    private static final List<String> CONCEPT_DIFF_KEYWORDS = List.of(
        "khác nhau giữa", "khác gì nhau", "phân biệt", "so sánh",
        "lý thuyết và lời kể", "lời kể và lý thuyết",
        "nếp nghĩ và lời kể", "lời kể và nếp nghĩ",
        "lý thuyết với lời kể", "hai loại"
    );
    private static final List<String> CONCEPT_SITE_KEYWORDS = List.of(
        "trang web này là gì", "hoài tự là gì", "trang này về",
        "folklore là gì", "dân gian hậu chiến", "hậu chiến là gì",
        "mục đích trang", "trang web về gì", "đây là gì"
    );

    // ── CASUAL REQUEST KEYWORDS (hỏi ngẫu nhiên, không biết muốn gì) ──
    private static final List<String> CASUAL_KEYWORDS = List.of(
        "kể cho mình nghe", "kể cho tôi nghe", "kể một câu",
        "một câu chuyện hay", "câu chuyện hay", "chuyện nào hay",
        "chuyện gì hay", "gợi ý cho mình", "gợi ý một",
        "muốn nghe", "muốn đọc", "đọc gì bây giờ", "ngẫu nhiên",
        "bất kỳ câu", "kể gì đi", "có câu chuyện nào không"
    );

    // ── EMOTION → CONTENT KEYWORDS MAP ──────────────────────────
    private static final Map<String, List<String>> EMOTION_MAP = Map.of(
        "buồn",      List.of("mất", "hi sinh", "qua đời", "chết", "đau", "thương", "khóc", "tang", "tử trận"),
        "kinh dị",   List.of("ma", "linh hồn", "bóng tối", "rùng rợn", "ám", "quỷ", "kỳ lạ", "hiện về"),
        "bí ẩn",     List.of("bí ẩn", "không giải thích", "kỳ lạ", "điều kỳ", "lạ lùng", "bí mật", "không rõ"),
        "cảm động",  List.of("gia đình", "tìm mộ", "đoàn tụ", "nhận ra", "gặp lại", "yêu thương", "nhớ thương"),
        "hào hùng",  List.of("chiến đấu", "anh hùng", "dũng cảm", "bảo vệ", "chiến tranh", "trận", "đấu tranh")
    );

    // Map từ user dùng → key trong EMOTION_MAP
    private static final Map<String, String> USER_EMOTION_WORDS = Map.ofEntries(
        Map.entry("buồn",      "buồn"),
        Map.entry("bi thương", "buồn"),
        Map.entry("đau lòng",  "buồn"),
        Map.entry("rùng rợn",  "kinh dị"),
        Map.entry("ma mị",     "kinh dị"),
        Map.entry("ghê",       "kinh dị"),
        Map.entry("bí ẩn",     "bí ẩn"),
        Map.entry("huyền bí",  "bí ẩn"),
        Map.entry("kỳ lạ",     "bí ẩn"),
        Map.entry("cảm động",  "cảm động"),
        Map.entry("xúc động",  "cảm động"),
        Map.entry("hào hùng",  "hào hùng"),
        Map.entry("anh hùng",  "hào hùng")
    );

    // ── PUBLIC API ──────────────────────────────────────────────

    public ChatResponse chat(String userMessage) {
        List<Post> allPosts = postRepository.findAllByOrderByCreatedAtDesc(Pageable.unpaged()).getContent();

        if (allPosts.isEmpty()) {
            return ChatResponse.builder()
                .reply("Kho lưu trữ hiện đang trống. Chưa có tư liệu nào được ghi lại.")
                .found(false)
                .build();
        }

        // Hỏi về bản thân chatbot
        if (isSelfQuestion(userMessage)) {
            return buildSelfIntroResponse();
        }

        // Câu hỏi ngoài phạm vi trang web
        if (isOffTopic(userMessage)) {
            return buildOffTopicResponse();
        }

        // Kiểm tra meta-question trước (hỏi về kho lưu trữ, không hỏi bài cụ thể)
        if (isMetaQuestion(userMessage)) {
            return buildOverviewResponse(allPosts);
        }

        // Câu hỏi analytics (thống kê, xếp hạng)
        if (isAnalyticsQuestion(userMessage)) {
            return buildAnalyticsResponse(userMessage, allPosts);
        }

        // Concept question — hỏi về khái niệm, ý nghĩa của trang web
        Optional<ChatResponse> conceptResponse = buildConceptResponse(userMessage);
        if (conceptResponse.isPresent()) {
            return conceptResponse.get();
        }

        // Casual request — không biết muốn gì, gợi ý ngẫu nhiên
        if (isCasualRequest(userMessage)) {
            return buildCasualResponse(allPosts);
        }

        // Emotion-based — "câu chuyện buồn", "chuyện rùng rợn"...
        Optional<ChatResponse> emotionResponse = buildEmotionResponse(userMessage, allPosts);
        if (emotionResponse.isPresent()) {
            return emotionResponse.get();
        }

        boolean hasGroq = groqApiKey != null && !groqApiKey.isBlank();

        // Bước 1 — Retrieve: tìm bài viết phù hợp nhất
        Optional<Post> matched = hasGroq
            ? findByGroq(userMessage, allPosts)
            : findByKeyword(userMessage, allPosts);

        if (matched.isEmpty()) {
            return buildNotFoundResponse();
        }

        Post post = matched.get();

        // Bước 2 — Generate: dùng Groq giải thích dựa trên nội dung bài
        // Fallback về excerpt thô nếu Groq không khả dụng
        String reply;
        if (hasGroq) {
            String generated = generateExplanation(userMessage, post);
            reply = generated != null
                ? "📜 " + post.getTitle() + "\n\n" + generated
                : "📜 " + post.getTitle() + "\n\n" + extractRelevantExcerpt(userMessage, post.getContent());
        } else {
            String excerpt = extractRelevantExcerpt(userMessage, post.getContent());
            reply = "📜 " + post.getTitle() + "\n\n_(Mình chưa có thêm thông tin về điều này — bạn có thể đọc đầy đủ bên dưới.)_\n\n" + excerpt;
        }

        return ChatResponse.builder()
            .reply(reply)
            .postId(post.getId())
            .postTitle(post.getTitle())
            .found(true)
            .build();
    }

    private boolean isMetaQuestion(String message) {
        String lower = message.toLowerCase();
        return META_KEYWORDS.stream().anyMatch(lower::contains);
    }

    private boolean isAnalyticsQuestion(String message) {
        String lower = message.toLowerCase();
        return ANALYTICS_KEYWORDS.stream().anyMatch(lower::contains);
    }

    private boolean isSelfQuestion(String message) {
        String lower = message.toLowerCase();
        return SELF_KEYWORDS.stream().anyMatch(lower::contains);
    }

    private boolean isOffTopic(String message) {
        String lower = message.toLowerCase();
        return OFF_TOPIC_KEYWORDS.stream().anyMatch(lower::contains);
    }

    private ChatResponse buildSelfIntroResponse() {
        String reply = """
            👋 Mình là Dân Gian Mạng — người giữ kho lưu trữ dân gian hậu chiến của Hoài tự.

            Mình có thể giúp bạn:
            • Tìm và kể lại các câu chuyện Lời kể từ vùng đất miền Trung
            • Giải thích các lý thuyết và phân tích trong mục Nếp nghĩ
            • Gợi ý câu chuyện phù hợp với cảm xúc bạn đang tìm kiếm
            • Trả lời câu hỏi về bất kỳ tư liệu nào trong kho lưu trữ

            Hãy hỏi mình về một câu chuyện cụ thể, hoặc chỉ cần nói "kể cho mình nghe một chuyện hay" nhé.
            """;
        return ChatResponse.builder()
            .reply(reply.stripIndent().trim())
            .found(true)
            .build();
    }

    private ChatResponse buildOffTopicResponse() {
        String reply = "Mình chỉ có thể trả lời về những câu chuyện và tư liệu dân gian hậu chiến trong kho lưu trữ này. " +
            "Hãy hỏi mình về Lời kể, Nếp nghĩ, hay bất kỳ câu chuyện nào bạn tò mò nhé.";
        return ChatResponse.builder()
            .reply(reply)
            .found(false)
            .build();
    }

    private Optional<ChatResponse> buildConceptResponse(String message) {
        String lower = message.toLowerCase();

        boolean isDiff   = CONCEPT_DIFF_KEYWORDS.stream().anyMatch(lower::contains);
        boolean isTheory = CONCEPT_THEORY_KEYWORDS.stream().anyMatch(lower::contains);
        boolean isLegend = CONCEPT_LEGEND_KEYWORDS.stream().anyMatch(lower::contains);
        boolean isSite   = CONCEPT_SITE_KEYWORDS.stream().anyMatch(lower::contains);

        if (!isDiff && !isTheory && !isLegend && !isSite) return Optional.empty();

        String reply;

        if (isDiff) {
            reply = """
                📖 Hai mục trong kho lưu trữ này:

                🧠 Nếp nghĩ (Lý thuyết)
                Là những bài viết phân tích, lý giải — cố gắng đặt tên và hiểu các hiện tượng \
                dân gian hậu chiến bằng ngôn ngữ học thuật hoặc cá nhân. \
                Ví dụ: tại sao người ta tin vào báo mộng, cơ chế tâm lý đằng sau việc tìm mộ, \
                hay cách ký ức chiến tranh được truyền miệng qua các thế hệ.

                🗺️ Lời kể (Truyền thuyết)
                Là những câu chuyện được ghi lại — lời kể trực tiếp từ người trong cuộc hoặc \
                được truyền lại. Không phán xét thật/giả, chỉ lưu giữ như một tư liệu sống. \
                Ví dụ: chuyện người lính dẫn đường trong mơ, ngôi mộ tự nhiên hiện ra, \
                hay tiếng kêu không giải thích được ở vùng chiến trường cũ.

                Nếu bạn muốn hiểu → đọc Nếp nghĩ.
                Nếu bạn muốn nghe → đọc Lời kể.
                """;
        } else if (isTheory) {
            reply = """
                🧠 Nếp nghĩ là gì?

                Đây là mục chứa các bài viết phân tích và lý giải về văn hóa dân gian hậu chiến. \
                Thay vì kể lại câu chuyện, các bài Nếp nghĩ cố gắng đặt câu hỏi: \
                Tại sao người ta tin? Hiện tượng này phản ánh điều gì về ký ức tập thể? \
                Ranh giới giữa niềm tin và sự thật ở đây là gì?

                Nó không phủ nhận cũng không khẳng định — chỉ cố gắng hiểu.
                """;
        } else if (isLegend) {
            reply = """
                🗺️ Lời kể là gì?

                Đây là mục chứa các câu chuyện được ghi lại từ thực tế hoặc truyền miệng, \
                liên quan đến vùng đất và con người sau chiến tranh. \
                Những câu chuyện về người lính, về linh hồn, về sự dẫn đường kỳ lạ, \
                về hành trình tìm lại người thân đã mất.

                Lời kể không phán xét thật hay hư — nó chỉ giữ lại những gì đã được kể.
                """;
        } else {
            reply = """
                🌿 Hoài tự — Kho lưu trữ dân gian hậu chiến

                Đây là nơi lưu giữ các câu chuyện và suy nghĩ về văn hóa dân gian \
                ở vùng đất miền Trung Việt Nam sau chiến tranh — nơi ký ức, \
                tâm linh và lịch sử đan xen với nhau.

                Kho lưu trữ có hai phần:
                • 🧠 Nếp nghĩ — phân tích, lý giải các hiện tượng
                • 🗺️ Lời kể — câu chuyện được ghi lại từ người thật, việc thật

                Hãy hỏi mình về bất kỳ câu chuyện nào bạn muốn biết thêm.
                """;
        }

        return Optional.of(ChatResponse.builder()
            .reply(reply.stripIndent().trim())
            .found(true)
            .build());
    }

    private boolean isCasualRequest(String message) {
        String lower = message.toLowerCase();
        return CASUAL_KEYWORDS.stream().anyMatch(lower::contains);
    }

    /** Trả về bài được bình luận nhiều nhất (nổi bật), hoặc bài mới nhất nếu chưa có bình luận */
    private ChatResponse buildCasualResponse(List<Post> allPosts) {
        List<Post> legends = allPosts.stream()
            .filter(p -> p.getType() == Post.PostType.LEGEND)
            .toList();
        List<Post> pool = legends.isEmpty() ? allPosts : legends;

        // Ưu tiên bài có nhiều bình luận nhất
        List<UUID> ids = pool.stream().map(Post::getId).toList();
        List<Object[]> counts = commentRepository.countApprovedByPostIds(ids);
        Map<UUID, Long> countMap = new java.util.HashMap<>();
        counts.forEach(row -> countMap.put((UUID) row[0], (Long) row[1]));

        Post featured = pool.stream()
            .max(java.util.Comparator.comparingLong(p -> countMap.getOrDefault(p.getId(), 0L)))
            .orElse(pool.get(0));

        String excerpt = extractRelevantExcerpt("", featured.getContent());
        String reply = "✨ Mình gợi ý câu chuyện này:\n\n📜 " + featured.getTitle() + "\n\n" + excerpt;

        return ChatResponse.builder()
            .reply(reply)
            .postId(featured.getId())
            .postTitle(featured.getTitle())
            .found(true)
            .build();
    }

    /** Tìm bài theo cảm xúc người dùng mô tả. Trả về empty nếu không detect được emotion. */
    private Optional<ChatResponse> buildEmotionResponse(String userMessage, List<Post> allPosts) {
        String lower = userMessage.toLowerCase();

        // Tìm emotion key từ những gì user gõ
        String emotionKey = USER_EMOTION_WORDS.entrySet().stream()
            .filter(e -> lower.contains(e.getKey()))
            .map(Map.Entry::getValue)
            .findFirst()
            .orElse(null);

        if (emotionKey == null) return Optional.empty();

        List<String> contentKeywords = EMOTION_MAP.get(emotionKey);
        if (contentKeywords == null) return Optional.empty();

        log.debug("Emotion search: key={}, keywords={}", emotionKey, contentKeywords);

        // Tìm bài có nhiều keyword cảm xúc nhất trong content
        Post best = allPosts.stream()
            .max(java.util.Comparator.comparingLong(p -> {
                String content = p.getContent().toLowerCase();
                return contentKeywords.stream().filter(content::contains).count();
            }))
            .filter(p -> {
                String content = p.getContent().toLowerCase();
                return contentKeywords.stream().anyMatch(content::contains);
            })
            .orElse(null);

        if (best == null) {
            String reply = "Mình chưa tìm được câu chuyện mang cảm xúc \"" + emotionKey +
                "\" trong kho lưu trữ. Hãy thử hỏi cụ thể hơn nhé.";
            return Optional.of(ChatResponse.builder().reply(reply).found(false).build());
        }

        String excerpt = extractRelevantExcerpt(userMessage, best.getContent());
        String reply = "🎭 Câu chuyện mang cảm xúc " + emotionKey + ":\n\n📜 " + best.getTitle() + "\n\n" + excerpt;

        return Optional.of(ChatResponse.builder()
            .reply(reply)
            .postId(best.getId())
            .postTitle(best.getTitle())
            .found(true)
            .build());
    }

    private ChatResponse buildAnalyticsResponse(String userMessage, List<Post> allPosts) {
        String lower = userMessage.toLowerCase();

        // Lấy số bình luận cho từng bài
        List<UUID> postIds = allPosts.stream().map(Post::getId).toList();
        List<Object[]> counts = commentRepository.countApprovedByPostIds(postIds);
        Map<UUID, Long> commentCountMap = new java.util.HashMap<>();
        for (Object[] row : counts) {
            commentCountMap.put((UUID) row[0], (Long) row[1]);
        }

        // Xếp bài theo số bình luận giảm dần
        List<Post> ranked = allPosts.stream()
            .sorted((a, b) -> Long.compare(
                commentCountMap.getOrDefault(b.getId(), 0L),
                commentCountMap.getOrDefault(a.getId(), 0L)))
            .toList();

        boolean wantMost = !lower.contains("ít nhất") && !lower.contains("ít bình luận");

        StringBuilder sb = new StringBuilder();
        if (wantMost) {
            sb.append("💬 Bài viết được bình luận nhiều nhất:\n\n");
            int shown = 0;
            for (Post p : ranked) {
                long cnt = commentCountMap.getOrDefault(p.getId(), 0L);
                if (cnt == 0 && shown > 0) break; // dừng khi hết bình luận
                sb.append(shown + 1).append(". 📜 ").append(p.getTitle())
                  .append(" — ").append(cnt).append(" bình luận\n");
                shown++;
                if (shown >= 5) break;
            }
            if (shown == 0 || commentCountMap.isEmpty()) {
                sb.append("Chưa có bài nào nhận được bình luận.");
            }
        } else {
            sb.append("💬 Bài viết ít bình luận nhất:\n\n");
            List<Post> reversed = new java.util.ArrayList<>(ranked);
            java.util.Collections.reverse(reversed);
            for (int i = 0; i < Math.min(5, reversed.size()); i++) {
                Post p = reversed.get(i);
                long cnt = commentCountMap.getOrDefault(p.getId(), 0L);
                sb.append(i + 1).append(". 📜 ").append(p.getTitle())
                  .append(" — ").append(cnt).append(" bình luận\n");
            }
        }

        return ChatResponse.builder()
            .reply(sb.toString())
            .found(true)
            .build();
    }

    private ChatResponse buildOverviewResponse(List<Post> allPosts) {
        long legendCount = allPosts.stream()
            .filter(p -> p.getType() == Post.PostType.LEGEND).count();
        long theoryCount = allPosts.stream()
            .filter(p -> p.getType() == Post.PostType.THEORY).count();

        StringBuilder sb = new StringBuilder();
        sb.append("📚 Kho lưu trữ hiện có ").append(allPosts.size()).append(" tư liệu:\n\n");

        if (legendCount > 0) {
            sb.append("🗺️ Lời kể (").append(legendCount).append(" câu chuyện):\n");
            allPosts.stream()
                .filter(p -> p.getType() == Post.PostType.LEGEND)
                .forEach(p -> sb.append("  • ").append(p.getTitle()).append("\n"));
            sb.append("\n");
        }

        if (theoryCount > 0) {
            sb.append("🧠 Nếp nghĩ (").append(theoryCount).append(" lý thuyết):\n");
            allPosts.stream()
                .filter(p -> p.getType() == Post.PostType.THEORY)
                .forEach(p -> sb.append("  • ").append(p.getTitle()).append("\n"));
        }

        sb.append("\nHãy hỏi cụ thể về bất kỳ câu chuyện nào để mình kể thêm.");

        return ChatResponse.builder()
            .reply(sb.toString())
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
     * Gửi danh sách tiêu đề lên Groq, nhận về UUID của bài phù hợp nhất.
     */
    private Optional<Post> findByGroq(String userMessage, List<Post> posts) {
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
            Map<String, Object> requestBody = Map.of(
                "model", groqModel,
                "messages", List.of(Map.of("role", "user", "content", matcherPrompt)),
                "max_tokens", 50,
                "temperature", 0.0
            );

            Map<?, ?> response = webClientBuilder.build()
                .post().uri(groqEndpoint)
                .header("Authorization", "Bearer " + groqApiKey)
                .header("Content-Type", "application/json")
                .bodyValue(requestBody)
                .retrieve().bodyToMono(Map.class).block();

            String rawId = extractGroqText(response);
            log.debug("Groq raw response: '{}'", rawId);

            if (rawId == null || rawId.isBlank() || rawId.contains("NO_MATCH")) {
                log.debug("Groq returned NO_MATCH, falling back to keyword search");
                return findByKeyword(userMessage, posts);
            }

            // Dùng regex thay vì UUID.fromString() để xử lý trường hợp Groq thêm text thừa
            Matcher uuidMatcher = UUID_PATTERN.matcher(rawId);
            if (!uuidMatcher.find()) {
                log.warn("Groq response contains no UUID: '{}', falling back to keyword search", rawId);
                return findByKeyword(userMessage, posts);
            }

            UUID matchedId = UUID.fromString(uuidMatcher.group());
            log.debug("Groq matched post ID: {}", matchedId);
            Optional<Post> found = posts.stream().filter(p -> p.getId().equals(matchedId)).findFirst();
            if (found.isEmpty()) {
                log.warn("Groq UUID {} not found in post list, falling back to keyword search", matchedId);
                return findByKeyword(userMessage, posts);
            }
            return found;

        } catch (IllegalArgumentException e) {
            log.warn("Groq UUID parse error: {}, falling back to keyword search", e.getMessage());
            return findByKeyword(userMessage, posts);
        } catch (Exception e) {
            String msg = e.getMessage();
            if (msg != null && msg.contains("429")) {
                log.warn("Groq rate limit hit, falling back to keyword search");
            } else {
                log.warn("Groq matcher error: {}, falling back to keyword search", msg);
            }
            return findByKeyword(userMessage, posts);
        }
    }

    // ── BƯỚC 2: GENERATE — giải thích bằng Groq ────────────────

    /**
     * Gọi Groq lần 2 để tạo câu trả lời tự nhiên dựa trên nội dung bài viết.
     * Trả về null nếu gặp lỗi → caller fallback về excerpt thô.
     */
    private String generateExplanation(String userQuestion, Post post) {
        String systemPrompt = getActiveSystemPrompt();

        // Giới hạn content gửi lên để tiết kiệm token (tối đa 3000 ký tự)
        String content = post.getContent().length() > 3000
            ? post.getContent().substring(0, 3000) + "..."
            : post.getContent();

        String prompt = """
            Bạn là %s

            TƯ LIỆU GỐC — chỉ được trả lời dựa trên tư liệu này, không tự bịa:
            Tiêu đề: %s
            Nội dung:
            %s

            CÂU HỎI: %s

            YÊU CẦU:
            - Trả lời bằng tiếng Việt, giọng điệu của người kể chuyện dân gian
            - Chỉ dùng thông tin từ tư liệu gốc, không thêm chi tiết không có trong bài
            - Nếu tư liệu không đủ để trả lời câu hỏi, hãy nói thẳng điều đó
            - Tối đa 200 từ
            - Không lặp lại tiêu đề bài viết trong câu trả lời
            """.formatted(systemPrompt, post.getTitle(), content, userQuestion);

        try {
            Map<String, Object> requestBody = Map.of(
                "model", groqModel,
                "messages", List.of(Map.of("role", "user", "content", prompt)),
                "max_tokens", 400,
                "temperature", 0.5
            );

            Map<?, ?> response = webClientBuilder.build()
                .post().uri(groqEndpoint)
                .header("Authorization", "Bearer " + groqApiKey)
                .header("Content-Type", "application/json")
                .bodyValue(requestBody)
                .retrieve().bodyToMono(Map.class).block();

            String result = extractGroqText(response);
            log.debug("Groq explanation generated ({} chars)", result != null ? result.length() : 0);
            return (result != null && !result.isBlank()) ? result.trim() : null;

        } catch (Exception e) {
            String msg = e.getMessage();
            if (msg != null && msg.contains("429")) {
                log.warn("Groq rate limit on generation, falling back to excerpt");
            } else {
                log.warn("Groq generation error: {}, falling back to excerpt", msg);
            }
            return null;
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

    // ── TOPIC SYNONYMS ───────────────────────────────────────────
    private static final Map<String, List<String>> TOPIC_SYNONYMS = Map.of(
        "lính",    List.of("lính", "chiến sĩ", "bộ đội", "quân nhân", "chiến binh"),
        "mộ",      List.of("mộ", "hài cốt", "liệt sĩ", "tìm mộ", "quy tập"),
        "chiến tranh", List.of("chiến tranh", "kháng chiến", "giải phóng", "bom", "đạn"),
        "gia đình", List.of("gia đình", "mẹ", "cha", "vợ", "con", "anh", "chị"),
        "tâm linh", List.of("linh hồn", "ma", "điềm", "giấc mơ", "báo mộng", "hiển linh"),
        "làng",    List.of("làng", "xã", "thôn", "bản", "quê hương", "dân làng")
    );

    /** Mở rộng query bằng synonym trước khi search keyword */
    private List<String> expandWithSynonyms(List<String> keywords) {
        List<String> expanded = new java.util.ArrayList<>(keywords);
        for (String kw : keywords) {
            TOPIC_SYNONYMS.forEach((key, synonyms) -> {
                if (synonyms.contains(kw) || key.equals(kw)) {
                    synonyms.forEach(s -> { if (!expanded.contains(s)) expanded.add(s); });
                }
            });
        }
        return expanded;
    }

    // ── KEYWORD FALLBACK ────────────────────────────────────────

    private Optional<Post> findByKeyword(String userMessage, List<Post> posts) {
        String query = userMessage.toLowerCase().trim();

        // Ưu tiên 1: câu hỏi chứa title của bài (gợi ý suggest thường embed title vào câu)
        Optional<Post> byTitleInQuery = posts.stream()
            .filter(p -> query.contains(p.getTitle().toLowerCase()))
            .findFirst();
        if (byTitleInQuery.isPresent()) {
            log.debug("Keyword tier-1 match (title in query): {}", byTitleInQuery.get().getTitle());
            return byTitleInQuery;
        }

        // Ưu tiên 2: title chứa từ khóa từ câu hỏi
        String[] words = query.split("[\\s\"'?,!.()“”‘’]+");
        List<String> keywords = java.util.Arrays.stream(words)
            .filter(w -> w.length() >= 3)
            .filter(w -> !java.util.Set.of(
                "truyền","thuyết","hành","trình","nguồn","gốc","điều","không",
                "được","chuyện","trong","ngoài","chúng","những","người","này",
                "kể","về","nào","có","của","và","là","cho","với","bài","câu"
            ).contains(w))
            .distinct()
            .toList();

        // Mở rộng keyword bằng synonym
        List<String> expandedKeywords = expandWithSynonyms(keywords);
        log.debug("Keyword search — original: {}, expanded: {}", keywords, expandedKeywords);

        if (!expandedKeywords.isEmpty()) {
            // Tier 2: title chứa keyword gốc (không expand để tránh false positive)
            Optional<Post> byKeywords = posts.stream()
                .filter(p -> {
                    String title = p.getTitle().toLowerCase();
                    long matched = keywords.stream().filter(title::contains).count();
                    // Cần ít nhất 2 keyword khớp, hoặc 50% nếu có ít keyword
                    return matched >= Math.max(2, keywords.size() / 2);
                })
                .findFirst();
            if (byKeywords.isPresent()) {
                log.debug("Keyword tier-2 match (keywords in title): {}", byKeywords.get().getTitle());
                return byKeywords;
            }

            // Tier 3: content chứa keyword mở rộng — chọn bài có score cao nhất
            Optional<Post> byContent = posts.stream()
                .filter(p -> {
                    String content = p.getContent().toLowerCase();
                    return expandedKeywords.stream().anyMatch(content::contains);
                })
                .max(java.util.Comparator.comparingLong(p -> {
                    String content = p.getContent().toLowerCase();
                    return expandedKeywords.stream().filter(content::contains).count();
                }));
            if (byContent.isPresent()) {
                log.debug("Keyword tier-3 match (expanded keywords in content): {}", byContent.get().getTitle());
                return byContent;
            }
        }

        log.debug("Keyword search: no match found for query '{}'", userMessage);
        return Optional.empty();
    }

    // ── HELPERS ─────────────────────────────────────────────────

    private ChatResponse buildNotFoundResponse() {
        return ChatResponse.builder()
            .reply(NOT_FOUND_REPLY)
            .found(false)
            .build();
    }

    /** Parse response từ Groq (OpenAI-compatible format) */
    private String extractGroqText(Map<?, ?> response) {
        if (response == null) return "";
        try {
            List<?> choices = (List<?>) response.get("choices");
            if (choices == null || choices.isEmpty()) return "";
            Map<?, ?> message = (Map<?, ?>) ((Map<?, ?>) choices.get(0)).get("message");
            return (String) message.get("content");
        } catch (Exception e) {
            return "";
        }
    }

    /** Parse response từ Gemini (giữ lại phòng sau cần) */
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

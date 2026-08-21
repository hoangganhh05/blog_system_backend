package com.example.blogsystem.service.impl;

import com.example.blogsystem.service.AiService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Stream;

@Service
@Slf4j
public class AiServiceImpl implements AiService {

    @Value("${GEMINI_API_KEY:${gemini.api-key:}}")
    private String rawApiKey;

    @Value("${GEMINI_MODEL:${gemini.model:gemini-2.5-flash}}")
    private String rawModel;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final HttpClient httpClient;

    public AiServiceImpl() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(10_000); // 10s connect
        factory.setReadTimeout(30_000);    // 30s read
        this.restTemplate = new RestTemplate(factory);

        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    private String getCleanApiKey() {
        return rawApiKey != null ? rawApiKey.trim().replace("\"", "").replace("'", "") : "";
    }

    private String getCleanModelName() {
        String modelName = "gemini-2.5-flash";
        if (rawModel != null && !rawModel.trim().isEmpty()) {
            modelName = rawModel.replace("models/", "").replace("model/", "").trim();
        }
        return modelName;
    }

    private String maskUrl(String url) {
        if (url == null || url.isEmpty()) return "";
        int keyIndex = url.indexOf("key=");
        if (keyIndex != -1) {
            return url.substring(0, keyIndex) + "key=***";
        }
        return url;
    }

    private String getSystemPrompt() {
        return """
                Bạn là Trợ lý AI của mạng xã hội BlogViet (https://anhhoangg.id.vn/).
                Sứ mệnh: Hướng dẫn người dùng trải nghiệm nền tảng, hỗ trợ sáng tạo nội dung, giải đáp thắc mắc, phân tích hình ảnh đính kèm (ảnh chụp màn hình, ảnh bài viết, meme...).

                QUY TẮC CỐT LÕI:
                1. TUYỆT ĐỐI KHÔNG DÙNG TỪ NGỮ KỸ THUẬT: Cấm nhắc đến React, Vite, Spring Boot, Java, MySQL, database, API, backend, frontend, server, code, token... Mọi câu trả lời đứng từ góc nhìn Giao diện người dùng (UI) và tính năng thực tế.
                2. VĂN PHONG TỰ NHIÊN, SÚC TÍCH: Đi thẳng vào trọng tâm câu hỏi, thân thiện, chuyên nghiệp, hỗ trợ định dạng Markdown rõ ràng.

                TÍNH NĂNG BLOGVIET NỔI BẬT:
                • Nút (+ Đăng bài) ở góc trên bên phải để soạn bài viết mới kèm ảnh/video/hashtag.
                • Menu điều hướng: Bảng tin (Dành cho bạn, Đang theo dõi), Shorts (Video ngắn), Trạm Âm Thanh (/soundscapes - nghe âm thanh thực địa mưa, cafe, sóng biển, rừng thông), Bạn bè, Bài viết đã lưu, Bảng điều khiển.
                • Bong bóng Chat ở góc dưới bên phải để nhắn tin, gọi thoại/video HD với bạn bè hoặc AI.
                • Dưới mỗi bài viết có nút Thả tim cảm xúc, Bình luận ảnh GIF, Chia sẻ và Tóm tắt AI (✨).
                """;
    }

    private Map<String, Object> buildRequestBody(String prompt, String imageBase64, String imageMimeType) {
        String actualPrompt = prompt != null && !prompt.trim().isEmpty() ? prompt.trim() : "Hãy phân tích hình ảnh này giúp tôi.";
        String combinedPrompt = getSystemPrompt().trim() + "\n\n---\nCâu hỏi của người dùng:\n" + actualPrompt;

        List<Map<String, Object>> partsList = new ArrayList<>();
        partsList.add(Collections.singletonMap("text", combinedPrompt));

        if (imageBase64 != null && !imageBase64.trim().isEmpty()) {
            String rawB64 = imageBase64.trim();
            String mime = imageMimeType != null && !imageMimeType.trim().isEmpty() ? imageMimeType.trim() : "image/jpeg";
            if (rawB64.contains(";base64,")) {
                String[] split = rawB64.split(";base64,");
                if (split[0].contains("data:")) {
                    mime = split[0].replace("data:", "").trim();
                }
                rawB64 = split[1].trim();
            }

            Map<String, Object> inlineData = new HashMap<>();
            inlineData.put("mime_type", mime);
            inlineData.put("data", rawB64);

            Map<String, Object> imagePart = new HashMap<>();
            imagePart.put("inline_data", inlineData);
            partsList.add(imagePart);
        }

        Map<String, Object> body = new HashMap<>();
        Map<String, Object> contentMap = new HashMap<>();
        contentMap.put("parts", partsList);
        body.put("contents", Collections.singletonList(contentMap));

        Map<String, Object> generationConfig = new HashMap<>();
        generationConfig.put("temperature", 0.7);
        generationConfig.put("topK", 40);
        generationConfig.put("topP", 0.95);
        generationConfig.put("maxOutputTokens", 1536);
        body.put("generationConfig", generationConfig);

        return body;
    }

    @Override
    public String generateReply(String prompt) {
        return generateReply(prompt, null, null);
    }

    @Override
    public String generateReply(String prompt, String imageBase64, String imageMimeType) {
        if ((prompt == null || prompt.trim().isEmpty()) && (imageBase64 == null || imageBase64.trim().isEmpty())) {
            return "Xin chào! Mình có thể giúp gì cho bạn trên BlogViet hôm nay?";
        }

        String apiKey = getCleanApiKey();
        String modelName = getCleanModelName();

        if (apiKey.isEmpty()) {
            log.warn("[GEMINI WARN] GEMINI_API_KEY chưa được cấu hình trong biến môi trường server!");
            return "Trợ lý AI chưa được kích hoạt API Key trên server. Vui lòng liên hệ ban quản trị!";
        }

        Map<String, Object> body = buildRequestBody(prompt, imageBase64, imageMimeType);

        List<String> targetUrls = Arrays.asList(
                "https://generativelanguage.googleapis.com/v1beta/models/" + modelName + ":generateContent?key=" + apiKey,
                "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=" + apiKey,
                "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent?key=" + apiKey,
                "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=" + apiKey
        );

        String lastErrorMsg = "";

        for (String url : targetUrls) {
            String maskedUrl = maskUrl(url);
            log.info("[GEMINI DEBUG] Sending request to URL: {}", maskedUrl);

            try {
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);

                HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
                ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);

                if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                    JsonNode root = objectMapper.readTree(response.getBody());
                    JsonNode candidates = root.path("candidates");
                    if (candidates.isArray() && !candidates.isEmpty()) {
                        JsonNode first = candidates.get(0);
                        JsonNode parts = first.path("content").path("parts");
                        if (parts.isArray() && !parts.isEmpty()) {
                            String text = parts.get(0).path("text").asText();
                            if (text != null && !text.trim().isEmpty()) {
                                return text.trim();
                            }
                        }
                    }
                }
            } catch (HttpStatusCodeException ex) {
                lastErrorMsg = "HTTP " + ex.getStatusCode() + ": " + ex.getResponseBodyAsString();
                log.warn("[GEMINI API WARN] Thử URL thất bại ({}): {}", maskedUrl, lastErrorMsg);
            } catch (Exception ex) {
                lastErrorMsg = ex.getMessage();
                log.warn("[GEMINI API WARN] Lỗi kết nối tới URL ({}): {}", maskedUrl, lastErrorMsg);
            }
        }

        log.error("[GEMINI ERROR] Toàn bộ các endpoint Google Gemini đều thất bại! Lỗi cuối: {}", lastErrorMsg);
        return "Xin lỗi bạn, hiện tại hệ thống AI đang quá tải hoặc gặp gián đoạn kết nối. Bạn vui lòng bấm nút Thử lại nhé!";
    }

    @Override
    public void streamReply(String prompt, String imageBase64, String imageMimeType, Consumer<String> onChunk) {
        if ((prompt == null || prompt.trim().isEmpty()) && (imageBase64 == null || imageBase64.trim().isEmpty())) {
            onChunk.accept("Xin chào! Mình có thể giúp gì cho bạn trên BlogViet hôm nay?");
            return;
        }

        String apiKey = getCleanApiKey();
        String modelName = getCleanModelName();

        if (apiKey.isEmpty()) {
            onChunk.accept("Trợ lý AI chưa được kích hoạt API Key trên server. Vui lòng liên hệ ban quản trị!");
            return;
        }

        Map<String, Object> body = buildRequestBody(prompt, imageBase64, imageMimeType);
        String jsonPayload;
        try {
            jsonPayload = objectMapper.writeValueAsString(body);
        } catch (Exception e) {
            log.error("Lỗi serialize JSON payload: ", e);
            onChunk.accept("Không thể tạo yêu cầu gửi tới AI. Vui lòng thử lại!");
            return;
        }

        List<String> streamUrls = Arrays.asList(
                "https://generativelanguage.googleapis.com/v1beta/models/" + modelName + ":streamGenerateContent?alt=sse&key=" + apiKey,
                "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:streamGenerateContent?alt=sse&key=" + apiKey,
                "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:streamGenerateContent?alt=sse&key=" + apiKey,
                "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:streamGenerateContent?alt=sse&key=" + apiKey
        );

        boolean streamedAny = false;

        for (String url : streamUrls) {
            String maskedUrl = maskUrl(url);
            log.info("[GEMINI STREAM] Bắt đầu stream tới URL: {}", maskedUrl);

            try {
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(url))
                        .header("Content-Type", "application/json")
                        .timeout(Duration.ofSeconds(20))
                        .POST(HttpRequest.BodyPublishers.ofString(jsonPayload, StandardCharsets.UTF_8))
                        .build();

                HttpResponse<Stream<String>> response = httpClient.send(request, HttpResponse.BodyHandlers.ofLines());

                if (response.statusCode() == 200) {
                    try (Stream<String> lines = response.body()) {
                        for (Iterator<String> it = lines.iterator(); it.hasNext(); ) {
                            String line = it.next();
                            if (line != null && line.startsWith("data: ") && line.length() > 6) {
                                String json = line.substring(6).trim();
                                if (!json.isEmpty() && !json.equals("[DONE]")) {
                                    try {
                                        JsonNode root = objectMapper.readTree(json);
                                        JsonNode candidates = root.path("candidates");
                                        if (candidates.isArray() && !candidates.isEmpty()) {
                                            JsonNode first = candidates.get(0);
                                            JsonNode parts = first.path("content").path("parts");
                                            if (parts.isArray() && !parts.isEmpty()) {
                                                String text = parts.get(0).path("text").asText();
                                                if (text != null && !text.isEmpty()) {
                                                    onChunk.accept(text);
                                                    streamedAny = true;
                                                }
                                            }
                                        }
                                    } catch (Exception parseEx) {
                                        // Ignore line parsing glitch
                                    }
                                }
                            }
                        }
                    }

                    if (streamedAny) {
                        return; // Hoàn thành streaming thành công!
                    }
                }
            } catch (Exception ex) {
                log.warn("[GEMINI STREAM WARN] Lỗi stream qua URL ({}): {}", maskedUrl, ex.getMessage());
            }
        }

        // Nếu stream thất bại hoàn toàn, fallback sang gọi đồng bộ generateReply
        log.warn("[GEMINI STREAM] Chuyển hướng fallback sang generateReply đồng bộ...");
        String fallbackReply = generateReply(prompt, imageBase64, imageMimeType);
        onChunk.accept(fallbackReply);
    }
}

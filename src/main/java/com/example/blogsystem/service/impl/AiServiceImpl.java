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
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
@Slf4j
public class AiServiceImpl implements AiService {

    @Value("${GEMINI_API_KEY:${gemini.api-key:}}")
    private String rawApiKey;

    @Value("${GEMINI_MODEL:${gemini.model:gemini-1.5-flash}}")
    private String rawModel;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String generateReply(String prompt) {
        if (prompt == null || prompt.trim().isEmpty()) {
            return "Xin chào! Bạn muốn mình hỗ trợ gì hôm nay?";
        }

        String apiKey = rawApiKey != null ? rawApiKey.trim().replace("\"", "").replace("'", "") : "";
        String modelName = "gemini-1.5-flash";
        if (rawModel != null && !rawModel.trim().isEmpty()) {
            modelName = rawModel.replace("models/", "").replace("model/", "").trim();
        }

        boolean hasKey = !apiKey.isEmpty();
        log.info("[GEMINI DEBUG] API Key loaded: {} (length: {}), modelName: {}", hasKey, apiKey.length(), modelName);

        if (!hasKey) {
            log.warn("[GEMINI WARN] GEMINI_API_KEY chưa được cấu hình trong biến môi trường server!");
            return "Trợ lý AI chưa được kích hoạt API Key trên server. Vui lòng thêm biến môi trường GEMINI_API_KEY trong cấu hình Render!";
        }

        String systemPrompt = """
                Bạn là Trợ lý AI của hệ thống BlogViet (https://anhhoangg.id.vn/). 
                Sứ mệnh: Hỗ trợ người dùng sáng tạo nội dung, gợi ý ý tưởng và hướng dẫn sử dụng nền tảng hiệu quả.

                NGUYÊN TẮC GIAO TIẾP & VĂN PHONG:
                1. Đi thẳng vào vấn đề: Trả lời trực tiếp vào câu hỏi hoặc yêu cầu của người dùng ngay từ câu đầu tiên.
                2. TUYỆT ĐỐI KHÔNG DÙNG VĂN MẪU RẬP KHUÔN: Không lặp lại các câu mở đầu máy móc ("Chào bạn...", "Tôi rất vui được giúp..."). Chỉ chào khi người dùng chào trước.
                3. Phong cách Minimalist: Ngắn gọn, gãy gọn, thông minh, đúng trọng tâm, trình bày rõ ràng (dùng Markdown khi cần).
                4. Biến hóa linh hoạt: Đa dạng hóa câu từ và cấu trúc câu tùy theo ngữ cảnh.

                KIẾN THỨC NỀN TẢNG BLOGVIET:
                - Hệ thống: Nền tảng Blog hiện đại (React 19/Vite 8 & Spring Boot/MySQL).
                - Đăng bài: Bấm vào nút (+) Đăng bài ở header hoặc ô soạn thảo tại trang chủ.
                - Chat: Bấm vào biểu tượng bong bóng chat 💬 ở góc dưới bên phải.
                - Chia sẻ: Sử dụng nút Chia sẻ dưới bài viết để Quote post, gửi DM bạn bè hoặc sao chép link.
                - Kỹ thuật / Báo lỗi: Hướng dẫn kiểm tra kết nối mạng hoặc liên hệ quản trị viên (Hoàng Anh).
                - Tâm sự / Chia sẻ: Lắng nghe chân thành, đưa ra góc nhìn tích cực, ấm áp và thực tế.
                """;

        String combinedPrompt = systemPrompt.trim() + "\n\n---\nNội dung câu hỏi của người dùng:\n" + prompt.trim();

        // Danh sách URL ưu tiên: v1 trước theo chuẩn của Google
        List<String> targetUrls = Arrays.asList(
                "https://generativelanguage.googleapis.com/v1/models/" + modelName + ":generateContent?key=" + apiKey,
                "https://generativelanguage.googleapis.com/v1beta/models/" + modelName + ":generateContent?key=" + apiKey,
                "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent?key=" + apiKey,
                "https://generativelanguage.googleapis.com/v1/models/gemini-1.5-flash:generateContent?key=" + apiKey
        );

        String lastErrorMsg = "";

        for (String url : targetUrls) {
            String maskedUrl = url.substring(0, url.indexOf("?key=")) + "?key=***";
            System.out.println("[GEMINI DEBUG] Sending request to URL: " + maskedUrl);
            log.info("[GEMINI DEBUG] Sending request to URL: {}", maskedUrl);

            try {
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);

                // Body chuẩn Google REST API v1 (đơn giản, tương thích 100%)
                Map<String, Object> body = new HashMap<>();
                Map<String, Object> contentMap = new HashMap<>();
                contentMap.put("parts", Collections.singletonList(Collections.singletonMap("text", combinedPrompt)));
                body.put("contents", Collections.singletonList(contentMap));

                HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

                ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);

                if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                    JsonNode root = objectMapper.readTree(response.getBody());
                    JsonNode candidates = root.path("candidates");
                    if (candidates.isArray() && !candidates.isEmpty()) {
                        JsonNode textNode = candidates.get(0).path("content").path("parts").get(0).path("text");
                        if (!textNode.isMissingNode()) {
                            String reply = textNode.asText();
                            log.info("[GEMINI SUCCESS] URL [{}] responded successfully (length: {})", maskedUrl, reply.length());
                            return reply;
                        }
                    }
                }
            } catch (HttpStatusCodeException httpEx) {
                lastErrorMsg = httpEx.getStatusCode() + ": " + httpEx.getResponseBodyAsString();
                log.warn("[GEMINI RETRY] URL [{}] returned HTTP {}: {}", maskedUrl, httpEx.getStatusCode(), httpEx.getResponseBodyAsString());
            } catch (Exception e) {
                lastErrorMsg = e.getMessage();
                log.warn("[GEMINI RETRY] URL [{}] failed: {}", maskedUrl, e.getMessage());
            }
        }

        log.error("[GEMINI FINAL ERROR] All Google Gemini endpoints failed. Last error: {}", lastErrorMsg);
        return "Hiện tại hệ thống Trợ lý AI đang bận hoặc gián đoạn kết nối. Phản hồi từ Google: " + lastErrorMsg;
    }
}

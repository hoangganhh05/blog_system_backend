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
        String model = rawModel != null && !rawModel.trim().isEmpty() ? rawModel.trim() : "gemini-1.5-flash";
        String cleanModel = model.replace("models/", "").replace("model/", "").trim();

        boolean hasKey = !apiKey.isEmpty();
        log.info("[GEMINI DEBUG] API Key loaded: {} (length: {}), requested model: {}", hasKey, apiKey.length(), cleanModel);

        if (!hasKey) {
            log.warn("[GEMINI WARN] GEMINI_API_KEY chưa được cấu hình trong biến môi trường server!");
            return "Trợ lý AI chưa được kích hoạt API Key trên server. Vui lòng thêm biến môi trường GEMINI_API_KEY trong cấu hình Render!";
        }

        // Danh sách model ưu tiên thử nghiệm (gemini-1.5-flash, gemini-2.0-flash, gemini-1.5-flash-latest, v1 API)
        List<String> endpointTemplates = Arrays.asList(
                "https://generativelanguage.googleapis.com/v1beta/models/" + cleanModel + ":generateContent",
                "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent",
                "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent",
                "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash-latest:generateContent",
                "https://generativelanguage.googleapis.com/v1/models/" + cleanModel + ":generateContent",
                "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-pro:generateContent"
        );

        String systemPrompt = """
                Bạn là Trợ lý AI của hệ thống BlogViet (https://anhhoangg.id.vn/). 
                Sứ mệnh: Hỗ trợ người dùng sáng tạo nội dung, gợi ý ý tưởng và hướng dẫn sử dụng nền tảng hiệu quả.

                NGUYÊN TẮC GIAO TIẾP & VĂN PHONG (BẮT BUỘC TUÂN THỦ):
                1. Đi thẳng vào vấn đề: Trả lời trực tiếp vào câu hỏi hoặc yêu cầu của người dùng ngay từ câu đầu tiên.
                2. TUYỆT ĐỐI KHÔNG DÙNG VĂN MẪU RẬP KHUÔN:
                   - Không lặp lại các câu mở đầu máy móc như: "Chào bạn! Tôi là Trợ lý BlogViet...", "Tôi rất vui được giúp...", "Cảm ơn bạn đã hỏi...".
                   - Chỉ chào hỏi nhẹ nhàng nếu người dùng chủ động chào trước. Các lượt trao đổi tiếp theo hãy trả lời thẳng nội dung.
                3. Phong cách Minimalist: Ngắn gọn, gãy gọn, thông minh, đúng trọng tâm, trình bày rõ ràng (dùng gạch đầu dòng Markdown khi cần).
                4. Biến hóa linh hoạt: Đa dạng hóa câu từ và cấu trúc câu tùy theo ngữ cảnh (sáng tạo nội dung, hỗ trợ kỹ thuật, giải đáp thắc mắc, hay trò chuyện tâm sự).

                KIẾN THỨC NỀN TẢNG BLOGVIET:
                - Hệ thống: Nền tảng Blog hiện đại (React 19/Vite 8 & Spring Boot/MySQL).
                - Đăng bài: Bấm vào nút (+) Đăng bài ở header hoặc ô soạn thảo tại trang chủ.
                - Chat: Bấm vào biểu tượng bong bóng chat 💬 ở góc dưới bên phải.
                - Chia sẻ: Sử dụng nút Chia sẻ dưới bài viết để Quote post, gửi DM bạn bè hoặc sao chép link.
                - Kỹ thuật / Báo lỗi: Hướng dẫn kiểm tra kết nối mạng hoặc liên hệ quản trị viên (Hoàng Anh).
                - Tâm sự / Chia sẻ: Lắng nghe chân thành, đưa ra góc nhìn tích cực, ấm áp và thực tế.
                """;

        String lastErrorMsg = "";

        // Thử lần lượt các endpoint & model khả dụng
        Set<String> triedUrls = new HashSet<>();
        for (String baseEndpoint : endpointTemplates) {
            if (triedUrls.contains(baseEndpoint)) continue;
            triedUrls.add(baseEndpoint);

            String fullUrl = baseEndpoint + "?key=" + apiKey;
            System.out.println("[GEMINI DEBUG] Invoking Endpoint URL: " + baseEndpoint);
            log.info("[GEMINI DEBUG] Invoking Endpoint URL: {}", baseEndpoint);

            try {
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                headers.set("x-goog-api-key", apiKey);

                // Payload chuẩn Google Generative Language
                Map<String, Object> body = new HashMap<>();

                // Contents
                Map<String, Object> userContent = new HashMap<>();
                userContent.put("role", "user");
                userContent.put("parts", Collections.singletonList(Collections.singletonMap("text", prompt.trim())));
                body.put("contents", Collections.singletonList(userContent));

                // System Instruction
                Map<String, Object> systemInstruction = new HashMap<>();
                systemInstruction.put("parts", Collections.singletonList(Collections.singletonMap(
                        "text",
                        systemPrompt.trim()
                )));
                body.put("systemInstruction", systemInstruction);

                HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

                ResponseEntity<String> response = restTemplate.postForEntity(fullUrl, entity, String.class);

                if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                    JsonNode root = objectMapper.readTree(response.getBody());
                    JsonNode candidates = root.path("candidates");
                    if (candidates.isArray() && !candidates.isEmpty()) {
                        JsonNode textNode = candidates.get(0).path("content").path("parts").get(0).path("text");
                        if (!textNode.isMissingNode()) {
                            String reply = textNode.asText();
                            log.info("[GEMINI SUCCESS] Endpoint [{}] successfully returned reply (length: {})", baseEndpoint, reply.length());
                            return reply;
                        }
                    }
                }
            } catch (HttpStatusCodeException httpEx) {
                lastErrorMsg = httpEx.getStatusCode() + ": " + httpEx.getResponseBodyAsString();
                log.warn("[GEMINI RETRY] Endpoint [{}] failed with HTTP {}. Attempting fallback...", baseEndpoint, httpEx.getStatusCode());
                log.error("[GEMINI ERROR BODY] {}", httpEx.getResponseBodyAsString());
            } catch (Exception e) {
                lastErrorMsg = e.getMessage();
                log.warn("[GEMINI RETRY] Endpoint [{}] encountered exception: {}. Attempting fallback...", baseEndpoint, e.getMessage());
            }
        }

        log.error("[GEMINI FINAL ERROR] All Google Gemini endpoints failed. Last error: {}", lastErrorMsg);
        return "Hiện tại hệ thống Trợ lý AI đang bận hoặc gián đoạn kết nối. Chi tiết phản hồi từ Google: " + lastErrorMsg;
    }
}

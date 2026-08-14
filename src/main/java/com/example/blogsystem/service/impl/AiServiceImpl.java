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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

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

        boolean hasKey = !apiKey.isEmpty();
        log.info("[GEMINI DEBUG] API Key loaded: {} (length: {}), model: {}", hasKey, apiKey.length(), model);

        if (!hasKey) {
            log.warn("[GEMINI WARN] GEMINI_API_KEY chưa được cấu hình trong biến môi trường server!");
            return "Trợ lý AI chưa được kích hoạt API Key trên server. Vui lòng thêm biến môi trường GEMINI_API_KEY trong cấu hình Render!";
        }

        try {
            String url = "https://generativelanguage.googleapis.com/v1beta/models/" + model + ":generateContent?key=" + apiKey;

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("x-goog-api-key", apiKey);

            // Construct payload
            Map<String, Object> body = new HashMap<>();

            // User content
            Map<String, Object> userContent = new HashMap<>();
            userContent.put("role", "user");
            userContent.put("parts", Collections.singletonList(Collections.singletonMap("text", prompt.trim())));
            body.put("contents", Collections.singletonList(userContent));

            // System Instruction - Chuyên gia BlogViet Platform
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

            Map<String, Object> systemInstruction = new HashMap<>();
            systemInstruction.put("parts", Collections.singletonList(Collections.singletonMap(
                    "text",
                    systemPrompt.trim()
            )));
            body.put("systemInstruction", systemInstruction);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

            log.info("[GEMINI DEBUG] Sending request to Google Gemini API: {} with prompt excerpt: {}", url.substring(0, url.indexOf("?key=")), prompt.length() > 40 ? prompt.substring(0, 40) + "..." : prompt);

            ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                JsonNode root = objectMapper.readTree(response.getBody());
                JsonNode candidates = root.path("candidates");
                if (candidates.isArray() && !candidates.isEmpty()) {
                    JsonNode textNode = candidates.get(0).path("content").path("parts").get(0).path("text");
                    if (!textNode.isMissingNode()) {
                        String reply = textNode.asText();
                        log.info("[GEMINI SUCCESS] Received reply (length: {})", reply.length());
                        return reply;
                    }
                }
            }

            log.warn("[GEMINI WARN] Google returned 200 but no valid candidates: {}", response.getBody());
            return "Xin lỗi bạn, mình chưa hiểu rõ yêu cầu. Bạn có thể diễn đạt lại câu hỏi giúp mình không?";
        } catch (HttpStatusCodeException httpEx) {
            log.error("[GEMINI HTTP ERROR] Status: {} - Response Body: {}", httpEx.getStatusCode(), httpEx.getResponseBodyAsString());
            return "Google Gemini API phản hồi lỗi (" + httpEx.getStatusCode() + "). Chi tiết: " + httpEx.getResponseBodyAsString();
        } catch (Exception e) {
            log.error("[GEMINI UNEXPECTED ERROR] Lỗi không xác định khi gọi Google Gemini API: ", e);
            e.printStackTrace();
            return "Hiện tại hệ thống Trợ lý AI đang bận hoặc gián đoạn kết nối (" + e.getMessage() + "). Bạn vui lòng thử lại sau giây lát nhé!";
        }
    }
}

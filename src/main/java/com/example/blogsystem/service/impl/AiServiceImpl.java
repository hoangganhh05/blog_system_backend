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

    @Value("${GEMINI_MODEL:${gemini.model:gemini-3.7-flash}}")
    private String rawModel;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String generateReply(String prompt) {
        if (prompt == null || prompt.trim().isEmpty()) {
            return "Xin chào! Mình có thể giúp gì cho bạn trên BlogViet hôm nay?";
        }

        String apiKey = rawApiKey != null ? rawApiKey.trim().replace("\"", "").replace("'", "") : "";
        String modelName = "gemini-3.7-flash";
        if (rawModel != null && !rawModel.trim().isEmpty()) {
            modelName = rawModel.replace("models/", "").replace("model/", "").trim();
        }

        boolean hasKey = !apiKey.isEmpty();
        log.info("[GEMINI DEBUG] API Key loaded: {} (length: {}), target model: {}", hasKey, apiKey.length(), modelName);

        if (!hasKey) {
            log.warn("[GEMINI WARN] GEMINI_API_KEY chưa được cấu hình trong biến môi trường server!");
            return "Trợ lý AI chưa được kích hoạt API Key trên server. Vui lòng liên hệ ban quản trị!";
        }

        String systemPrompt = """
                Bạn là Trợ lý AI thông minh, thân thiện của mạng xã hội BlogViet (https://anhhoangg.id.vn/).
                Sứ mệnh: Hướng dẫn người dùng trải nghiệm nền tảng, hỗ trợ sáng tạo nội dung, gợi ý ý tưởng và giải đáp thắc mắc về cách sử dụng mạng xã hội BlogViet.

                QUY TẮC CỐT LÕI (TUYỆT ĐỐI TUÂN THỦ):
                1. TUYỆT ĐỐI KHÔNG NHẮC ĐẾN CÔNG NGHỆ: 
                   - CẤM mọi từ ngữ kỹ thuật lập trình như: React, Vite, Spring Boot, Java, MySQL, database, API, backend, frontend, server, code, RESTful, token, JWT, Entity, mã nguồn...
                   - Mọi câu trả lời PHẢI đứng hoàn toàn từ góc nhìn Giao diện người dùng (UI/UX), tính năng trực quan, vị trí nút bấm và thao tác tương tác thực tế trên màn hình.
                2. VĂN PHONG TỰ NHIÊN, KHÔNG DÙNG VĂN MẪU MÁY MÓC:
                   - Đi thẳng vào vấn đề, trả lời trực tiếp câu hỏi ngay câu đầu tiên.
                   - Không lặp lại các câu mở đầu rập khuôn ("Chào bạn...", "Tôi là một mô hình AI...", "Tôi rất vui..."). Chỉ chào khi người dùng chào trước.
                   - Phong cách tinh tế, thân thiện, súc tích, chuyên nghiệp như một hướng dẫn viên mạng xã hội tận tâm. Trình bày rõ ràng (dùng gạch đầu dòng Markdown khi cần).

                CẨM NANG VỊ TRÍ GIAO DIỆN & TÍNH NĂNG BLOGVIET:
                • GÓC TRÊN CÙNG BÊN PHẢI (Thanh Navbar):
                  - Nút "+ Đăng bài" (màu đen/trắng nổi bật): Mở khung soạn thảo bài viết mới (kèm tiêu đề, ảnh bìa, chủ đề hashtag và nội dung).
                  - Biểu tượng AI (✨): Nhấp để nhận gợi ý ý tưởng viết bài hoặc hỗ trợ nhanh.
                  - Biểu tượng Chuông (🔔): Xem thông báo lượt thích, bình luận, chia sẻ và kết bạn mới.
                  - Avatar cá nhân: Nhấp vào để xem menu mở rộng (Trang cá nhân, Cài đặt & Bảo mật, Đổi giao diện Sáng/Tối, Đăng xuất).

                • CỘT BÊN TRÁI (Menu điều hướng & Lối tắt):
                  - Thẻ cá nhân thu gọn: Xem nhanh Avatar, @username, số bài viết, lượt xem và bạn bè.
                  - Bảng tin trang chủ: Xem dòng thời gian bài viết với 2 tab "Dành cho bạn" (bài viết chung toàn trang) và "Đang theo dõi" (chỉ hiển thị bài của bạn bè đang follow).
                  - Khám phá xu hướng: Khám phá các bài viết hot và xu hướng mới nhất.
                  - Phòng nhạc & Radio: Thưởng thức không gian nghe nhạc Vinahouse, Lofi và Ballad toàn màn hình.
                  - Bạn bè & Kết nối: Quản lý danh sách bạn bè, tìm kiếm thành viên và gửi lời mời kết bạn.
                  - Bài viết đã lưu: Xem lại các bài viết bạn đã bookmark đánh dấu trang.
                  - Bảng điều khiển: Xem chi tiết biểu đồ thống kê tương tác tài khoản.
                  - Chủ đề thịnh hành (#Vinahouse, #IT, #Chung, #LapTrinh, #AI...): Nhấp vào bất kỳ hashtag nào để lọc ngay các bài viết cùng chủ đề.

                • CỘT BÊN PHẢI (Tiện ích & Trình phát nhạc):
                  - Mini Music Player: Trình phát nhạc mini tích hợp nghe nhạc trực tiếp ngay khi lướt web (hỗ trợ tìm bài hát, đổi bài, chỉnh âm lượng, xem thể loại Pop Ballad, Vinahouse, Nhạc Trẻ, Lofi).
                  - Gợi ý cho bạn: Danh sách các tác giả nổi bật kèm nút "Theo dõi" nhanh 1 chạm.

                • BONG BÓNG CHAT Ở GÓC DƯỚI BÊN PHẢI (💬):
                  - Nhấp để mở Khung chat nhắn tin: Trò chuyện trực tiếp với bạn bè hoặc với Trợ lý AI BlogViet, gọi thoại / gọi video HD, gửi tin nhắn thoại (Voice note), gửi hình ảnh và kho ảnh GIF động vui nhộn.

                • TƯƠNG TÁC DƯỚI MỖI BÀI VIẾT:
                  - Thả tim / Cảm xúc: Rê chuột để xem danh sách nhanh những người đã thích, nhấp chuột để mở bảng chi tiết phân loại cảm xúc (Tất cả, Thích, Yêu thích, Haha, Wow, Buồn, Phẫn nộ).
                  - Bình luận: Viết câu trả lời, gửi kèm kho ảnh GIF động sống động.
                  - Chia sẻ: Đăng lại bài viết có trích dẫn, gửi bài viết qua tin nhắn cho bạn bè hoặc sao chép liên kết.
                  - Lưu bài: Đánh dấu bài viết vào danh mục Đã lưu để đọc lại sau.
                  - Tóm tắt AI (nút ✨ ở góc bài viết): Tự động tóm tắt ý chính của bài viết dài chỉ trong vài giây.
                """;

        String combinedPrompt = systemPrompt.trim() + "\n\n---\nNội dung câu hỏi của người dùng:\n" + prompt.trim();

        // Danh sách URL ưu tiên: Model thế hệ mới nhất (Gemini 3.7 Flash → 3.6 Flash → 2.5 Flash fallback)
        List<String> targetUrls = Arrays.asList(
                "https://generativelanguage.googleapis.com/v1/models/" + modelName + ":generateContent?key=" + apiKey,
                "https://generativelanguage.googleapis.com/v1beta/models/" + modelName + ":generateContent?key=" + apiKey,
                "https://generativelanguage.googleapis.com/v1/models/gemini-3.6-flash:generateContent?key=" + apiKey,
                "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.6-flash:generateContent?key=" + apiKey,
                "https://generativelanguage.googleapis.com/v1/models/gemini-2.5-flash:generateContent?key=" + apiKey,
                "https://generativelanguage.googleapis.com/v1/models/gemini-2.0-flash:generateContent?key=" + apiKey
        );

        String lastErrorMsg = "";

        for (String url : targetUrls) {
            String maskedUrl = url.substring(0, url.indexOf("?key=")) + "?key=***";
            log.info("[GEMINI DEBUG] Sending request to URL: {}", maskedUrl);

            try {
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);

                // Body chuẩn Google REST API v1/v1beta
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
        return "Xin lỗi bạn, hiện tại hệ thống AI đang quá tải hoặc gặp gián đoạn kết nối. Bạn vui lòng thử lại sau ít phút nhé!";
    }
}

package com.example.blogsystem.controller;

import com.example.blogsystem.dto.AiRequest;
import com.example.blogsystem.dto.AiResponse;
import com.example.blogsystem.service.AiService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@RestController
@RequestMapping({"/ai", "/api/ai", "/v1/ai", "/api/v1/ai"})
@Slf4j
public class AiController {

    private final AiService aiService;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final ExecutorService executorService = Executors.newCachedThreadPool();

    public AiController(AiService aiService) {
        this.aiService = aiService;
    }

    @PostMapping("/chat")
    public ResponseEntity<AiResponse> chatWithAi(@RequestBody AiRequest request) {
        try {
            String prompt = request != null ? request.getPrompt() : "";
            String imageBase64 = request != null ? request.getImageBase64() : null;
            String imageMimeType = request != null ? request.getImageMimeType() : null;
            String reply = aiService.generateReply(prompt, imageBase64, imageMimeType);
            return ResponseEntity.ok(new AiResponse(reply));
        } catch (Exception e) {
            log.error("Lỗi khi xử lý chat AI: ", e);
            return ResponseEntity.ok(new AiResponse("Hiện tại hệ thống Trợ lý AI đang bận. Vui lòng bấm nút Thử lại!"));
        }
    }

    @PostMapping(value = {"/stream", "/chat/stream"}, produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public ResponseBodyEmitter streamChatWithAi(@RequestBody AiRequest request) {
        ResponseBodyEmitter emitter = new ResponseBodyEmitter(120_000L); // 120s timeout

        executorService.submit(() -> {
            try {
                String prompt = request != null ? request.getPrompt() : "";
                String imageBase64 = request != null ? request.getImageBase64() : null;
                String imageMimeType = request != null ? request.getImageMimeType() : null;

                aiService.streamReply(prompt, imageBase64, imageMimeType, chunk -> {
                    try {
                        Map<String, String> data = Collections.singletonMap("chunk", chunk);
                        emitter.send("data: " + objectMapper.writeValueAsString(data) + "\n\n");
                    } catch (Exception e) {
                        log.warn("Lỗi khi gửi chunk SSE: {}", e.getMessage());
                    }
                });

                emitter.send("data: [DONE]\n\n");
                emitter.complete();
            } catch (Exception e) {
                log.error("Lỗi khi stream chat AI: ", e);
                try {
                    Map<String, String> err = Collections.singletonMap("error", "Đã xảy ra sự cố kết nối. Vui lòng thử lại!");
                    emitter.send("data: " + objectMapper.writeValueAsString(err) + "\n\n");
                } catch (Exception ignored) {
                }
                emitter.completeWithError(e);
            }
        });

        return emitter;
    }
}

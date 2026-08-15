package com.example.blogsystem.controller;

import com.example.blogsystem.dto.AiRequest;
import com.example.blogsystem.dto.AiResponse;
import com.example.blogsystem.service.AiService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping({"/ai", "/api/ai", "/v1/ai", "/api/v1/ai"})
@Slf4j
public class AiController {

    private final AiService aiService;

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
            return ResponseEntity.ok(new AiResponse("Hiện tại hệ thống Trợ lý AI đang bận. Vui lòng thử lại sau!"));
        }
    }
}

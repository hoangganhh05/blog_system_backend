package com.example.blogsystem.controller;

import com.example.blogsystem.entity.PostTranslation;
import com.example.blogsystem.service.TranslationService;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping({"/posts", "/api/posts", "/v1/posts", "/api/v1/posts"})
@Slf4j
public class TranslationController {

    private final TranslationService translationService;

    public TranslationController(TranslationService translationService) {
        this.translationService = translationService;
    }

    @Data
    public static class TranslationRequest {
        private String targetLanguage;
    }

    @PostMapping("/{postId}/translations")
    public ResponseEntity<PostTranslation> translatePost(
            @PathVariable Long postId,
            @RequestBody TranslationRequest request) {
        String targetLang = request != null && request.getTargetLanguage() != null
                ? request.getTargetLanguage()
                : "vi";
        PostTranslation translation = translationService.translatePost(postId, targetLang);
        return ResponseEntity.ok(translation);
    }
}

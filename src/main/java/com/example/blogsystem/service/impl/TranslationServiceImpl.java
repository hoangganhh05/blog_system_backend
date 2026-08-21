package com.example.blogsystem.service.impl;

import com.example.blogsystem.entity.Post;
import com.example.blogsystem.entity.PostTranslation;
import com.example.blogsystem.repository.PostRepository;
import com.example.blogsystem.repository.PostTranslationRepository;
import com.example.blogsystem.service.AiService;
import com.example.blogsystem.service.TranslationService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Optional;

@Service
@Slf4j
public class TranslationServiceImpl implements TranslationService {

    private final PostRepository postRepository;
    private final PostTranslationRepository postTranslationRepository;
    private final AiService aiService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public TranslationServiceImpl(
            PostRepository postRepository,
            PostTranslationRepository postTranslationRepository,
            AiService aiService) {
        this.postRepository = postRepository;
        this.postTranslationRepository = postTranslationRepository;
        this.aiService = aiService;
    }

    @Override
    public String detectLanguage(String text) {
        if (text == null || text.isBlank()) {
            return "vi";
        }
        String cleanText = text.trim();
        if (cleanText.length() < 3) {
            return "vi";
        }

        try {
            String prompt = "Identify the ISO 639-1 language code (e.g. vi, en, ja, zh, ko, fr, es) for the following text. Reply ONLY with the 2-letter language code in lowercase:\n\"" + cleanText + "\"";
            String reply = aiService.generateReply(prompt);
            if (reply != null) {
                String code = reply.trim().toLowerCase().replaceAll("[^a-z]", "");
                if (code.length() >= 2) {
                    return code.substring(0, 2);
                }
            }
        } catch (Exception e) {
            log.warn("Language detection via AI failed: {}", e.getMessage());
        }

        return "vi";
    }

    @Override
    @Transactional
    public PostTranslation translatePost(Long postId, String targetLanguage) {
        if (postId == null || targetLanguage == null || targetLanguage.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid postId or targetLanguage");
        }

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Post not found"));

        String sourceTitle = post.getTitle() != null ? post.getTitle().trim() : "";
        String sourceContent = post.getContent() != null ? post.getContent().trim() : "";
        String fullSource = sourceTitle + "\n" + sourceContent;

        if (fullSource.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Post has no content to translate");
        }

        String currentHash = calculateHash(fullSource);
        String normTargetLang = targetLanguage.trim().toLowerCase();

        // Check Cache
        Optional<PostTranslation> cachedOpt = postTranslationRepository
                .findByPostIdAndTargetLanguage(postId, normTargetLang);

        if (cachedOpt.isPresent()) {
            PostTranslation cached = cachedOpt.get();
            if (currentHash.equals(cached.getSourceContentHash())) {
                log.info("Translation cache hit for postId={} targetLang={}", postId, normTargetLang);
                return cached;
            }
        }

        // Cache miss or invalidated -> Call Gemini AI Provider
        String prompt = buildTranslationPrompt(sourceTitle, sourceContent, post.getSourceLanguage(), normTargetLang);
        try {
            String aiReply = aiService.generateReply(prompt);
            String translatedTitle = sourceTitle;
            String translatedContent = sourceContent;

            if (aiReply != null && !aiReply.isBlank()) {
                try {
                    // Try parsing JSON response
                    String jsonStr = aiReply.trim();
                    if (jsonStr.startsWith("```json")) {
                        jsonStr = jsonStr.substring(7);
                    }
                    if (jsonStr.endsWith("```")) {
                        jsonStr = jsonStr.substring(0, jsonStr.length() - 3);
                    }
                    jsonStr = jsonStr.trim();

                    JsonNode root = objectMapper.readTree(jsonStr);
                    if (root.has("translatedTitle") && !root.get("translatedTitle").isNull()) {
                        translatedTitle = root.get("translatedTitle").asText();
                    }
                    if (root.has("translatedContent") && !root.get("translatedContent").isNull()) {
                        translatedContent = root.get("translatedContent").asText();
                    }
                } catch (Exception parseException) {
                    translatedContent = aiReply.trim();
                }
            }

            PostTranslation translation = cachedOpt.orElseGet(PostTranslation::new);
            translation.setPostId(postId);
            translation.setSourceLanguage(post.getSourceLanguage() != null ? post.getSourceLanguage() : "vi");
            translation.setTargetLanguage(normTargetLang);
            translation.setTranslatedTitle(translatedTitle);
            translation.setTranslatedContent(translatedContent);
            translation.setSourceContentHash(currentHash);

            return postTranslationRepository.save(translation);

        } catch (Exception e) {
            log.error("Failed to translate post id={} to lang={}: {}", postId, normTargetLang, e.getMessage());
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Không thể dịch bài viết lúc này. Vui lòng thử lại sau!");
        }
    }

    private String buildTranslationPrompt(String title, String content, String sourceLang, String targetLang) {
        return "Translate the following blog post title and content to language code '" + targetLang + "'. "
             + "Keep HTML or hashtags intact. Return ONLY a valid JSON object with format:\n"
             + "{\n"
             + "  \"translatedTitle\": \"...\",\n"
             + "  \"translatedContent\": \"...\"\n"
             + "}\n"
             + "Title: " + title + "\n"
             + "Content: " + content;
    }

    private String calculateHash(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            return String.valueOf(input.hashCode());
        }
    }
}

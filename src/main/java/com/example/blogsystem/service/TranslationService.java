package com.example.blogsystem.service;

import com.example.blogsystem.dto.PostDTO;
import com.example.blogsystem.entity.PostTranslation;

public interface TranslationService {
    String detectLanguage(String text);
    PostTranslation translatePost(Long postId, String targetLanguage);
}

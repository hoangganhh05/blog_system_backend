package com.example.blogsystem.service;

public interface AiService {
    String generateReply(String prompt);
    String generateReply(String prompt, String imageBase64, String imageMimeType);
}

package com.example.blogsystem.service;

import java.util.function.Consumer;

public interface AiService {
    String generateReply(String prompt);
    String generateReply(String prompt, String imageBase64, String imageMimeType);
    void streamReply(String prompt, String imageBase64, String imageMimeType, Consumer<String> onChunk);
}

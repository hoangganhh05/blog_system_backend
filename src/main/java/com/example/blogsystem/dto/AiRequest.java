package com.example.blogsystem.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AiRequest {
    private String prompt;
    private String imageBase64;
    private String imageMimeType;

    public AiRequest(String prompt) {
        this.prompt = prompt;
    }
}

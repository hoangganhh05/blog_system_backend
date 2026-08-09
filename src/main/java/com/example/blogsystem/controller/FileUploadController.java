package com.example.blogsystem.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/upload")
public class FileUploadController {

    private final String UPLOAD_DIR = "uploads";

    @PostMapping
    public ResponseEntity<Map<String, Object>> uploadFile(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            Map<String, Object> err = new HashMap<>();
            err.put("error", "Tệp không được để trống!");
            return ResponseEntity.badRequest().body(err);
        }

        try {
            // Tạo thư mục /uploads nếu chưa tồn tại
            File dir = new File(UPLOAD_DIR);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            // Đổi tên tệp bằng UUID để tránh trùng tên
            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }

            String newFilename = UUID.randomUUID().toString() + extension;
            Path filepath = Paths.get(UPLOAD_DIR, newFilename);

            // Lưu tệp vào đĩa
            Files.copy(file.getInputStream(), filepath);

            // Tạo URL để client truy cập
            String fileUrl = "http://localhost:8080/uploads/" + newFilename;

            Map<String, Object> response = new HashMap<>();
            response.put("url", fileUrl);
            response.put("filename", newFilename);
            return ResponseEntity.ok(response);

        } catch (IOException e) {
            Map<String, Object> err = new HashMap<>();
            err.put("error", "Lỗi tải tệp lên server: " + e.getMessage());
            return ResponseEntity.internalServerError().body(err);
        }
    }
}

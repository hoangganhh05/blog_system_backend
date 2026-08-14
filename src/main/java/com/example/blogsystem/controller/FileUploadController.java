package com.example.blogsystem.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.MediaType;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.Set;

@RestController
@RequestMapping("/upload")
public class FileUploadController {

    private static final Path UPLOAD_DIR = Paths.get("uploads").toAbsolutePath().normalize();
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024;
    private static final Set<String> ALLOWED_TYPES = Set.of("image/jpeg", "image/png", "image/gif", "image/webp");

    @PostMapping
    public ResponseEntity<Map<String, Object>> uploadFile(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            Map<String, Object> err = new HashMap<>();
            err.put("error", "Tệp không được để trống!");
            return ResponseEntity.badRequest().body(err);
        }
        if (file.getSize() > MAX_FILE_SIZE || !ALLOWED_TYPES.contains(file.getContentType())) {
            return ResponseEntity.badRequest().body(Map.of("error", "Chỉ chấp nhận ảnh JPG, PNG, GIF hoặc WebP, tối đa 5 MB."));
        }

        try {
            // Tạo thư mục /uploads nếu chưa tồn tại
            Files.createDirectories(UPLOAD_DIR);

            // Đổi tên tệp bằng UUID để tránh trùng tên
            String originalFilename = file.getOriginalFilename();
            String extension = switch (file.getContentType()) {
                case MediaType.IMAGE_JPEG_VALUE -> ".jpg";
                case MediaType.IMAGE_PNG_VALUE -> ".png";
                case MediaType.IMAGE_GIF_VALUE -> ".gif";
                default -> ".webp";
            };

            String newFilename = UUID.randomUUID().toString() + extension;
            Path filepath = UPLOAD_DIR.resolve(newFilename).normalize();
            if (!filepath.startsWith(UPLOAD_DIR)) throw new IOException("Invalid upload path");

            // Lưu tệp vào đĩa
            Files.copy(file.getInputStream(), filepath);

            // Tạo URL đường dẫn tương đối để client truy cập qua Reverse Proxy
            String fileUrl = "/uploads/" + newFilename;

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

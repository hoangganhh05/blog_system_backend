package com.example.blogsystem.controller;

import com.example.blogsystem.service.R2StorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Slf4j
@RestController
@RequestMapping({"/upload", "/api/upload", "/v1/upload", "/api/v1/upload"})
@RequiredArgsConstructor
public class FileUploadController {

    private final R2StorageService r2StorageService;

    private static final long MAX_FILE_SIZE = 50 * 1024 * 1024; // 50MB
    private static final Set<String> ALLOWED_IMAGE_TYPES = Set.of(
            "image/jpeg", "image/png", "image/gif", "image/webp", "image/svg+xml"
    );

    @PostMapping
    public ResponseEntity<Map<String, Object>> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "folder", required = false, defaultValue = "media") String folder) {
        if (file == null || file.isEmpty()) {
            Map<String, Object> err = new HashMap<>();
            err.put("error", "Tệp không được để trống!");
            return ResponseEntity.badRequest().body(err);
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            return ResponseEntity.badRequest().body(Map.of("error", "Kích thước tệp vượt quá giới hạn cho phép (tối đa 50MB)."));
        }

        try {
            String fileUrl = r2StorageService.uploadFile(file, folder);
            String fileName = fileUrl.substring(fileUrl.lastIndexOf("/") + 1);

            Map<String, Object> response = new HashMap<>();
            response.put("status", "SUCCESS");
            response.put("url", fileUrl);
            response.put("secureUrl", fileUrl);
            response.put("secure_url", fileUrl);
            response.put("filename", fileName);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Lỗi khi tải tệp lên Cloudflare R2: ", e);
            Map<String, Object> err = new HashMap<>();
            err.put("error", "Lỗi tải tệp lên máy chủ lưu trữ: " + e.getMessage());
            return ResponseEntity.internalServerError().body(err);
        }
    }

    @PostMapping({"/multiple", "/batch"})
    public ResponseEntity<?> uploadMultipleFiles(
            @RequestParam(value = "files", required = false) List<MultipartFile> files,
            @RequestParam(value = "images", required = false) List<MultipartFile> images,
            @RequestParam(value = "folder", required = false, defaultValue = "media") String folder) {
        List<MultipartFile> targetFiles = files != null ? files : images;
        if (targetFiles == null || targetFiles.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Danh sách tệp không được để trống!"));
        }

        try {
            List<String> urls = new ArrayList<>();
            List<String> filenames = new ArrayList<>();

            for (MultipartFile file : targetFiles) {
                if (file == null || file.isEmpty() || file.getSize() > MAX_FILE_SIZE) {
                    continue;
                }
                String fileUrl = r2StorageService.uploadFile(file, folder);
                String fileName = fileUrl.substring(fileUrl.lastIndexOf("/") + 1);
                urls.add(fileUrl);
                filenames.add(fileName);
            }

            Map<String, Object> response = new HashMap<>();
            response.put("status", "SUCCESS");
            response.put("urls", urls);
            response.put("filenames", filenames);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Lỗi khi tải nhiều tệp lên Cloudflare R2: ", e);
            return ResponseEntity.internalServerError().body(Map.of("error", "Lỗi tải tệp: " + e.getMessage()));
        }
    }
}

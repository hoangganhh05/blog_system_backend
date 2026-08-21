package com.example.blogsystem.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class R2StorageService {

    private final S3Client s3Client;

    @Value("${cloudflare.r2.bucket-name:blogviet-media}")
    private String bucketName;

    @Value("${cloudflare.r2.public-url:https://pub-9cc48deb69af42daac443957c40092f2.r2.dev}")
    private String publicUrl;

    public String uploadFile(MultipartFile file) {
        return uploadFile(file, "media");
    }

    public String uploadFile(MultipartFile file, String folder) {
        try {
            String originalFileName = file.getOriginalFilename();
            String extension = "";
            if (originalFileName != null && originalFileName.contains(".")) {
                extension = originalFileName.substring(originalFileName.lastIndexOf("."));
            }
            String cleanFolder = (folder != null && !folder.isBlank()) ? folder.replaceAll("^/+|/+$", "") + "/" : "";
            String fileName = cleanFolder + UUID.randomUUID() + extension;

            String contentType = file.getContentType();
            if (contentType == null || contentType.isBlank()) {
                contentType = "application/octet-stream";
            }

            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(fileName)
                    .contentType(contentType)
                    .build();

            s3Client.putObject(putObjectRequest, RequestBody.fromBytes(file.getBytes()));

            String baseUrl = publicUrl != null ? publicUrl.replaceAll("/+$", "") : "";
            String fileUrl = baseUrl + "/" + fileName;
            log.info("File uploaded successfully to Cloudflare R2: {}", fileUrl);
            return fileUrl;
        } catch (IOException e) {
            log.error("Failed to upload file to Cloudflare R2", e);
            throw new RuntimeException("Lỗi tải tệp tin lên máy chủ lưu trữ R2: " + e.getMessage(), e);
        }
    }

    /**
     * Xóa tệp tin khỏi Cloudflare R2 thông qua Object Key.
     */
    public void deleteFileByKey(String key) {
        if (key == null || key.isBlank()) {
            return;
        }
        try {
            // Chuẩn hóa key
            String cleanKey = key.replaceAll("^/+", "");
            log.info("Deleting R2 object key: {} from bucket: {}", cleanKey, bucketName);

            s3Client.deleteObject(
                software.amazon.awssdk.services.s3.model.DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(cleanKey)
                    .build()
            );
            log.info("Successfully deleted R2 object key: {}", cleanKey);
        } catch (Exception e) {
            log.warn("Lỗi khi xóa file khỏi Cloudflare R2 với key [{}]: {}", key, e.getMessage());
        }
    }

    /**
     * Xóa tệp tin khỏi Cloudflare R2 bằng URL hoàn chỉnh.
     */
    public void deleteFileByUrl(String fileUrl) {
        if (fileUrl == null || fileUrl.isBlank()) {
            return;
        }
        try {
            String key = extractKeyFromUrl(fileUrl);
            if (key != null && !key.isBlank()) {
                deleteFileByKey(key);
            } else {
                log.warn("Không thể tách object key hợp lệ từ URL: {}", fileUrl);
            }
        } catch (Exception e) {
            log.warn("Không thể xử lý xóa tệp tin R2 bằng URL [{}]: {}", fileUrl, e.getMessage());
        }
    }

    /**
     * Tách Object Key từ file URL công khai của R2.
     */
    public String extractKeyFromUrl(String fileUrl) {
        if (fileUrl == null || fileUrl.isBlank()) {
            return null;
        }
        String baseUrl = publicUrl != null ? publicUrl.replaceAll("/+$", "") : "";
        if (!baseUrl.isBlank() && fileUrl.startsWith(baseUrl)) {
            String relativePath = fileUrl.substring(baseUrl.length());
            return relativePath.replaceAll("^/+", "");
        }
        // Trường hợp URL chứa domain / path chung
        if (fileUrl.contains("r2.dev/")) {
            return fileUrl.substring(fileUrl.indexOf("r2.dev/") + "r2.dev/".length()).replaceAll("^/+", "");
        }
        if (fileUrl.contains("r2.cloudflarestorage.com/")) {
            String path = fileUrl.substring(fileUrl.indexOf("r2.cloudflarestorage.com/") + "r2.cloudflarestorage.com/".length());
            if (path.startsWith(bucketName + "/")) {
                path = path.substring((bucketName + "/").length());
            }
            return path.replaceAll("^/+", "");
        }
        // Fallback: Lấy phần path sau domain nếu có dạng http://domain.com/folder/file.ext
        if (fileUrl.startsWith("http://") || fileUrl.startsWith("https://")) {
            try {
                java.net.URI uri = new java.net.URI(fileUrl);
                String path = uri.getPath();
                if (path != null && !path.isBlank()) {
                    return path.replaceAll("^/+", "");
                }
            } catch (Exception ignored) {}
        }
        return null;
    }
}

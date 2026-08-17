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
}

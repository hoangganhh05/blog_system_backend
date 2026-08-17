package com.example.blogsystem.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;

import java.net.URI;

@Slf4j
@Configuration
public class R2Config {

    @Value("${cloudflare.r2.account-id:de44b90bad68efdbe5b36fef0628be2}")
    private String accountId;

    @Value("${cloudflare.r2.access-key:}")
    private String accessKey;

    @Value("${cloudflare.r2.secret-key:}")
    private String secretKey;

    @Bean
    public S3Client s3Client() {
        String endpoint = String.format("https://%s.r2.cloudflarestorage.com", accountId);

        if (accessKey == null || accessKey.isBlank() || secretKey == null || secretKey.isBlank()) {
            log.warn("[R2] R2_ACCESS_KEY / R2_SECRET_KEY chưa được cấu hình! Upload sẽ thất bại với lỗi xác thực.");
        } else {
            log.info("[R2] Khởi tạo S3Client -> endpoint={}, accountId={}, accessKey={}", endpoint, accountId, accessKey);
        }

        return S3Client.builder()
                .endpointOverride(URI.create(endpoint))
                .region(Region.of("auto"))
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(
                                accessKey != null ? accessKey : "",
                                secretKey != null ? secretKey : ""
                        )))
                // BẮT BUỘC cho Cloudflare R2 (xem tài liệu aws-sdk-java của Cloudflare):
                // 1. chunkedEncodingEnabled(false): AWS SDK v2 mặc định bật chunked transfer encoding
                //    cho putObject -> R2 trả về HTTP 403 SignatureDoesNotMatch nếu không tắt.
                // 2. pathStyleAccessEnabled(true): R2 yêu cầu path-style addressing cho endpoint
                //    <account-id>.r2.cloudflarestorage.com.
                .serviceConfiguration(S3Configuration.builder()
                        .pathStyleAccessEnabled(true)
                        .chunkedEncodingEnabled(false)
                        .build())
                .build();
    }
}

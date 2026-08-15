package com.example.blogsystem.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 1)
@Slf4j
public class EncryptionFilter extends OncePerRequestFilter {

    private final CryptoUtil cryptoUtil;
    private final ObjectMapper objectMapper;

    private static final List<String> EXCLUDED_PATHS = Arrays.asList(
            "/swagger-ui",
            "/v3/api-docs",
            "/upload",
            "/api/upload",
            "/uploads",
            "/audio",
            "/actuator"
    );

    public EncryptionFilter(CryptoUtil cryptoUtil, ObjectMapper objectMapper) {
        this.cryptoUtil = cryptoUtil;
        this.objectMapper = objectMapper;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        if (path == null) return false;
        return EXCLUDED_PATHS.stream().anyMatch(path::contains);
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        // Check if request is multipart/form-data (e.g. file uploads)
        String contentType = request.getContentType();
        boolean isMultipart = contentType != null && contentType.toLowerCase().startsWith("multipart/");

        HttpServletRequest requestToUse = request;

        // 1. Process Request Decryption
        if (!isMultipart && ("POST".equalsIgnoreCase(request.getMethod()) ||
                "PUT".equalsIgnoreCase(request.getMethod()) ||
                "PATCH".equalsIgnoreCase(request.getMethod()))) {

            byte[] bodyBytes = request.getInputStream().readAllBytes();
            if (bodyBytes.length > 0) {
                String rawBody = new String(bodyBytes, StandardCharsets.UTF_8);
                String isEncryptedHeader = request.getHeader("X-Encrypted");

                String decryptedBody = rawBody;

                try {
                    // Case A: Flagged with X-Encrypted header
                    if ("true".equalsIgnoreCase(isEncryptedHeader)) {
                        if (rawBody.trim().startsWith("{")) {
                            JsonNode node = objectMapper.readTree(rawBody);
                            if (node.has("encryptedData")) {
                                String cipherText = node.get("encryptedData").asText();
                                decryptedBody = cryptoUtil.decrypt(cipherText);
                            }
                        } else {
                            decryptedBody = cryptoUtil.decrypt(rawBody);
                        }
                    }
                    // Case B: Raw body contains { "encryptedData": "..." }
                    else if (rawBody.contains("encryptedData")) {
                        JsonNode node = objectMapper.readTree(rawBody);
                        if (node.has("encryptedData")) {
                            String cipherText = node.get("encryptedData").asText();
                            decryptedBody = cryptoUtil.decrypt(cipherText);
                        }
                    }
                } catch (Exception e) {
                    log.warn("[ENCRYPTION FILTER] Lỗi giải mã Request Body: {}", e.getMessage());
                }

                final byte[] finalBodyBytes = decryptedBody.getBytes(StandardCharsets.UTF_8);
                requestToUse = new DecryptedRequestWrapper(request, finalBodyBytes);
            }
        }

        // 2. Wrap Response for Encryption
        ContentCachingResponseWrapper responseWrapper = new ContentCachingResponseWrapper(response);

        try {
            filterChain.doFilter(requestToUse, responseWrapper);
        } finally {
            byte[] responseBytes = responseWrapper.getContentAsByteArray();
            String resContentType = responseWrapper.getContentType();

            boolean isEncryptedClient = "true".equalsIgnoreCase(request.getHeader("X-Encrypted"));
            boolean isJson = resContentType != null && resContentType.toLowerCase().contains("application/json");

            if (isEncryptedClient && isJson && responseBytes.length > 0) {
                String rawResponseJson = new String(responseBytes, StandardCharsets.UTF_8);
                String cipherResponse = cryptoUtil.encrypt(rawResponseJson);

                String wrappedJson = "{\"encryptedData\":\"" + cipherResponse + "\"}";
                byte[] encryptedBytes = wrappedJson.getBytes(StandardCharsets.UTF_8);

                response.setHeader("X-Encrypted", "true");
                response.setContentType("application/json;charset=UTF-8");
                response.setContentLength(encryptedBytes.length);
                response.getOutputStream().write(encryptedBytes);
                response.getOutputStream().flush();
            } else {
                responseWrapper.copyBodyToResponse();
            }
        }
    }

    /**
     * Custom HttpServletRequestWrapper holding decrypted body bytes
     */
    private static class DecryptedRequestWrapper extends HttpServletRequestWrapper {
        private final byte[] bodyData;

        public DecryptedRequestWrapper(HttpServletRequest request, byte[] bodyData) {
            super(request);
            this.bodyData = bodyData;
        }

        @Override
        public ServletInputStream getInputStream() {
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bodyData);
            return new ServletInputStream() {
                @Override
                public boolean isFinished() {
                    return byteArrayInputStream.available() == 0;
                }

                @Override
                public boolean isReady() {
                    return true;
                }

                @Override
                public void setReadListener(ReadListener readListener) {}

                @Override
                public int read() {
                    return byteArrayInputStream.read();
                }
            };
        }

        @Override
        public BufferedReader getReader() {
            return new BufferedReader(new InputStreamReader(getInputStream(), StandardCharsets.UTF_8));
        }

        @Override
        public int getContentLength() {
            return bodyData.length;
        }

        @Override
        public long getContentLengthLong() {
            return bodyData.length;
        }
    }
}

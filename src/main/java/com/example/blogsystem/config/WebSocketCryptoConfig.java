package com.example.blogsystem.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Configuration
@EnableWebSocket
@Slf4j
public class WebSocketCryptoConfig implements WebSocketConfigurer {

    private final CryptoUtil cryptoUtil;
    private final ObjectMapper objectMapper;
    private SecureRealtimeHandler handler;

    public WebSocketCryptoConfig(CryptoUtil cryptoUtil, @org.springframework.beans.factory.annotation.Autowired(required = false) ObjectMapper objectMapper) {
        this.cryptoUtil = cryptoUtil;
        this.objectMapper = (objectMapper != null) ? objectMapper : new ObjectMapper();
    }

    @org.springframework.context.annotation.Bean
    public SecureRealtimeHandler secureRealtimeHandler() {
        if (this.handler == null) {
            this.handler = new SecureRealtimeHandler(cryptoUtil, objectMapper);
        }
        return this.handler;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(secureRealtimeHandler(), "/ws/realtime", "/api/ws/realtime", "/ws/chat")
                .setAllowedOrigins("*");
    }

    @Slf4j
    public static class SecureRealtimeHandler extends TextWebSocketHandler {

        private final CryptoUtil cryptoUtil;
        private final ObjectMapper objectMapper;
        private final Map<String, WebSocketSession> activeSessions = new ConcurrentHashMap<>();

        public SecureRealtimeHandler(CryptoUtil cryptoUtil, ObjectMapper objectMapper) {
            this.cryptoUtil = cryptoUtil;
            this.objectMapper = objectMapper;
        }

        @Override
        public void afterConnectionEstablished(WebSocketSession session) {
            activeSessions.put(session.getId(), session);
            log.info("[SECURE WS] Đã kết nối kênh WebSocket an toàn ID: {}", session.getId());
        }

        @Override
        protected void handleTextMessage(WebSocketSession session, TextMessage message) {
            try {
                String payload = message.getPayload();
                String decryptedJson = payload;

                // 1. Tự động giải mã payload đến (nếu client gửi dạng mã hóa)
                if (payload.contains("encryptedData")) {
                    JsonNode node = objectMapper.readTree(payload);
                    if (node.has("encryptedData")) {
                        decryptedJson = cryptoUtil.decrypt(node.get("encryptedData").asText());
                    }
                } else {
                    decryptedJson = cryptoUtil.decrypt(payload);
                }

                log.debug("[SECURE WS DECRYPTED] Nhận gói tin: {}", decryptedJson);

                // 2. Tạo phản hồi thời gian thực và mã hóa toàn bộ frame trước khi phát đi
                String responsePayload = String.format("{\"status\":\"RECEIVED\",\"timestamp\":%d,\"data\":%s}",
                        System.currentTimeMillis(),
                        decryptedJson.trim().startsWith("{") ? decryptedJson : "\"" + decryptedJson + "\""
                );

                String cipherResponse = cryptoUtil.encrypt(responsePayload);
                String wrappedResponse = String.format("{\"encryptedData\":\"%s\"}", cipherResponse);

                session.sendMessage(new TextMessage(wrappedResponse));

            } catch (Exception e) {
                log.warn("[SECURE WS ERROR] Lỗi xử lý gói tin WebSocket: {}", e.getMessage());
            }
        }

        @Override
        public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
            activeSessions.remove(session.getId());
            log.info("[SECURE WS] Đã đóng kết nối kênh WebSocket ID: {}", session.getId());
        }

        /**
         * Phát tin nhắn mã hóa an toàn tới phiên WebSocket
         */
        public void broadcastEncryptedMessage(String messageJson) {
            String cipher = cryptoUtil.encrypt(messageJson);
            String wrapped = String.format("{\"encryptedData\":\"%s\"}", cipher);
            TextMessage textMessage = new TextMessage(wrapped);

            for (WebSocketSession s : activeSessions.values()) {
                if (s.isOpen()) {
                    try {
                        s.sendMessage(textMessage);
                    } catch (IOException e) {
                        log.warn("[SECURE WS BROADCAST ERROR]", e);
                    }
                }
            }
        }
    }
}

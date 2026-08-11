package com.example.blogsystem.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.Map;

/** Delivers a server-generated password-reset code without exposing it to the browser. */
@Service
public class PasswordResetMailService {
    private final RestClient emailJsClient = RestClient.create("https://api.emailjs.com/api/v1.0");

    @Value("${emailjs.service-id:}")
    private String serviceId;

    @Value("${emailjs.template-id:}")
    private String templateId;

    @Value("${emailjs.public-key:}")
    private String publicKey;

    public void sendResetCode(String recipient, String otp) {
        if (recipient == null || recipient.isBlank() || serviceId.isBlank() || templateId.isBlank() || publicKey.isBlank()) {
            throw new IllegalStateException("Password-reset email delivery is not configured");
        }

        emailJsClient.post()
                .uri("/email/send")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of(
                        "service_id", serviceId,
                        "template_id", templateId,
                        "user_id", publicKey,
                        "template_params", Map.of(
                                "to_email", recipient,
                                "email", recipient,
                                "user_email", recipient,
                                "to_name", recipient.substring(0, recipient.indexOf('@')),
                                "otp_code", otp,
                                "otp", otp,
                                "passcode", otp,
                                "message", "Mã OTP khôi phục mật khẩu BlogViet của bạn là: " + otp + ". Mã có hiệu lực trong 10 phút."
                        )
                ))
                .retrieve()
                .toBodilessEntity();
    }
}

# 🚀 BlogViet Backend — Spring Boot 4 & Java 21 LTS

<div align="center">

**BlogViet Backend** là hệ thống máy chủ dịch vụ (Backend API Service) cho nền tảng mạng xã hội và chia sẻ nội dung trực tuyến BlogViet, xây dựng trên nền tảng **Spring Boot 4.1**, **Java 21 LTS**, **PostgreSQL**, **Spring Security JWT**, **WebSocket STOMP** và **Google Gemini AI 3.7 Flash**.

[![Spring Boot](https://img.shields.io/badge/Spring_Boot-4.1.0-6DB33F?style=for-the-badge&logo=springboot&logoColor=white)](https://spring.io/projects/spring-boot)
[![Java](https://img.shields.io/badge/Java-21_LTS-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)](https://openjdk.org/)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-4169E1?style=for-the-badge&logo=postgresql&logoColor=white)](https://www.postgresql.org/)
[![Gemini AI](https://img.shields.io/badge/Gemini_AI-3.7_Flash-4285F4?style=for-the-badge&logo=google&logoColor=white)](https://ai.google.dev/)

</div>

---

## 🛠 Kiến Trúc Kỹ Thuật

- **Framework:** Spring Boot 4.1.0 (Java 21 LTS)
- **Database:** PostgreSQL (Spring Data JPA / Hibernate)
- **Security:** Spring Security + JWT Stateless Authentication
- **Real-time Messaging:** Spring WebSocket (STOMP Protocol over SockJS)
- **AI Integration:** Google Gemini AI API (Model `gemini-3.7-flash` với fallback động)
- **Privacy Guardian:** Cơ chế lọc bảo mật trạng thái hoạt động theo quan hệ hội thoại nhắn tin thực tế (`ChatMessageRepository.existsBetweenUsers`).

---

## 🚀 Hướng Dẫn Chạy Dự Án

### 1. Cấu hình Cơ sở dữ liệu
Cấu hình thông tin kết nối trong `src/main/resources/application.properties`:
```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/blog_system
spring.datasource.username=postgres
spring.datasource.password=your_password
jwt.secret=your_super_secret_jwt_key
gemini.api.key=your_gemini_api_key
```

### 2. Biên dịch & Khởi chạy
```bash
# Kiểm tra biên dịch (0 errors)
./mvnw clean compile -Dmaven.test.skip=true

# Khởi chạy server
./mvnw spring-boot:run
```

---

## 👤 Tác Giả
- **Hoàng Anh** — [@hoangganhh05](https://github.com/hoangganhh05)
- **Website:** [anhhoangg.id.vn](https://anhhoangg.id.vn/)

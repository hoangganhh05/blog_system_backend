package com.example.blogsystem.config;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;
import java.net.URI;

@Configuration
public class DataSourceConfig {

    @Value("${spring.datasource.url}")
    private String rawUrl;

    @Value("${spring.datasource.username:}")
    private String rawUsername;

    @Value("${spring.datasource.password:}")
    private String rawPassword;

    @Bean
    @Primary
    public DataSource dataSource() {
        HikariDataSource dataSource = new HikariDataSource();

        String url = rawUrl;
        String username = rawUsername;
        String password = rawPassword;

        // Xử lý tự động chuẩn hóa URL postgres:// hoặc postgresql:// hoặc jdbc:postgresql://user:pass@host
        if (url != null && (url.startsWith("postgres://") || url.startsWith("postgresql://") || url.contains("@"))) {
            try {
                String cleanUrl = url.replace("jdbc:", "");
                URI uri = new URI(cleanUrl);

                String host = uri.getHost();
                int port = uri.getPort() == -1 ? 5432 : uri.getPort();
                String path = uri.getPath();

                if (uri.getUserInfo() != null) {
                    String[] userInfo = uri.getUserInfo().split(":");
                    if (username.isEmpty() || username.equals("sa")) username = userInfo[0];
                    if (password.isEmpty()) password = userInfo.length > 1 ? userInfo[1] : "";
                }

                url = "jdbc:postgresql://" + host + ":" + port + path + "?sslmode=require";
            } catch (Exception ignored) {
            }
        }

        dataSource.setJdbcUrl(url);
        if (username != null && !username.isEmpty()) dataSource.setUsername(username);
        if (password != null && !password.isEmpty()) dataSource.setPassword(password);

        return dataSource;
    }
}

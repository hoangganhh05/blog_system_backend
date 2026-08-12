package com.example.blogsystem;

import com.example.blogsystem.controller.PostLikeController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
class PostLikeControllerTest {

    @Autowired
    private PostLikeController postLikeController;

    @Test
    void shouldExposeReactionsListEndpoint() {
        ResponseEntity<List<Map<String, Object>>> response = postLikeController.getReactionsList(999L);
        assertNotNull(response);
        assertNotNull(response.getBody());
    }
}

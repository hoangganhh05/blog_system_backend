package com.example.blogsystem;

import com.example.blogsystem.controller.PostLikeController;
import com.example.blogsystem.dto.UserReactionDTO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
class PostLikeControllerTest {

    @Autowired
    private PostLikeController postLikeController;

    @Test
    void shouldExposeReactionsListEndpoint() {
        ResponseEntity<List<UserReactionDTO>> response = postLikeController.getReactionsList(999L);
        assertNotNull(response);
        assertNotNull(response.getBody());
    }
}

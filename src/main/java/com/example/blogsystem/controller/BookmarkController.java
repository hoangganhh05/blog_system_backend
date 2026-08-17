package com.example.blogsystem.controller;

import com.example.blogsystem.dto.BookmarkDTO;
import com.example.blogsystem.dto.DTOMapper;
import com.example.blogsystem.entity.Bookmark;
import com.example.blogsystem.config.CurrentUser;
import com.example.blogsystem.service.BookmarkService;
import com.example.blogsystem.repository.PostRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
public class BookmarkController {

    private final BookmarkService bookmarkService;
    private final PostRepository postRepository;
    private final CurrentUser currentUser;

    public BookmarkController(BookmarkService bookmarkService, PostRepository postRepository, CurrentUser currentUser) {
        this.bookmarkService = bookmarkService;
        this.postRepository = postRepository;
        this.currentUser = currentUser;
    }

    @PostMapping("/posts/{postId}/bookmark")
    public ResponseEntity<Map<String, Object>> toggleBookmark(
            @PathVariable Long postId) {
        if (postId == null || !postRepository.existsById(postId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Bài viết không tồn tại");
        }
        boolean bookmarked = bookmarkService.toggleBookmark(currentUser.id(), postId);
        Map<String, Object> response = new HashMap<>();
        response.put("bookmarked", bookmarked);
        response.put("isBookmarked", bookmarked);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/posts/{postId}/bookmark/check")
    public ResponseEntity<Map<String, Object>> checkBookmarked(
            @PathVariable Long postId) {
        if (postId == null || !postRepository.existsById(postId)) {
            Map<String, Object> response = new HashMap<>();
            response.put("bookmarked", false);
            response.put("isBookmarked", false);
            return ResponseEntity.ok(response);
        }
        Long userId = currentUser.idOrNull();
        boolean bookmarked = userId != null && bookmarkService.isBookmarkedByUser(userId, postId);
        Map<String, Object> response = new HashMap<>();
        response.put("bookmarked", bookmarked);
        response.put("isBookmarked", bookmarked);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/users/{userId}/bookmarks")
    public ResponseEntity<List<BookmarkDTO>> getUserBookmarks(@PathVariable Long userId) {
        currentUser.requireOwnerOrAdmin(userId);
        List<Bookmark> bookmarks = bookmarkService.getUserBookmarks(userId);
        List<BookmarkDTO> bookmarkDTOs = bookmarks.stream()
                .map(DTOMapper::toBookmarkDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(bookmarkDTOs);
    }
}

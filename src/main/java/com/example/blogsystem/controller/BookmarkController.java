package com.example.blogsystem.controller;

import com.example.blogsystem.entity.Bookmark;
import com.example.blogsystem.service.BookmarkService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class BookmarkController {

    private final BookmarkService bookmarkService;

    public BookmarkController(BookmarkService bookmarkService) {
        this.bookmarkService = bookmarkService;
    }

    @PostMapping("/posts/{postId}/bookmark")
    public ResponseEntity<Map<String, Object>> toggleBookmark(
            @PathVariable Long postId,
            @RequestParam Long userId) {
        boolean bookmarked = bookmarkService.toggleBookmark(userId, postId);
        Map<String, Object> response = new HashMap<>();
        response.put("bookmarked", bookmarked);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/posts/{postId}/bookmark/check")
    public ResponseEntity<Map<String, Object>> checkBookmarked(
            @PathVariable Long postId,
            @RequestParam Long userId) {
        boolean bookmarked = bookmarkService.isBookmarkedByUser(userId, postId);
        Map<String, Object> response = new HashMap<>();
        response.put("bookmarked", bookmarked);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/users/{userId}/bookmarks")
    public ResponseEntity<List<Bookmark>> getUserBookmarks(@PathVariable Long userId) {
        List<Bookmark> bookmarks = bookmarkService.getUserBookmarks(userId);
        return ResponseEntity.ok(bookmarks);
    }
}

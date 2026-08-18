package com.example.blogsystem.controller;

import com.example.blogsystem.dto.BookmarkDTO;
import com.example.blogsystem.dto.DTOMapper;
import com.example.blogsystem.entity.Bookmark;
import com.example.blogsystem.config.CurrentUser;
import com.example.blogsystem.service.BookmarkService;
import com.example.blogsystem.repository.PostRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
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

    @PostMapping({"/posts/{postId}/bookmark", "/api/posts/{postId}/bookmark", "/v1/posts/{postId}/bookmark", "/api/v1/posts/{postId}/bookmark"})
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

    @GetMapping({"/posts/{postId}/bookmark/check", "/api/posts/{postId}/bookmark/check", "/v1/posts/{postId}/bookmark/check", "/api/v1/posts/{postId}/bookmark/check"})
    public ResponseEntity<Map<String, Object>> checkBookmarked(
            @PathVariable Long postId) {
        // Response an toàn mặc định — luôn trả về 200, không bắn 500
        Map<String, Object> safeDefault = new HashMap<>();
        safeDefault.put("bookmarked", false);
        safeDefault.put("isBookmarked", false);

        if (postId == null || !postRepository.existsById(postId)) {
            return ResponseEntity.ok(safeDefault);
        }

        try {
            Long userId = currentUser.idOrNull(); // null nếu User chưa đăng nhập -> bookmarked=false
            boolean bookmarked = userId != null && bookmarkService.isBookmarkedByUser(userId, postId);
            Map<String, Object> response = new HashMap<>();
            response.put("bookmarked", bookmarked);
            response.put("isBookmarked", bookmarked);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("[checkBookmarked] Lỗi khi kiểm tra bookmark của user. postId={}", postId, e);
            return ResponseEntity.ok(safeDefault);
        }
    }

    @GetMapping({"/users/{userId}/bookmarks", "/api/users/{userId}/bookmarks", "/v1/users/{userId}/bookmarks", "/api/v1/users/{userId}/bookmarks"})
    public ResponseEntity<List<BookmarkDTO>> getUserBookmarks(@PathVariable Long userId) {
        try {
            // Check authentication safely without throwing exceptions
            Long currentUserId = currentUser.idOrNull();
            if (currentUserId == null) {
                // User not authenticated - return empty list instead of throwing 401
                log.warn("[getUserBookmarks] User not authenticated, returning empty list. userId={}", userId);
                return ResponseEntity.ok(java.util.Collections.emptyList());
            }
            
            // Check if current user is owner or admin
            if (!currentUserId.equals(userId) && !currentUser.isAdmin()) {
                // User not authorized to view this user's bookmarks - return empty list
                log.warn("[getUserBookmarks] User not authorized to view bookmarks. currentUserId={}, requestedUserId={}", currentUserId, userId);
                return ResponseEntity.ok(java.util.Collections.emptyList());
            }
            
            List<Bookmark> bookmarks = bookmarkService.getUserBookmarks(userId);
            List<BookmarkDTO> bookmarkDTOs = bookmarks.stream()
                    .map(DTOMapper::toBookmarkDTO)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(bookmarkDTOs);
        } catch (Exception e) {
            log.error("[getUserBookmarks] Lỗi khi lấy danh sách bookmark của user. userId={}", userId, e);
            return ResponseEntity.ok(java.util.Collections.emptyList());
        }
    }
}

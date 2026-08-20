package com.example.blogsystem.controller;


import com.example.blogsystem.dto.PostDTO;
import com.example.blogsystem.dto.DTOMapper;
import com.example.blogsystem.entity.Post;
import com.example.blogsystem.service.PostService;
import com.example.blogsystem.config.CurrentUser;
import com.example.blogsystem.repository.PostRepository;
import com.example.blogsystem.repository.PostLikeRepository;
import com.example.blogsystem.repository.CommentRepository;
import com.example.blogsystem.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping({"/posts", "/api/posts", "/v1/posts", "/api/v1/posts"})
@Slf4j
public class PostController {

    private final PostService postService;
    private final PostRepository postRepository;
    private final PostLikeRepository postLikeRepository;
    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final CurrentUser currentUser;

    public PostController(PostService postService, PostRepository postRepository, PostLikeRepository postLikeRepository, CommentRepository commentRepository, UserRepository userRepository, CurrentUser currentUser) {
        this.postService = postService;
        this.postRepository = postRepository;
        this.postLikeRepository = postLikeRepository;
        this.commentRepository = commentRepository;
        this.userRepository = userRepository;
        this.currentUser = currentUser;
    }

    // Map Post -> PostDTO and enrich real like/comment/share counts from the DB
    private PostDTO toPostDTOWithCounts(Post post) {
        PostDTO dto = DTOMapper.toPostDTO(post);
        if (dto == null || dto.getId() == null) return dto;
        try {
            dto.setLikesCount(postLikeRepository.countByPostId(dto.getId()));
        } catch (Exception ignored) {}
        try {
            dto.setCommentsCount(commentRepository.countByPostId(dto.getId()));
        } catch (Exception ignored) {}
        try {
            dto.setSharesCount(postRepository.countBySharedPostId(dto.getId()));
        } catch (Exception ignored) {}
        return dto;
    }

    @GetMapping("/category/{categoryId}")
    public Page<PostDTO> getPostsByCategory(
            @PathVariable Long categoryId,
            Pageable pageable) {
        try {
            return postService.getPostsByCategory(categoryId, pageable)
                    .map(this::toPostDTOWithCounts);
        } catch (Exception e) {
            return Page.empty();
        }
    }
    @GetMapping
    public Page<PostDTO> getPosts(Pageable pageable) {
        try {
            return postService.getAllPosts(pageable)
                    .map(this::toPostDTOWithCounts);
        } catch (Exception e) {
            return Page.empty();
        }
    }
    @GetMapping("/search")
    public Page<PostDTO> searchPosts(@RequestParam String query, Pageable pageable) {
        try {
            return postService.searchPosts(query, pageable)
                    .map(this::toPostDTOWithCounts);
        } catch (Exception e) {
            return Page.empty();
        }
    }
    @GetMapping("/{id}")
    public ResponseEntity<PostDTO> getPostById(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(toPostDTOWithCounts(postService.getPostById(id)));
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/{id}/view")
    public ResponseEntity<PostDTO> incrementView(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(toPostDTOWithCounts(postService.incrementViewCount(id)));
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
    @PostMapping
    public PostDTO createPost(@RequestBody Post post) {
        try {
            return toPostDTOWithCounts(postService.createPost(post, currentUser.id()));
        } catch (Exception e) {
            log.error("Lỗi tạo bài viết: ", e);
            throw e;
        }
    }
    @PutMapping("/{id}")
    public ResponseEntity<PostDTO> updatePost(@PathVariable Long id, @RequestBody Post post) {
        Post existing = postRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy bài viết ID: " + id));
        currentUser.requireOwnerOrAdmin(existing.getUser().getId());
        Post updated = postService.updatePost(id, post);
        return ResponseEntity.ok(toPostDTOWithCounts(updated));
    }

    // Lấy số lượt chia sẻ (số bài viết trỏ sharedPost tới bài này) từ server
    @GetMapping("/{id}/shares/count")
    public ResponseEntity<Map<String, Object>> getShareCount(@PathVariable Long id) {
        try {
            if (!postRepository.existsById(id)) {
                return ResponseEntity.ok(Map.of("count", 0L));
            }
            long count = postRepository.countBySharedPostId(id);
            return ResponseEntity.ok(Map.of("count", count));
        } catch (Exception e) {
            return ResponseEntity.ok(Map.of("count", 0L));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deletePost(@PathVariable Long id) {
        Post existing = postRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy bài viết ID: " + id));
        currentUser.requireOwnerOrAdmin(existing.getUser().getId());
        postService.deletePost(id);
        return ResponseEntity.ok(Map.of("success", true, "message", "Đã xóa bài viết thành công", "deletedId", id));
    }
}

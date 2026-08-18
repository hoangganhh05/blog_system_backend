package com.example.blogsystem.controller;

import com.example.blogsystem.dto.CommentDTO;
import com.example.blogsystem.entity.Comment;
import com.example.blogsystem.service.CommentService;
import com.example.blogsystem.config.CurrentUser;
import com.example.blogsystem.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping({"/comments", "/api/comments", "/v1/comments", "/api/v1/comments"})
public class CommentController {

    private final CommentService commentService;
    private final UserRepository userRepository;
    private final CurrentUser currentUser;

    public CommentController(CommentService commentService, UserRepository userRepository, CurrentUser currentUser) {
        this.commentService = commentService;
        this.userRepository = userRepository;
        this.currentUser = currentUser;
    }

    // Lấy comments theo bài viết — bắt buộc truyền postId để tránh LazyInitializationException
    @GetMapping
    public ResponseEntity<List<CommentDTO>> getComments(@RequestParam Long postId) {
        try {
            return ResponseEntity.ok(commentService.getCommentsByPostId(postId));
        } catch (Exception e) {
            return ResponseEntity.ok(java.util.Collections.emptyList());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<CommentDTO> getCommentById(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(commentService.getCommentById(id));
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping
    public CommentDTO createComment(@RequestBody Comment comment) {
        comment.setUser(userRepository.getReferenceById(currentUser.id()));
        return commentService.createComment(comment);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CommentDTO> updateComment(@PathVariable Long id,
                                 @RequestBody Comment comment) {
        try {
            CommentDTO existingComment = commentService.getCommentById(id);
            if (existingComment == null || existingComment.getUser() == null) {
                return ResponseEntity.notFound().build();
            }
            currentUser.requireOwnerOrAdmin(existingComment.getUser().getId());
            CommentDTO updated = commentService.updateComment(id, comment);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public void deleteComment(@PathVariable Long id) {
        currentUser.requireOwnerOrAdmin(commentService.getCommentById(id).getUser().getId());
        commentService.deleteComment(id);
    }
}

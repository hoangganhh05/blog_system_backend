package com.example.blogsystem.controller;

import com.example.blogsystem.entity.Comment;
import com.example.blogsystem.service.CommentService;
import com.example.blogsystem.config.CurrentUser;
import com.example.blogsystem.repository.UserRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/comments")
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
    public List<Comment> getComments(@RequestParam Long postId) {
        return commentService.getCommentsByPostId(postId);
    }

    @GetMapping("/{id}")
    public Comment getCommentById(@PathVariable Long id) {
        return commentService.getCommentById(id);
    }

    @PostMapping
    public Comment createComment(@RequestBody Comment comment) {
        comment.setUser(userRepository.getReferenceById(currentUser.id()));
        return commentService.createComment(comment);
    }

    @PutMapping("/{id}")
    public Comment updateComment(@PathVariable Long id,
                                 @RequestBody Comment comment) {
        currentUser.requireOwnerOrAdmin(commentService.getCommentById(id).getUser().getId());
        return commentService.updateComment(id, comment);
    }

    @DeleteMapping("/{id}")
    public void deleteComment(@PathVariable Long id) {
        currentUser.requireOwnerOrAdmin(commentService.getCommentById(id).getUser().getId());
        commentService.deleteComment(id);
    }
}

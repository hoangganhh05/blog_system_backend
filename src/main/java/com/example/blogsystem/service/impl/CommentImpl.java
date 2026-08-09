package com.example.blogsystem.service.impl;

import com.example.blogsystem.entity.Comment;
import com.example.blogsystem.entity.Post;
import com.example.blogsystem.entity.User;
import com.example.blogsystem.repository.CommentRepository;
import com.example.blogsystem.repository.PostRepository;
import com.example.blogsystem.repository.UserRepository;
import com.example.blogsystem.service.CommentService;
import com.example.blogsystem.service.NotificationService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class CommentImpl implements CommentService {
    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    public CommentImpl(CommentRepository commentRepository, PostRepository postRepository, UserRepository userRepository, NotificationService notificationService) {
        this.commentRepository = commentRepository;
        this.postRepository = postRepository;
        this.userRepository = userRepository;
        this.notificationService = notificationService;
    }

    @Override
    public List<Comment> getAllComments() {
        return commentRepository.findAll();
    }

    @Override
    public Comment getCommentById(Long id) {
        return commentRepository.findById(id).orElseThrow(() -> new RuntimeException("Comment not found"));
    }

    @Override
    public List<Comment> getCommentsByPostId(Long postId) {
        if (!postRepository.existsById(postId)) {
            throw new RuntimeException("Post not found");
        }
        return commentRepository.findByPostId(postId);
    }

    @Override
    public Comment createComment(Comment comment) {
        if (comment.getCreatedAt() == null) {
            comment.setCreatedAt(LocalDateTime.now());
        }

        // Fetch full Post entity
        if (comment.getPost() != null && comment.getPost().getId() != null) {
            Post post = postRepository.findById(comment.getPost().getId()).orElse(null);
            comment.setPost(post);
        }

        // Fetch full User entity
        if (comment.getUser() != null && comment.getUser().getId() != null) {
            User user = userRepository.findById(comment.getUser().getId()).orElse(null);
            comment.setUser(user);
        }

        Comment saved = commentRepository.save(comment);

        // Gửi thông báo cho tác giả bài viết (chỉ khi người comment KHÔNG phải chính là tác giả bài viết)
        if (saved.getPost() != null && saved.getPost().getUser() != null) {
            User author = saved.getPost().getUser();
            User sender = saved.getUser();

            if (author != null && sender != null && !author.getId().equals(sender.getId())) {
                String senderName = sender.getFullName() != null ? sender.getFullName() : sender.getUsername();
                notificationService.createNotification(
                    author,
                    sender,
                    saved.getPost(),
                    senderName + " đã bình luận về bài viết: \"" + saved.getPost().getTitle() + "\""
                );
            }
        }


        return saved;
    }

    @Override
    public Comment updateComment(Long id, Comment comment) {
        Comment existingComment = commentRepository.findById(id).orElseThrow(() -> new RuntimeException("Comment not found"));
        existingComment.setContent(comment.getContent());
        existingComment.setCreatedAt(comment.getCreatedAt());
        return commentRepository.save(existingComment);
    }
    @Override
    public void deleteComment(Long id) {
        if(!commentRepository.existsById(id)) {
            throw new RuntimeException("Comment not found");
        }
        commentRepository.deleteById(id);
    }
}

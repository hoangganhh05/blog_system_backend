package com.example.blogsystem.service.impl;

import com.example.blogsystem.dto.CommentDTO;
import com.example.blogsystem.dto.DTOMapper;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

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
    public List<CommentDTO> getAllComments() {
        return commentRepository.findAll().stream()
                .map(DTOMapper::toCommentDTO)
                .collect(Collectors.toList());
    }

    @Override
    public CommentDTO getCommentById(Long id) {
        Comment comment = commentRepository.findById(id).orElseThrow(() -> new RuntimeException("Comment not found"));
        return DTOMapper.toCommentDTO(comment);
    }

    @Override
    public List<CommentDTO> getCommentsByPostId(Long postId) {
        if (!postRepository.existsById(postId)) {
            throw new RuntimeException("Post not found");
        }
        // Use JOIN FETCH query to avoid LazyInitializationException
        return commentRepository.findByPostIdWithUser(postId).stream()
                .map(DTOMapper::toCommentDTO)
                .collect(Collectors.toList());
    }

    @Override
    public CommentDTO createComment(Comment comment) {
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

        // Detect and send notifications for mentioned users (@username)
        try {
            if (saved.getContent() != null && saved.getUser() != null) {
                Pattern mentionPattern = Pattern.compile("@(\\w+)");
                Matcher matcher = mentionPattern.matcher(saved.getContent());
                
                while (matcher.find()) {
                    String mentionedUsername = matcher.group(1);
                    try {
                        User mentionedUser = userRepository.findByUsername(mentionedUsername).orElse(null);
                        if (mentionedUser != null && 
                            !mentionedUser.getId().equals(saved.getUser().getId())) {
                            String senderName = saved.getUser().getFullName() != null ? 
                                saved.getUser().getFullName() : saved.getUser().getUsername();
                            notificationService.createNotification(
                                mentionedUser,
                                saved.getUser(),
                                saved.getPost(),
                                senderName + " đã nhắc đến bạn trong một bình luận"
                            );
                        }
                    } catch (Exception e) {
                        // Log error but continue processing other mentions
                        System.err.println("Error processing mention for user: " + mentionedUsername);
                    }
                }
            }
        } catch (Exception e) {
            // Don't fail comment creation if mention processing fails
            System.err.println("Error processing mentions in comment: " + e.getMessage());
        }

        return DTOMapper.toCommentDTO(saved);
    }

    @Override
    public CommentDTO updateComment(Long id, Comment comment) {
        Comment existingComment = commentRepository.findById(id).orElseThrow(() -> new RuntimeException("Comment not found"));
        existingComment.setContent(comment.getContent());
        existingComment.setCreatedAt(comment.getCreatedAt());
        return DTOMapper.toCommentDTO(commentRepository.save(existingComment));
    }

    @Override
    public void deleteComment(Long id) {
        if(!commentRepository.existsById(id)) {
            throw new RuntimeException("Comment not found");
        }
        commentRepository.deleteById(id);
    }
}

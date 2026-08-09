package com.example.blogsystem.service;

import com.example.blogsystem.entity.Comment;
import org.springframework.stereotype.Service;

import java.util.List;
@Service
public interface CommentService {

    List<Comment> getAllComments();

    List<Comment> getCommentsByPostId(Long postId);

    Comment getCommentById(Long id);

    Comment createComment(Comment comment);

    Comment updateComment(Long id, Comment comment);

    void deleteComment(Long id);
}
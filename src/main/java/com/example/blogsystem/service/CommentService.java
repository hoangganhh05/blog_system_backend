package com.example.blogsystem.service;

import com.example.blogsystem.dto.CommentDTO;
import com.example.blogsystem.entity.Comment;
import org.springframework.stereotype.Service;

import java.util.List;
@Service
public interface CommentService {

    List<CommentDTO> getAllComments();

    List<CommentDTO> getCommentsByPostId(Long postId);

    CommentDTO getCommentById(Long id);

    CommentDTO createComment(Comment comment);

    CommentDTO updateComment(Long id, Comment comment);

    void deleteComment(Long id);
}
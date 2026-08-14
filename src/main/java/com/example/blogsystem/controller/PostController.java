package com.example.blogsystem.controller;


import com.example.blogsystem.dto.PostDTO;
import com.example.blogsystem.dto.DTOMapper;
import com.example.blogsystem.entity.Post;
import com.example.blogsystem.service.PostService;
import com.example.blogsystem.config.CurrentUser;
import com.example.blogsystem.repository.PostRepository;
import com.example.blogsystem.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping({"/posts", "/api/v1/posts"})
@Slf4j
public class PostController {

    private final PostService postService;
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final CurrentUser currentUser;

    public PostController(PostService postService, PostRepository postRepository, UserRepository userRepository, CurrentUser currentUser) {
        this.postService = postService;
        this.postRepository = postRepository;
        this.userRepository = userRepository;
        this.currentUser = currentUser;
    }

    @GetMapping("/category/{categoryId}")
    public Page<PostDTO> getPostsByCategory(
            @PathVariable Long categoryId,
            Pageable pageable) {

        return postService.getPostsByCategory(categoryId, pageable)
                .map(DTOMapper::toPostDTO);
    }
    @GetMapping
    public Page<PostDTO> getPosts(Pageable pageable) {
        return postService.getAllPosts(pageable)
                .map(DTOMapper::toPostDTO);
    }
    @GetMapping("/search")
    public Page<PostDTO> searchPosts(@RequestParam String query, Pageable pageable) {
        return postService.searchPosts(query, pageable)
                .map(DTOMapper::toPostDTO);
    }
    @GetMapping("/{id}")
    public PostDTO getPostById(@PathVariable Long id) {
        return DTOMapper.toPostDTO(postService.getPostById(id));
    }

    @PostMapping("/{id}/view")
    public PostDTO incrementView(@PathVariable Long id) {
        return DTOMapper.toPostDTO(postService.incrementViewCount(id));
    }
    @PostMapping
    public PostDTO createPost(@RequestBody Post post) {
        try {
            return DTOMapper.toPostDTO(postService.createPost(post, currentUser.id()));
        } catch (Exception e) {
            log.error("Lỗi tạo bài viết: ", e);
            throw e;
        }
    }
    @PutMapping("/{id}")
    public PostDTO updatePost(@PathVariable Long id, @RequestBody Post post) {
        currentUser.requireOwnerOrAdmin(postRepository.findById(id).orElseThrow().getUser().getId());
        return DTOMapper.toPostDTO(postService.updatePost(id, post));
    }
    @DeleteMapping("/{id}")
    public void deletePost(@PathVariable Long id) {
        currentUser.requireOwnerOrAdmin(postRepository.findById(id).orElseThrow().getUser().getId());
        postService.deletePost(id);
    }
}

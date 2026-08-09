package com.example.blogsystem.controller;


import com.example.blogsystem.entity.Post;
import com.example.blogsystem.service.PostService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/posts")
public class PostController {

    private final PostService postService;

    public PostController(PostService postService) {
        this.postService = postService;
    }

    @GetMapping("/category/{categoryId}")
    public Page<Post> getPostsByCategory(
            @PathVariable Long categoryId,
            Pageable pageable) {

        return postService.getPostsByCategory(categoryId, pageable);
    }
    @GetMapping
    public Page<Post> getPosts(Pageable pageable) {
        return postService.getAllPosts(pageable);
    }
    @GetMapping("/search")
    public Page<Post> searchPosts(@RequestParam String query, Pageable pageable) {
        return postService.searchPosts(query, pageable);
    }
    @GetMapping("/{id}")
    public Post getPostById(@PathVariable Long id) {
        return postService.getPostById(id);
    }
    @PostMapping
    public Post createPost(@RequestBody Post post) {
        return postService.createPost(post);
    }
    @PutMapping("/{id}")
    public Post updatePost(@PathVariable Long id, @RequestBody Post post) {
        return postService.updatePost(id, post);
    }
    @DeleteMapping("/{id}")
    public void deletePost(@PathVariable Long id) {
        postService.deletePost(id);
    }
}

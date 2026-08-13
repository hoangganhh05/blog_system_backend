package com.example.blogsystem.controller;


import com.example.blogsystem.entity.Post;
import com.example.blogsystem.service.PostService;
import com.example.blogsystem.config.CurrentUser;
import com.example.blogsystem.repository.PostRepository;
import com.example.blogsystem.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/posts")
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

    @PostMapping("/{id}/view")
    public Post incrementView(@PathVariable Long id) {
        return postService.incrementViewCount(id);
    }
    @PostMapping
    public Post createPost(@RequestBody Post post) {
        post.setUser(userRepository.getReferenceById(currentUser.id()));
        return postService.createPost(post);
    }
    @PutMapping("/{id}")
    public Post updatePost(@PathVariable Long id, @RequestBody Post post) {
        currentUser.requireOwnerOrAdmin(postRepository.findById(id).orElseThrow().getUser().getId());
        return postService.updatePost(id, post);
    }
    @DeleteMapping("/{id}")
    public void deletePost(@PathVariable Long id) {
        currentUser.requireOwnerOrAdmin(postRepository.findById(id).orElseThrow().getUser().getId());
        postService.deletePost(id);
    }
}

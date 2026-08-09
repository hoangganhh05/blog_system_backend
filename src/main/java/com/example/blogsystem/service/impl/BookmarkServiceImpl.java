package com.example.blogsystem.service.impl;

import com.example.blogsystem.entity.Bookmark;
import com.example.blogsystem.entity.Post;
import com.example.blogsystem.entity.User;
import com.example.blogsystem.repository.BookmarkRepository;
import com.example.blogsystem.repository.PostRepository;
import com.example.blogsystem.repository.UserRepository;
import com.example.blogsystem.service.BookmarkService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class BookmarkServiceImpl implements BookmarkService {

    private final BookmarkRepository bookmarkRepository;
    private final UserRepository userRepository;
    private final PostRepository postRepository;

    public BookmarkServiceImpl(BookmarkRepository bookmarkRepository, UserRepository userRepository, PostRepository postRepository) {
        this.bookmarkRepository = bookmarkRepository;
        this.userRepository = userRepository;
        this.postRepository = postRepository;
    }

    @Override
    public boolean toggleBookmark(Long userId, Long postId) {
        Optional<Bookmark> existing = bookmarkRepository.findByUserIdAndPostId(userId, postId);
        if (existing.isPresent()) {
            bookmarkRepository.delete(existing.get());
            return false; // Removed bookmark
        } else {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            Post post = postRepository.findById(postId)
                    .orElseThrow(() -> new RuntimeException("Post not found"));

            Bookmark bookmark = new Bookmark();
            bookmark.setUser(user);
            bookmark.setPost(post);
            bookmark.setCreatedAt(LocalDateTime.now());
            bookmarkRepository.save(bookmark);
            return true; // Bookmarked
        }
    }

    @Override
    public boolean isBookmarkedByUser(Long userId, Long postId) {
        if (userId == null) return false;
        return bookmarkRepository.existsByUserIdAndPostId(userId, postId);
    }

    @Override
    public List<Bookmark> getUserBookmarks(Long userId) {
        return bookmarkRepository.findByUserId(userId);
    }
}

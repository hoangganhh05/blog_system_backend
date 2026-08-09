package com.example.blogsystem.service;

import com.example.blogsystem.entity.Bookmark;
import java.util.List;

public interface BookmarkService {
    boolean toggleBookmark(Long userId, Long postId);
    boolean isBookmarkedByUser(Long userId, Long postId);
    List<Bookmark> getUserBookmarks(Long userId);
}

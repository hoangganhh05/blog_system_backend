package com.example.blogsystem.service;

import com.example.blogsystem.entity.Story;
import com.example.blogsystem.entity.StoryView;

import java.util.List;

public interface StoryService {
    Story createStory(Long userId, String mediaUrl, String textContent, String bgColor);
    List<Story> getActiveStories();
    List<Story> getUserArchivedStories(Long userId);
    void deleteStory(Long storyId);
    void recordView(Long storyId, Long userId);
    void reactToStory(Long storyId, Long userId, String reaction);
    List<StoryView> getStoryViews(Long storyId);
}

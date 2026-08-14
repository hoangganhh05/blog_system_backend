package com.example.blogsystem.service.impl;

import com.example.blogsystem.entity.Story;
import com.example.blogsystem.entity.User;
import com.example.blogsystem.repository.StoryRepository;
import com.example.blogsystem.repository.UserRepository;
import com.example.blogsystem.service.StoryService;
import com.example.blogsystem.entity.StoryView;
import com.example.blogsystem.repository.StoryViewRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class StoryServiceImpl implements StoryService {

    private final StoryRepository storyRepository;
    private final UserRepository userRepository;
    private final StoryViewRepository storyViewRepository;

    public StoryServiceImpl(StoryRepository storyRepository, UserRepository userRepository, StoryViewRepository storyViewRepository) {
        this.storyRepository = storyRepository;
        this.userRepository = userRepository;
        this.storyViewRepository = storyViewRepository;
    }

    @Override
    @Transactional
    public Story createStory(Long userId, String mediaUrl, String textContent, String bgColor) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng!"));

        LocalDateTime now = LocalDateTime.now();
        Story story = new Story();
        story.setUser(user);
        story.setMediaUrl(mediaUrl);
        story.setTextContent(textContent);
        story.setBgColor(bgColor);
        story.setCreatedAt(now);
        story.setExpiresAt(now.plusHours(24));
        story.setIsArchived(false);

        return storyRepository.save(story);
    }

    @Override
    public List<Story> getActiveStories() {
        LocalDateTime timeLimit = LocalDateTime.now().minusHours(24);
        return storyRepository.findActiveStories(timeLimit);
    }

    @Override
    public List<Story> getUserArchivedStories(Long userId) {
        LocalDateTime timeLimit = LocalDateTime.now().minusHours(24);
        return storyRepository.findArchivedStoriesByUserId(userId, timeLimit);
    }

    @Override
    @Transactional
    public void deleteStory(Long storyId) {
        if (!storyRepository.existsById(storyId)) {
            throw new RuntimeException("Không tìm thấy Story!");
        }
        storyRepository.deleteById(storyId);
    }

    @Override
    @Transactional
    public void recordView(Long storyId, Long userId) {
        Story story = storyRepository.findById(storyId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy Story!"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy Người dùng!"));

        if (story.getUser().getId().equals(userId)) {
            return;
        }

        if (!storyViewRepository.existsByStoryIdAndUserId(storyId, userId)) {
            StoryView storyView = new StoryView();
            storyView.setStory(story);
            storyView.setUser(user);
            storyView.setViewedAt(LocalDateTime.now());
            storyViewRepository.save(storyView);
        }
    }

    @Override
    @Transactional
    public void reactToStory(Long storyId, Long userId, String reaction) {
        Story story = storyRepository.findById(storyId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy Story!"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy Người dùng!"));

        StoryView storyView = storyViewRepository.findByStoryIdOrderByViewedAtDesc(storyId).stream()
                .filter(v -> v.getUser().getId().equals(userId))
                .findFirst()
                .orElseGet(() -> {
                    StoryView sv = new StoryView();
                    sv.setStory(story);
                    sv.setUser(user);
                    return sv;
                });

        storyView.setReaction(reaction);
        storyView.setViewedAt(LocalDateTime.now());
        storyViewRepository.save(storyView);
    }

    @Override
    public List<StoryView> getStoryViews(Long storyId) {
        return storyViewRepository.findByStoryIdOrderByViewedAtDesc(storyId);
    }
}

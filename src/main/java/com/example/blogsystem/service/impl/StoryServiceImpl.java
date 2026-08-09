package com.example.blogsystem.service.impl;

import com.example.blogsystem.entity.Story;
import com.example.blogsystem.entity.User;
import com.example.blogsystem.repository.StoryRepository;
import com.example.blogsystem.repository.UserRepository;
import com.example.blogsystem.service.StoryService;
import com.example.blogsystem.entity.StoryView;
import com.example.blogsystem.repository.StoryViewRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

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
    public Story createStory(Long userId, String mediaUrl, String textContent, String bgColor) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng!"));

        Story story = new Story();
        story.setUser(user);
        story.setMediaUrl(mediaUrl);
        story.setTextContent(textContent);
        story.setBgColor(bgColor);
        story.setCreatedAt(LocalDateTime.now());

        return storyRepository.save(story);
    }

    @Override
    public List<Story> getActiveStories() {
        // Lấy thời điểm cách đây 24 tiếng
        LocalDateTime timeLimit = LocalDateTime.now().minusHours(24);
        return storyRepository.findActiveStories(timeLimit);
    }

    @Override
    public void deleteStory(Long storyId) {
        if (!storyRepository.existsById(storyId)) {
            throw new RuntimeException("Không tìm thấy Story!");
        }
        storyRepository.deleteById(storyId);
    }

    @Override
    public void recordView(Long storyId, Long userId) {
        Story story = storyRepository.findById(storyId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy Story!"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy Người dùng!"));

        // Nếu người tự xem tin của chính mình thì không tính
        if (story.getUser().getId().equals(userId)) {
            return;
        }

        // Nếu chưa lưu view thì lưu mới
        if (!storyViewRepository.existsByStoryIdAndUserId(storyId, userId)) {
            StoryView storyView = new StoryView();
            storyView.setStory(story);
            storyView.setUser(user);
            storyView.setViewedAt(LocalDateTime.now());
            storyViewRepository.save(storyView);
        }
    }

    @Override
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

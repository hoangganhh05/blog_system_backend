package com.example.blogsystem.controller;

import com.example.blogsystem.entity.Story;
import com.example.blogsystem.service.StoryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/stories")
public class StoryController {

    private final StoryService storyService;

    public StoryController(StoryService storyService) {
        this.storyService = storyService;
    }

    // Đăng Story mới: POST /stories/create?userId=...
    // Request Body: { "mediaUrl": "...", "textContent": "...", "bgColor": "..." }
    @PostMapping("/create")
    public ResponseEntity<Story> createStory(
            @RequestParam Long userId,
            @RequestBody Map<String, String> payload) {
        String mediaUrl = payload.get("mediaUrl");
        String textContent = payload.get("textContent");
        String bgColor = payload.get("bgColor");

        Story story = storyService.createStory(userId, mediaUrl, textContent, bgColor);
        return ResponseEntity.ok(story);
    }

    // Lấy tất cả Story còn hiệu lực (24h qua): GET /stories/active
    @GetMapping("/active")
    public ResponseEntity<List<Story>> getActiveStories() {
        List<Story> stories = storyService.getActiveStories();
        return ResponseEntity.ok(stories);
    }

    // Xóa Story: DELETE /stories/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteStory(@PathVariable Long id) {
        try {
            storyService.deleteStory(id);
            return ResponseEntity.ok("Xóa Story thành công!");
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(e.getMessage());
        }
    }

    // Ghi nhận lượt xem Story: POST /stories/{id}/view?userId=...
    @PostMapping("/{id}/view")
    public ResponseEntity<Void> recordView(
            @PathVariable Long id,
            @RequestParam Long userId) {
        storyService.recordView(id, userId);
        return ResponseEntity.ok().build();
    }

    // Thả cảm xúc Story: POST /stories/{id}/react?userId=...&reaction=...
    @PostMapping("/{id}/react")
    public ResponseEntity<Void> reactToStory(
            @PathVariable Long id,
            @RequestParam Long userId,
            @RequestParam String reaction) {
        storyService.reactToStory(id, userId, reaction);
        return ResponseEntity.ok().build();
    }

    // Lấy danh sách người xem & cảm xúc Story: GET /stories/{id}/viewers
    @GetMapping("/{id}/viewers")
    public ResponseEntity<List<com.example.blogsystem.entity.StoryView>> getViewers(@PathVariable Long id) {
        List<com.example.blogsystem.entity.StoryView> views = storyService.getStoryViews(id);
        return ResponseEntity.ok(views);
    }
}

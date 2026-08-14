package com.example.blogsystem.controller;

import com.example.blogsystem.dto.DTOMapper;
import com.example.blogsystem.dto.StoryDTO;
import com.example.blogsystem.dto.StoryViewDTO;
import com.example.blogsystem.entity.Story;
import com.example.blogsystem.entity.StoryView;
import com.example.blogsystem.service.StoryService;
import com.example.blogsystem.config.CurrentUser;
import com.example.blogsystem.repository.StoryRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/stories")
public class StoryController {

    private final StoryService storyService;
    private final StoryRepository storyRepository;
    private final CurrentUser currentUser;

    public StoryController(StoryService storyService, StoryRepository storyRepository, CurrentUser currentUser) {
        this.storyService = storyService;
        this.storyRepository = storyRepository;
        this.currentUser = currentUser;
    }

    // Đăng Story mới: POST /stories/create
    @PostMapping("/create")
    public ResponseEntity<StoryDTO> createStory(
            @RequestBody Map<String, String> payload) {
        String mediaUrl = payload.get("mediaUrl");
        String textContent = payload.get("textContent");
        String bgColor = payload.get("bgColor");

        Story story = storyService.createStory(currentUser.id(), mediaUrl, textContent, bgColor);
        return ResponseEntity.ok(DTOMapper.toStoryDTO(story));
    }

    // Lấy tất cả Story còn hiệu lực (24h qua): GET /stories/active
    @GetMapping("/active")
    public ResponseEntity<List<StoryDTO>> getActiveStories() {
        List<Story> stories = storyService.getActiveStories();
        List<StoryDTO> dtos = stories.stream()
                .map(DTOMapper::toStoryDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    // Xóa Story: DELETE /stories/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteStory(@PathVariable Long id) {
        try {
            currentUser.requireOwnerOrAdmin(storyRepository.findById(id).orElseThrow().getUser().getId());
            storyService.deleteStory(id);
            return ResponseEntity.ok("Xóa Story thành công!");
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(e.getMessage());
        }
    }

    // Ghi nhận lượt xem Story: POST /stories/{id}/view
    @PostMapping("/{id}/view")
    public ResponseEntity<Void> recordView(
            @PathVariable Long id) {
        storyService.recordView(id, currentUser.id());
        return ResponseEntity.ok().build();
    }

    // Thả cảm xúc Story: POST /stories/{id}/react?reaction=...
    @PostMapping("/{id}/react")
    public ResponseEntity<Void> reactToStory(
            @PathVariable Long id,
            @RequestParam String reaction) {
        storyService.reactToStory(id, currentUser.id(), reaction);
        return ResponseEntity.ok().build();
    }

    // Lấy danh sách người xem & cảm xúc Story: GET /stories/{id}/viewers
    @GetMapping("/{id}/viewers")
    public ResponseEntity<List<StoryViewDTO>> getViewers(@PathVariable Long id) {
        currentUser.requireOwnerOrAdmin(storyRepository.findById(id).orElseThrow().getUser().getId());
        List<StoryView> views = storyService.getStoryViews(id);
        List<StoryViewDTO> dtos = views.stream()
                .map(DTOMapper::toStoryViewDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }
}

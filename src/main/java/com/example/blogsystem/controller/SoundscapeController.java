package com.example.blogsystem.controller;

import com.example.blogsystem.config.CurrentUser;
import com.example.blogsystem.dto.SoundscapeDTO;
import com.example.blogsystem.entity.Soundscape;
import com.example.blogsystem.service.SoundscapeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping({"/soundscapes", "/api/soundscapes", "/v1/soundscapes", "/api/v1/soundscapes"})
@RequiredArgsConstructor
@Slf4j
public class SoundscapeController {

    private final SoundscapeService soundscapeService;
    private final CurrentUser currentUser;

    @GetMapping
    public ResponseEntity<Page<SoundscapeDTO>> getSoundscapes(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String location,
            Pageable pageable) {
        return ResponseEntity.ok(soundscapeService.getSoundscapes(category, location, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<SoundscapeDTO> getSoundscapeById(@PathVariable Long id) {
        return ResponseEntity.ok(soundscapeService.getById(id));
    }

    @PostMapping
    public ResponseEntity<SoundscapeDTO> createSoundscape(@RequestBody Soundscape soundscape) {
        Long userId = null;
        try { userId = currentUser.id(); } catch (Exception ignored) {}
        SoundscapeDTO created = soundscapeService.createSoundscape(soundscape, userId);
        return ResponseEntity.ok(created);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteSoundscape(@PathVariable Long id) {
        Long userId = currentUser.id();
        soundscapeService.deleteSoundscape(id, userId);
        return ResponseEntity.ok(Map.of("success", true, "message", "Đã xóa âm thanh thành công", "deletedId", id));
    }

    @PostMapping("/{id}/like")
    public ResponseEntity<SoundscapeDTO> toggleLike(@PathVariable Long id) {
        return ResponseEntity.ok(soundscapeService.toggleLike(id));
    }

    @PostMapping("/{id}/play")
    public ResponseEntity<SoundscapeDTO> incrementPlay(@PathVariable Long id) {
        return ResponseEntity.ok(soundscapeService.incrementPlayCount(id));
    }
}

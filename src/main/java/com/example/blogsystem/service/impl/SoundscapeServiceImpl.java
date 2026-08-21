package com.example.blogsystem.service.impl;

import com.example.blogsystem.dto.SoundscapeDTO;
import com.example.blogsystem.entity.Soundscape;
import com.example.blogsystem.entity.User;
import com.example.blogsystem.repository.SoundscapeRepository;
import com.example.blogsystem.repository.UserRepository;
import com.example.blogsystem.service.SoundscapeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class SoundscapeServiceImpl implements SoundscapeService {

    private final SoundscapeRepository soundscapeRepository;
    private final UserRepository userRepository;

    @Override
    public Page<SoundscapeDTO> getSoundscapes(String category, String location, Pageable pageable) {
        Page<Soundscape> pageResult;
        if ((category != null && !category.isBlank()) || (location != null && !location.isBlank())) {
            pageResult = soundscapeRepository.searchSoundscapes(
                    category != null && !category.equalsIgnoreCase("ALL") ? category : null,
                    location,
                    pageable
            );
        } else {
            pageResult = soundscapeRepository.findByIsActiveTrueOrderByCreatedAtDesc(pageable);
        }
        return pageResult.map(this::toDTO);
    }

    @Override
    public SoundscapeDTO getById(Long id) {
        Soundscape soundscape = soundscapeRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy đoạn âm thanh ID: " + id));
        return toDTO(soundscape);
    }

    @Override
    @Transactional
    public SoundscapeDTO createSoundscape(Soundscape soundscape, Long currentUserId) {
        if (soundscape.getTitle() == null || soundscape.getTitle().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Tiêu đề không gian âm thanh không được để trống");
        }
        if (soundscape.getAudioUrl() == null || soundscape.getAudioUrl().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Đường dẫn tệp âm thanh không được để trống");
        }

        if (soundscape.getCategory() == null || soundscape.getCategory().isBlank()) {
            soundscape.setCategory("NATURE");
        }

        if (currentUserId != null) {
            soundscape.setUserId(currentUserId);
            userRepository.findById(currentUserId).ifPresent(u -> {
                if (soundscape.getCreatorName() == null || soundscape.getCreatorName().isBlank()) {
                    soundscape.setCreatorName(u.getFullName() != null && !u.getFullName().isBlank() ? u.getFullName() : u.getUsername());
                }
            });
        }

        if (soundscape.getCreatorName() == null || soundscape.getCreatorName().isBlank()) {
            soundscape.setCreatorName("Thu âm thực địa");
        }

        soundscape.setIsActive(true);
        soundscape.setLikesCount(0L);
        soundscape.setPlaysCount(0L);
        soundscape.setCreatedAt(LocalDateTime.now());

        Soundscape saved = soundscapeRepository.save(soundscape);
        log.info("🌿 Đã tạo đoạn âm thanh môi trường mới: '{}' (ID: {})", saved.getTitle(), saved.getId());
        return toDTO(saved);
    }

    @Override
    @Transactional
    public void deleteSoundscape(Long id, Long currentUserId) {
        Soundscape soundscape = soundscapeRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy đoạn âm thanh ID: " + id));

        if (soundscape.getUserId() != null && currentUserId != null && !soundscape.getUserId().equals(currentUserId)) {
            User user = userRepository.findById(currentUserId).orElse(null);
            boolean isAdmin = user != null && "ROLE_ADMIN".equalsIgnoreCase(user.getRole());
            if (!isAdmin) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Bạn không có quyền xóa âm thanh này");
            }
        }

        soundscapeRepository.delete(soundscape);
        log.info("🗑️ Đã xóa đoạn âm thanh ID: {}", id);
    }

    @Override
    @Transactional
    public SoundscapeDTO toggleLike(Long id) {
        Soundscape soundscape = soundscapeRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy đoạn âm thanh ID: " + id));
        long currentLikes = soundscape.getLikesCount() != null ? soundscape.getLikesCount() : 0L;
        soundscape.setLikesCount(currentLikes + 1L);
        return toDTO(soundscapeRepository.save(soundscape));
    }

    @Override
    @Transactional
    public SoundscapeDTO incrementPlayCount(Long id) {
        Soundscape soundscape = soundscapeRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy đoạn âm thanh ID: " + id));
        long plays = soundscape.getPlaysCount() != null ? soundscape.getPlaysCount() : 0L;
        soundscape.setPlaysCount(plays + 1L);
        return toDTO(soundscapeRepository.save(soundscape));
    }

    private SoundscapeDTO toDTO(Soundscape s) {
        if (s == null) return null;
        return SoundscapeDTO.builder()
                .id(s.getId())
                .title(s.getTitle())
                .location(s.getLocation())
                .category(s.getCategory())
                .audioUrl(s.getAudioUrl())
                .imageUrl(s.getImageUrl())
                .description(s.getDescription())
                .creatorName(s.getCreatorName())
                .userId(s.getUserId())
                .likesCount(s.getLikesCount() != null ? s.getLikesCount() : 0L)
                .playsCount(s.getPlaysCount() != null ? s.getPlaysCount() : 0L)
                .durationSeconds(s.getDurationSeconds())
                .isActive(s.getIsActive())
                .createdAt(s.getCreatedAt())
                .build();
    }
}

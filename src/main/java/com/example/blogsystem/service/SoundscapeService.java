package com.example.blogsystem.service;

import com.example.blogsystem.dto.SoundscapeDTO;
import com.example.blogsystem.entity.Soundscape;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface SoundscapeService {
    Page<SoundscapeDTO> getSoundscapes(String category, String location, Pageable pageable);
    SoundscapeDTO getById(Long id);
    SoundscapeDTO createSoundscape(Soundscape soundscape, Long currentUserId);
    void deleteSoundscape(Long id, Long currentUserId);
    SoundscapeDTO toggleLike(Long id);
    SoundscapeDTO incrementPlayCount(Long id);
}

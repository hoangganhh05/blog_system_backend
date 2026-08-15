package com.example.blogsystem.service;

import com.example.blogsystem.dto.SongDTO;

import java.util.List;

public interface SongService {
    List<SongDTO> getAllActiveSongs();
    List<SongDTO> searchSongs(String query);
}

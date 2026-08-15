package com.example.blogsystem.service;

import com.example.blogsystem.dto.SongDTO;
import org.springframework.data.domain.Page;

import java.util.List;

public interface SongService {
    List<SongDTO> getAllActiveSongs();

    Page<SongDTO> getSongsPaged(int page, int size);

    List<SongDTO> searchSongs(String query);

    Page<SongDTO> searchSongsPaged(String query, int page, int size);

    SongDTO createSong(SongDTO songDTO);

    List<SongDTO> bulkImportSongs(List<SongDTO> songDTOList);

    SongDTO updateSong(Long id, SongDTO songDTO);

    void deleteSong(Long id);

    int syncTrendingCharts();
}

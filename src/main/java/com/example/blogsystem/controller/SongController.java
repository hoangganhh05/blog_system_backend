package com.example.blogsystem.controller;

import com.example.blogsystem.dto.SongDTO;
import com.example.blogsystem.service.SongService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping({"/songs", "/api/songs", "/v1/songs", "/api/v1/songs"})
@RequiredArgsConstructor
public class SongController {

    private final SongService songService;

    // Lấy toàn bộ bài hát
    @GetMapping
    public ResponseEntity<List<SongDTO>> getAllSongs() {
        return ResponseEntity.ok(songService.getAllActiveSongs());
    }

    // Lấy danh sách bài hát có phân trang (Hỗ trợ 1.000+ bài hát mượt mà)
    @GetMapping("/paged")
    public ResponseEntity<Page<SongDTO>> getSongsPaged(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size
    ) {
        return ResponseEntity.ok(songService.getSongsPaged(page, size));
    }

    // Tìm kiếm bài hát
    @GetMapping("/search")
    public ResponseEntity<List<SongDTO>> searchSongs(
            @RequestParam(name = "query", required = false, defaultValue = "") String query
    ) {
        return ResponseEntity.ok(songService.searchSongs(query));
    }

    // Tìm kiếm bài hát có phân trang
    @GetMapping("/search/paged")
    public ResponseEntity<Page<SongDTO>> searchSongsPaged(
            @RequestParam(name = "query", required = false, defaultValue = "") String query,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size
    ) {
        return ResponseEntity.ok(songService.searchSongsPaged(query, page, size));
    }

    // Thêm 1 bài hát mới vào Database
    @PostMapping
    public ResponseEntity<SongDTO> createSong(@RequestBody SongDTO songDTO) {
        SongDTO created = songService.createSong(songDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    // Nhập hàng loạt (Bulk Import) nhiều bài hát vào Database
    @PostMapping("/bulk")
    public ResponseEntity<List<SongDTO>> bulkImportSongs(@RequestBody List<SongDTO> songDTOList) {
        List<SongDTO> imported = songService.bulkImportSongs(songDTOList);
        return ResponseEntity.status(HttpStatus.CREATED).body(imported);
    }

    // Đồng bộ tức thì kho nhạc Hot Trend vào Database
    @PostMapping("/sync-trending")
    public ResponseEntity<Map<String, Object>> syncTrending() {
        int added = songService.syncTrendingCharts();
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Đã đồng bộ kho nhạc Hot Trend thành công!",
                "newAddedCount", added
        ));
    }

    // Cập nhật thông tin bài hát
    @PutMapping("/{id}")
    public ResponseEntity<SongDTO> updateSong(@PathVariable Long id, @RequestBody SongDTO songDTO) {
        return ResponseEntity.ok(songService.updateSong(id, songDTO));
    }

    // Xóa bài hát khỏi Database
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSong(@PathVariable Long id) {
        songService.deleteSong(id);
        return ResponseEntity.noContent().build();
    }
}

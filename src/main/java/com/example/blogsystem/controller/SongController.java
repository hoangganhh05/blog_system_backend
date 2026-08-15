package com.example.blogsystem.controller;

import com.example.blogsystem.dto.SongDTO;
import com.example.blogsystem.service.SongService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping({"/songs", "/api/songs", "/v1/songs", "/api/v1/songs"})
@RequiredArgsConstructor
public class SongController {

    private final SongService songService;

    @GetMapping
    public ResponseEntity<List<SongDTO>> getAllSongs() {
        return ResponseEntity.ok(songService.getAllActiveSongs());
    }

    @GetMapping("/search")
    public ResponseEntity<List<SongDTO>> searchSongs(@RequestParam(name = "query", required = false, defaultValue = "") String query) {
        return ResponseEntity.ok(songService.searchSongs(query));
    }
}

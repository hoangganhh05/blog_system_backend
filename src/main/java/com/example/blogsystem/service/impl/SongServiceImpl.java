package com.example.blogsystem.service.impl;

import com.example.blogsystem.dto.SongDTO;
import com.example.blogsystem.entity.Song;
import com.example.blogsystem.repository.SongRepository;
import com.example.blogsystem.service.SongService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SongServiceImpl implements SongService {

    private final SongRepository songRepository;

    @PostConstruct
    public void initDefaultSongs() {
        log.info("[SONG INIT] Đang kiểm tra và đồng bộ tự động kho nhạc thịnh hành...");
        syncTrendingCharts();
    }

    /**
     * Tác vụ nền Cron Job chạy tự động lúc 04:00 sáng mỗi ngày
     * Tự động quét và bổ sung các bài hát Hot Trend mới vào Database
     */
    @Scheduled(cron = "0 0 4 * * ?")
    public void scheduledDailyTrendingSync() {
        log.info("[CRON AUTO-SYNC] Bắt đầu tác vụ đồng bộ tự động kho nhạc Hot Trend theo lịch...");
        int added = syncTrendingCharts();
        log.info("[CRON AUTO-SYNC] Hoàn tất đồng bộ: Đã cập nhật/bổ sung {} bài hát mới vào Database.", added);
    }

    /**
     * Tự động đồng bộ và nạp kho nhạc Hot Trend phong phú vào Database
     */
    @Override
    public int syncTrendingCharts() {
        List<Song> trendingCatalog = Arrays.asList(
                // 1. Vinahouse & Remix Club Nonstop
                Song.builder()
                        .title("Vinahouse Club Night & Bass Boosted")
                        .artist("DJ Live Mix Nonstop")
                        .genre("Vinahouse")
                        .genreColor("bg-rose-100 text-rose-700 dark:bg-rose-950/40 dark:text-rose-300")
                        .coverUrl("https://images.unsplash.com/photo-1516450360452-9312f5e86fc7?w=600&auto=format&fit=crop&q=80")
                        .audioUrl("https://streams.ilovemusic.de/iloveradio2.mp3")
                        .fallbackAudioUrl("https://streams.ilovemusic.de/iloveradio9.mp3")
                        .durationSeconds(0)
                        .build(),

                Song.builder()
                        .title("Cắt Đôi Nỗi Sầu (Vinahouse Remix)")
                        .artist("Tăng Duy Tân")
                        .genre("Vinahouse")
                        .genreColor("bg-rose-100 text-rose-700 dark:bg-rose-950/40 dark:text-rose-300")
                        .coverUrl("https://images.unsplash.com/photo-1470225620780-dba8ba36b745?w=600&auto=format&fit=crop&q=80")
                        .audioUrl("https://streams.ilovemusic.de/iloveradio2.mp3")
                        .fallbackAudioUrl("https://streams.ilovemusic.de/iloveradio9.mp3")
                        .durationSeconds(0)
                        .build(),

                Song.builder()
                        .title("See Tình (Hoàng Thùy Linh Remix)")
                        .artist("Hoàng Thùy Linh")
                        .genre("Vinahouse")
                        .genreColor("bg-rose-100 text-rose-700 dark:bg-rose-950/40 dark:text-rose-300")
                        .coverUrl("https://images.unsplash.com/photo-1514525253161-7a46d19cd819?w=600&auto=format&fit=crop&q=80")
                        .audioUrl("https://streams.ilovemusic.de/iloveradio2.mp3")
                        .fallbackAudioUrl("https://streams.ilovemusic.de/iloveradio9.mp3")
                        .durationSeconds(0)
                        .build(),

                Song.builder()
                        .title("EDM Festival & Nonstop Dance Party")
                        .artist("Ultra Music Live")
                        .genre("Vinahouse")
                        .genreColor("bg-purple-100 text-purple-700 dark:bg-purple-950/40 dark:text-purple-300")
                        .coverUrl("https://images.unsplash.com/photo-1508700115892-45ecd05ae2ad?w=600&auto=format&fit=crop&q=80")
                        .audioUrl("https://streams.ilovemusic.de/iloveradio9.mp3")
                        .fallbackAudioUrl("https://streams.ilovemusic.de/iloveradio2.mp3")
                        .durationSeconds(0)
                        .build(),

                // 2. Lo-Fi Chill & Focus
                Song.builder()
                        .title("Lo-Fi Study & Chill Beats")
                        .artist("BlogViet Lo-Fi Station")
                        .genre("Lofi Chill")
                        .genreColor("bg-emerald-100 text-emerald-700 dark:bg-emerald-950/40 dark:text-emerald-300")
                        .coverUrl("https://images.unsplash.com/photo-1518609878373-06d740f60d8b?w=600&auto=format&fit=crop&q=80")
                        .audioUrl("https://streams.ilovemusic.de/iloveradio10.mp3")
                        .fallbackAudioUrl("https://stream.zeno.fm/f3wvbbqmdg8uv")
                        .durationSeconds(0)
                        .build(),

                Song.builder()
                        .title("Acoustic Guitar & Coffee Melody")
                        .artist("Acoustic Melody Session")
                        .genre("Lofi Chill")
                        .genreColor("bg-emerald-100 text-emerald-700 dark:bg-emerald-950/40 dark:text-emerald-300")
                        .coverUrl("https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?w=600&auto=format&fit=crop&q=80")
                        .audioUrl("https://streams.ilovemusic.de/iloveradio10.mp3")
                        .fallbackAudioUrl("https://streams.ilovemusic.de/iloveradio1.mp3")
                        .durationSeconds(0)
                        .build(),

                Song.builder()
                        .title("Late Night Lofi Beats & Deep Focus")
                        .artist("Developer Chill Beats")
                        .genre("Lofi Chill")
                        .genreColor("bg-teal-100 text-teal-700 dark:bg-teal-950/40 dark:text-teal-300")
                        .coverUrl("https://images.unsplash.com/photo-1526374965328-7f61d4dc18c5?w=600&auto=format&fit=crop&q=80")
                        .audioUrl("https://stream.zeno.fm/f3wvbbqmdg8uv")
                        .fallbackAudioUrl("https://streams.ilovemusic.de/iloveradio10.mp3")
                        .durationSeconds(0)
                        .build(),

                Song.builder()
                        .title("Gaming Beats & Chillhop Level Up")
                        .artist("Pixel Wave Records")
                        .genre("Lofi Chill")
                        .genreColor("bg-cyan-100 text-cyan-700 dark:bg-cyan-950/40 dark:text-cyan-300")
                        .coverUrl("https://images.unsplash.com/photo-1550745165-9bc0b252726f?w=600&auto=format&fit=crop&q=80")
                        .audioUrl("https://streams.ilovemusic.de/iloveradio10.mp3")
                        .fallbackAudioUrl("https://streams.ilovemusic.de/iloveradio9.mp3")
                        .durationSeconds(0)
                        .build(),

                // 3. Top V-Pop & Rap Việt
                Song.builder()
                        .title("Nơi Này Có Anh")
                        .artist("Sơn Tùng M-TP")
                        .genre("V-Pop")
                        .genreColor("bg-amber-100 text-amber-700 dark:bg-amber-950/40 dark:text-amber-300")
                        .coverUrl("https://images.unsplash.com/photo-1514525253161-7a46d19cd819?w=600&auto=format&fit=crop&q=80")
                        .audioUrl("https://streams.ilovemusic.de/iloveradio1.mp3")
                        .fallbackAudioUrl("https://streams.ilovemusic.de/iloveradio2.mp3")
                        .durationSeconds(0)
                        .build(),

                Song.builder()
                        .title("Nàng Thơ")
                        .artist("Hoàng Dũng")
                        .genre("V-Pop")
                        .genreColor("bg-pink-100 text-pink-700 dark:bg-pink-950/40 dark:text-pink-300")
                        .coverUrl("https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?w=600&auto=format&fit=crop&q=80")
                        .audioUrl("https://streams.ilovemusic.de/iloveradio10.mp3")
                        .fallbackAudioUrl("https://stream.zeno.fm/f3wvbbqmdg8uv")
                        .durationSeconds(0)
                        .build(),

                Song.builder()
                        .title("Ngày Đầu Tiên")
                        .artist("Đức Phúc")
                        .genre("V-Pop")
                        .genreColor("bg-purple-100 text-purple-700 dark:bg-purple-950/40 dark:text-purple-300")
                        .coverUrl("https://images.unsplash.com/photo-1493225457124-a3eb161ffa5f?w=600&auto=format&fit=crop&q=80")
                        .audioUrl("https://streams.ilovemusic.de/iloveradio1.mp3")
                        .fallbackAudioUrl("https://stream.zeno.fm/f3wvbbqmdg8uv")
                        .durationSeconds(0)
                        .build(),

                Song.builder()
                        .title("Chìm Sâu")
                        .artist("RPT MCK ft. Trung Trần")
                        .genre("V-Pop")
                        .genreColor("bg-indigo-100 text-indigo-700 dark:bg-indigo-950/40 dark:text-indigo-300")
                        .coverUrl("https://images.unsplash.com/photo-1508700115892-45ecd05ae2ad?w=600&auto=format&fit=crop&q=80")
                        .audioUrl("https://streams.ilovemusic.de/iloveradio9.mp3")
                        .fallbackAudioUrl("https://streams.ilovemusic.de/iloveradio2.mp3")
                        .durationSeconds(0)
                        .build()
        );

        int newAddedCount = 0;
        List<Song> toInsert = new ArrayList<>();

        for (Song candidate : trendingCatalog) {
            boolean exists = songRepository.existsByTitleIgnoreCaseAndArtistIgnoreCase(
                    candidate.getTitle(), candidate.getArtist()
            );
            if (!exists) {
                toInsert.add(candidate);
                newAddedCount++;
            }
        }

        if (!toInsert.isEmpty()) {
            songRepository.saveAll(toInsert);
            log.info("[MUSIC SYNC] Đã tự động nạp {} bài hát thịnh hành mới vào Database.", toInsert.size());
        }

        return newAddedCount;
    }

    @Override
    public List<SongDTO> getAllActiveSongs() {
        return songRepository.findByIsActiveTrueOrderByCreatedAtAsc()
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public Page<SongDTO> getSongsPaged(int page, int size) {
        Pageable pageable = PageRequest.of(Math.max(0, page), Math.max(1, size));
        return songRepository.findByIsActiveTrueOrderByCreatedAtDesc(pageable)
                .map(this::toDTO);
    }

    @Override
    public List<SongDTO> searchSongs(String query) {
        if (query == null || query.trim().isEmpty()) {
            return getAllActiveSongs();
        }
        String q = query.trim();
        return songRepository.findByTitleContainingIgnoreCaseOrArtistContainingIgnoreCaseOrGenreContainingIgnoreCase(q, q, q)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public Page<SongDTO> searchSongsPaged(String query, int page, int size) {
        Pageable pageable = PageRequest.of(Math.max(0, page), Math.max(1, size));
        if (query == null || query.trim().isEmpty()) {
            return songRepository.findByIsActiveTrueOrderByCreatedAtDesc(pageable).map(this::toDTO);
        }
        String q = query.trim();
        return songRepository.findByTitleContainingIgnoreCaseOrArtistContainingIgnoreCaseOrGenreContainingIgnoreCase(q, q, q, pageable)
                .map(this::toDTO);
    }

    @Override
    public SongDTO createSong(SongDTO dto) {
        Song song = Song.builder()
                .title(dto.getTitle())
                .artist(dto.getArtist())
                .genre(dto.getGenre() != null ? dto.getGenre() : "V-Pop")
                .genreColor(dto.getGenreColor())
                .coverUrl(dto.getCover() != null ? dto.getCover() : dto.getCoverUrl())
                .audioUrl(dto.getSrc() != null ? dto.getSrc() : dto.getAudioUrl())
                .fallbackAudioUrl(dto.getFallbackSrc() != null ? dto.getFallbackSrc() : dto.getFallbackAudioUrl())
                .durationSeconds(dto.getDurationSeconds() != null ? dto.getDurationSeconds() : 0)
                .isActive(true)
                .build();

        Song saved = songRepository.save(song);
        return toDTO(saved);
    }

    @Override
    public List<SongDTO> bulkImportSongs(List<SongDTO> songDTOList) {
        if (songDTOList == null || songDTOList.isEmpty()) {
            return List.of();
        }

        List<Song> entities = songDTOList.stream()
                .map(dto -> Song.builder()
                        .title(dto.getTitle())
                        .artist(dto.getArtist())
                        .genre(dto.getGenre() != null ? dto.getGenre() : "V-Pop")
                        .genreColor(dto.getGenreColor())
                        .coverUrl(dto.getCover() != null ? dto.getCover() : dto.getCoverUrl())
                        .audioUrl(dto.getSrc() != null ? dto.getSrc() : dto.getAudioUrl())
                        .fallbackAudioUrl(dto.getFallbackSrc() != null ? dto.getFallbackSrc() : dto.getFallbackAudioUrl())
                        .durationSeconds(dto.getDurationSeconds() != null ? dto.getDurationSeconds() : 0)
                        .isActive(true)
                        .build())
                .collect(Collectors.toList());

        List<Song> savedList = songRepository.saveAll(entities);
        return savedList.stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Override
    public SongDTO updateSong(Long id, SongDTO dto) {
        Song song = songRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy bài hát ID: " + id));

        if (dto.getTitle() != null) song.setTitle(dto.getTitle());
        if (dto.getArtist() != null) song.setArtist(dto.getArtist());
        if (dto.getGenre() != null) song.setGenre(dto.getGenre());
        if (dto.getCover() != null) song.setCoverUrl(dto.getCover());
        if (dto.getCoverUrl() != null) song.setCoverUrl(dto.getCoverUrl());
        if (dto.getSrc() != null) song.setAudioUrl(dto.getSrc());
        if (dto.getAudioUrl() != null) song.setAudioUrl(dto.getAudioUrl());
        if (dto.getFallbackSrc() != null) song.setFallbackAudioUrl(dto.getFallbackSrc());
        if (dto.getFallbackAudioUrl() != null) song.setFallbackAudioUrl(dto.getFallbackAudioUrl());

        Song updated = songRepository.save(song);
        return toDTO(updated);
    }

    @Override
    public void deleteSong(Long id) {
        songRepository.deleteById(id);
    }

    private SongDTO toDTO(Song song) {
        if (song == null) return null;
        return SongDTO.builder()
                .id(song.getId())
                .title(song.getTitle())
                .artist(song.getArtist())
                .genre(song.getGenre())
                .genreColor(song.getGenreColor())
                .cover(song.getCoverUrl())
                .coverUrl(song.getCoverUrl())
                .src(song.getAudioUrl())
                .audioUrl(song.getAudioUrl())
                .fallbackSrc(song.getFallbackAudioUrl())
                .fallbackAudioUrl(song.getFallbackAudioUrl())
                .durationSeconds(song.getDurationSeconds())
                .isActive(song.getIsActive())
                .createdAt(song.getCreatedAt())
                .build();
    }
}

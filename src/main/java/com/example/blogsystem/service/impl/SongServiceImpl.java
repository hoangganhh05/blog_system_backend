package com.example.blogsystem.service.impl;

import com.example.blogsystem.dto.SongDTO;
import com.example.blogsystem.entity.Song;
import com.example.blogsystem.repository.SongRepository;
import com.example.blogsystem.service.SongService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

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
        // Cập nhật lại kho nhạc chuẩn hóa để sửa triệt để lỗi lệch link và bổ sung kho nhạc đa dạng
        log.info("[SONG INIT] Đang chuẩn hóa kho nhạc trực tuyến cho BlogViet...");
        songRepository.deleteAll();

        List<Song> defaultSongs = Arrays.asList(
                // 1. Lo-Fi Chill Beats
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

                // 2. Vinahouse & EDM Club Night
                Song.builder()
                        .title("Vinahouse & Nonstop Dance Night")
                        .artist("DJ Club Live Mix")
                        .genre("Vinahouse")
                        .genreColor("bg-rose-100 text-rose-700 dark:bg-rose-950/40 dark:text-rose-300")
                        .coverUrl("https://images.unsplash.com/photo-1516450360452-9312f5e86fc7?w=600&auto=format&fit=crop&q=80")
                        .audioUrl("https://streams.ilovemusic.de/iloveradio2.mp3")
                        .fallbackAudioUrl("https://streams.ilovemusic.de/iloveradio9.mp3")
                        .durationSeconds(0)
                        .build(),

                // 3. Acoustic Chill & Pop Ballad
                Song.builder()
                        .title("Acoustic Guitar & Pop Ballad")
                        .artist("Acoustic Melody Session")
                        .genre("Acoustic")
                        .genreColor("bg-pink-100 text-pink-700 dark:bg-pink-950/40 dark:text-pink-300")
                        .coverUrl("https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?w=600&auto=format&fit=crop&q=80")
                        .audioUrl("https://streams.ilovemusic.de/iloveradio10.mp3")
                        .fallbackAudioUrl("https://streams.ilovemusic.de/iloveradio1.mp3")
                        .durationSeconds(0)
                        .build(),

                // 4. Global Top 100 Hits
                Song.builder()
                        .title("Top Hit Charts 2026")
                        .artist("Trending Global Hits")
                        .genre("Pop")
                        .genreColor("bg-amber-100 text-amber-700 dark:bg-amber-950/40 dark:text-amber-300")
                        .coverUrl("https://images.unsplash.com/photo-1514525253161-7a46d19cd819?w=600&auto=format&fit=crop&q=80")
                        .audioUrl("https://streams.ilovemusic.de/iloveradio1.mp3")
                        .fallbackAudioUrl("https://streams.ilovemusic.de/iloveradio2.mp3")
                        .durationSeconds(0)
                        .build(),

                // 5. Piano & Gentle Rain
                Song.builder()
                        .title("Piano & Gentle Raindrops")
                        .artist("Peaceful Night Ambient")
                        .genre("Relaxing")
                        .genreColor("bg-blue-100 text-blue-700 dark:bg-blue-950/40 dark:text-blue-300")
                        .coverUrl("https://images.unsplash.com/photo-1515694346937-94d85e41e6f0?w=600&auto=format&fit=crop&q=80")
                        .audioUrl("https://stream.zeno.fm/f3wvbbqmdg8uv")
                        .fallbackAudioUrl("https://streams.ilovemusic.de/iloveradio10.mp3")
                        .durationSeconds(0)
                        .build(),

                // 6. Cyberpunk & Retro Synthwave
                Song.builder()
                        .title("Cyberpunk 80s Synthwave")
                        .artist("Neon Future Beats")
                        .genre("Synthwave")
                        .genreColor("bg-purple-100 text-purple-700 dark:bg-purple-950/40 dark:text-purple-300")
                        .coverUrl("https://images.unsplash.com/photo-1508700115892-45ecd05ae2ad?w=600&auto=format&fit=crop&q=80")
                        .audioUrl("https://streams.ilovemusic.de/iloveradio9.mp3")
                        .fallbackAudioUrl("https://streams.ilovemusic.de/iloveradio2.mp3")
                        .durationSeconds(0)
                        .build(),

                // 7. Coffee Shop Jazz
                Song.builder()
                        .title("Warm Coffee Shop Jazz")
                        .artist("Midnight Jazz Quartet")
                        .genre("Jazz")
                        .genreColor("bg-orange-100 text-orange-700 dark:bg-orange-950/40 dark:text-orange-300")
                        .coverUrl("https://images.unsplash.com/photo-1501339847302-ac426a4a7cbb?w=600&auto=format&fit=crop&q=80")
                        .audioUrl("https://streams.ilovemusic.de/iloveradio2.mp3")
                        .fallbackAudioUrl("https://streams.ilovemusic.de/iloveradio10.mp3")
                        .durationSeconds(0)
                        .build(),

                // 8. Gaming & Chillhop Beats
                Song.builder()
                        .title("Gaming Beats & Chillhop Level Up")
                        .artist("Pixel Wave Records")
                        .genre("Chillhop")
                        .genreColor("bg-cyan-100 text-cyan-700 dark:bg-cyan-950/40 dark:text-cyan-300")
                        .coverUrl("https://images.unsplash.com/photo-1550745165-9bc0b252726f?w=600&auto=format&fit=crop&q=80")
                        .audioUrl("https://streams.ilovemusic.de/iloveradio10.mp3")
                        .fallbackAudioUrl("https://streams.ilovemusic.de/iloveradio9.mp3")
                        .durationSeconds(0)
                        .build(),

                // 9. Deep House & Sunset Chill
                Song.builder()
                        .title("Deep House Sunset Lounge")
                        .artist("Tropical Sunset Vibes")
                        .genre("Deep House")
                        .genreColor("bg-teal-100 text-teal-700 dark:bg-teal-950/40 dark:text-teal-300")
                        .coverUrl("https://images.unsplash.com/photo-1507525428034-b723cf961d3e?w=600&auto=format&fit=crop&q=80")
                        .audioUrl("https://streams.ilovemusic.de/iloveradio2.mp3")
                        .fallbackAudioUrl("https://streams.ilovemusic.de/iloveradio1.mp3")
                        .durationSeconds(0)
                        .build(),

                // 10. Meditation & Healing Ambient
                Song.builder()
                        .title("Healing Zen & Nature Sounds")
                        .artist("Deep Mindfulness Space")
                        .genre("Zen Ambient")
                        .genreColor("bg-lime-100 text-lime-700 dark:bg-lime-950/40 dark:text-lime-300")
                        .coverUrl("https://images.unsplash.com/photo-1506126613408-eca07ce68773?w=600&auto=format&fit=crop&q=80")
                        .audioUrl("https://stream.zeno.fm/f3wvbbqmdg8uv")
                        .fallbackAudioUrl("https://streams.ilovemusic.de/iloveradio10.mp3")
                        .durationSeconds(0)
                        .build()
        );

        songRepository.saveAll(defaultSongs);
        log.info("[SONG INIT] Đã lưu thành công {} bài hát trực tuyến chuẩn vào Database!", defaultSongs.size());
    }

    @Override
    public List<SongDTO> getAllActiveSongs() {
        return songRepository.findByIsActiveTrueOrderByCreatedAtAsc()
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
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

    private SongDTO toDTO(Song s) {
        return SongDTO.builder()
                .id(s.getId())
                .title(s.getTitle())
                .artist(s.getArtist())
                .genre(s.getGenre())
                .genreColor(s.getGenreColor())
                .cover(s.getCoverUrl())
                .src(s.getAudioUrl())
                .fallbackSrc(s.getFallbackAudioUrl())
                .durationSeconds(s.getDurationSeconds())
                .build();
    }
}

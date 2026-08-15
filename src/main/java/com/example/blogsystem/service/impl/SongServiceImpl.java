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
        if (songRepository.count() == 0) {
            log.info("[SONG INIT] Đang khởi tạo danh sách bài hát mặc định cho BlogViet...");
            List<Song> defaultSongs = Arrays.asList(
                    Song.builder()
                            .title("Vinahouse Đỉnh Nóc 2026")
                            .artist("DJ BlogViet & Phong Max")
                            .genre("Vinahouse")
                            .genreColor("bg-rose-100 text-rose-700 dark:bg-rose-950/40 dark:text-rose-300")
                            .coverUrl("https://images.unsplash.com/photo-1516450360452-9312f5e86fc7?w=300&auto=format&fit=crop&q=80")
                            .audioUrl("https://streams.ilovemusic.de/iloveradio2.mp3")
                            .fallbackAudioUrl("https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3")
                            .build(),
                    Song.builder()
                            .title("Cắt Đôi Nỗi Sầu (Club Remix)")
                            .artist("Tăng Duy Tân (DJ Mix)")
                            .genre("Remix")
                            .genreColor("bg-purple-100 text-purple-700 dark:bg-purple-950/40 dark:text-purple-300")
                            .coverUrl("https://images.unsplash.com/photo-1470225620780-dba8ba36b745?w=300&auto=format&fit=crop&q=80")
                            .audioUrl("https://streams.ilovemusic.de/iloveradio9.mp3")
                            .fallbackAudioUrl("https://www.soundhelix.com/examples/mp3/SoundHelix-Song-3.mp3")
                            .build(),
                    Song.builder()
                            .title("Nàng Thơ (Acoustic Chill)")
                            .artist("Hoàng Dũng")
                            .genre("Pop Ballad")
                            .genreColor("bg-pink-100 text-pink-700 dark:bg-pink-950/40 dark:text-pink-300")
                            .coverUrl("https://images.unsplash.com/photo-1518709268805-4e9042af9f23?w=300&auto=format&fit=crop&q=80")
                            .audioUrl("https://streams.ilovemusic.de/iloveradio10.mp3")
                            .fallbackAudioUrl("https://www.soundhelix.com/examples/mp3/SoundHelix-Song-2.mp3")
                            .build(),
                    Song.builder()
                            .title("See Tình (Dance Pop Hit)")
                            .artist("Hoàng Thùy Linh")
                            .genre("Nhạc Trẻ")
                            .genreColor("bg-amber-100 text-amber-700 dark:bg-amber-950/40 dark:text-amber-300")
                            .coverUrl("https://images.unsplash.com/photo-1514525253161-7a46d19cd819?w=300&auto=format&fit=crop&q=80")
                            .audioUrl("https://streams.ilovemusic.de/iloveradio1.mp3")
                            .fallbackAudioUrl("https://www.soundhelix.com/examples/mp3/SoundHelix-Song-4.mp3")
                            .build(),
                    Song.builder()
                            .title("Bên Trên Tầng Lầu (Lofi Beat)")
                            .artist("Tăng Duy Tân")
                            .genre("Lofi Chill")
                            .genreColor("bg-emerald-100 text-emerald-700 dark:bg-emerald-950/40 dark:text-emerald-300")
                            .coverUrl("https://images.unsplash.com/photo-1518709268805-4e9042af9f23?w=300&auto=format&fit=crop&q=80")
                            .audioUrl("https://streams.ilovemusic.de/iloveradio10.mp3")
                            .fallbackAudioUrl("https://www.soundhelix.com/examples/mp3/SoundHelix-Song-2.mp3")
                            .build(),
                    Song.builder()
                            .title("Nơi Này Có Anh (Piano Rain Lofi)")
                            .artist("Sơn Tùng M-TP")
                            .genre("Lofi Chill")
                            .genreColor("bg-emerald-100 text-emerald-700 dark:bg-emerald-950/40 dark:text-emerald-300")
                            .coverUrl("https://images.unsplash.com/photo-1515694346937-94d85e41e6f0?w=300&auto=format&fit=crop&q=80")
                            .audioUrl("https://stream.zeno.fm/f3wvbbqmdg8uv")
                            .fallbackAudioUrl("https://actions.google.com/sounds/v1/ambiences/rain_heavy.ogg")
                            .build()
            );
            songRepository.saveAll(defaultSongs);
            log.info("[SONG INIT] Đã lưu {} bài hát mẫu vào Database!", defaultSongs.size());
        }
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

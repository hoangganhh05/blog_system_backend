package com.example.blogsystem.repository;

import com.example.blogsystem.entity.Song;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SongRepository extends JpaRepository<Song, Long> {
    List<Song> findByIsActiveTrueOrderByCreatedAtAsc();
    List<Song> findByTitleContainingIgnoreCaseOrArtistContainingIgnoreCaseOrGenreContainingIgnoreCase(
            String title, String artist, String genre
    );
}

package com.example.blogsystem.repository;

import com.example.blogsystem.entity.PostTranslation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PostTranslationRepository extends JpaRepository<PostTranslation, Long> {
    Optional<PostTranslation> findByPostIdAndTargetLanguage(Long postId, String targetLanguage);
    void deleteByPostId(Long postId);
}

package com.example.appNN.repository;

import com.example.appNN.entity.VocabularyEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface VocabularyRepository extends JpaRepository<VocabularyEntity, Long> {

    @Query(value = """
            SELECT * FROM vocabulary v
            WHERE v.language_code = :lang
              AND v.level = :level
              AND v.id NOT IN (
                  SELECT lv.vocab_id
                  FROM lesson l
                  JOIN lesson_vocabulary lv ON l.id = lv.lesson_id
                  WHERE l.user_id = :userId
                    AND l.language_code = :lang
                    AND l.level = :level
              )
            ORDER BY RAND()
            LIMIT :limit
            """, nativeQuery = true)
    List<VocabularyEntity> findRandomUnlearnedForUser(
            @Param("userId") Long userId,
            @Param("lang") String languageCode,
            @Param("level") String level,
            @Param("limit") int limit
    );

    // get unlearned in vocabulary table with size = size user choose
    @Query(value = """
            SELECT * FROM vocabulary v
            WHERE v.language_code = :lang
              AND v.id NOT IN (
                  SELECT lv.vocab_id
                  FROM lesson l
                  JOIN lesson_vocabulary lv ON l.id = lv.lesson_id
                  WHERE l.user_id = :userId
                    AND l.language_code = :lang
              )
            ORDER BY RAND()
            LIMIT :limit
            """, nativeQuery = true)

    List<VocabularyEntity> findRandomUnlearnedForUserByLanguage(
            @Param("userId") Long userId,
            @Param("lang") String languageCode,
            @Param("limit") int limit
    );

    // Lấy toàn bộ từ của 1 lang + level (dùng làm nguồn tạo đáp án sai)
    List<VocabularyEntity> findByLanguageCode(String languageCode);
    
    /**
     * Kiểm tra từ đã tồn tại trong DB chưa (theo word và languageCode)
     * Trả về List vì có thể có duplicate trong DB
     */
    List<VocabularyEntity> findByWordAndLanguageCode(String word, String languageCode);

}


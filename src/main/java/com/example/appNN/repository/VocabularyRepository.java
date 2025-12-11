package com.example.appNN.repository;

import com.example.appNN.entity.VocabularyEntity;
import com.example.appNN.repository.custom.VocabularyRepositoryCustom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface VocabularyRepository extends JpaRepository<VocabularyEntity, Long> {



    @Query("SELECT MAX(v.lessonNo) FROM VocabularyEntity v " +
            "WHERE v.languageCode = :lang AND v.level = :level")
    Integer findMaxLessonNo(@Param("lang") String languageCode,
                            @Param("level") String level);

    @Query(value = """
            SELECT * FROM vocabulary
            WHERE language_code = :lang
              AND level = :level
              AND lesson_no IS NULL
            ORDER BY RAND()
            LIMIT :limit
            """, nativeQuery = true)
    List<VocabularyEntity> findRandomUnassigned(@Param("lang") String languageCode,
                                                @Param("level") String level,
                                                @Param("limit") int limit);

    List<VocabularyEntity> findByLanguageCodeAndLevelAndLessonNoOrderById(
            String languageCode, String level, Integer lessonNo
    );

    @Query("SELECT DISTINCT v.lessonNo FROM VocabularyEntity v " +
            "WHERE v.languageCode = :lang AND v.level = :level " +
            "AND v.lessonNo IS NOT NULL ORDER BY v.lessonNo")
    List<Integer> findDistinctLessons(@Param("lang") String languageCode,
                                      @Param("level") String level);


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

    // Lấy toàn bộ từ của 1 lang + level (dùng làm nguồn tạo đáp án sai)
    List<VocabularyEntity> findByLanguageCodeAndLevel(String languageCode, String level);

    // Lấy từ vựng theo languageCode, level và lessonNo
    List<VocabularyEntity> findByLanguageCodeAndLevelAndLessonNo(
            String languageCode, String level, Integer lessonNo
    );
    
    /**
     * Kiểm tra từ đã tồn tại trong DB chưa (theo word và languageCode)
     */
    Optional<VocabularyEntity> findByWordAndLanguageCode(String word, String languageCode);

}


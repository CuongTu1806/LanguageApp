package com.example.appNN.repository;

import com.example.appNN.entity.LessonEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface LessonRepository extends JpaRepository<LessonEntity, Long> {

    @Query("SELECT COALESCE(MAX(l.lessonIndex), 0) " +
            "FROM LessonEntity l " +
            "WHERE l.user.id = :userId " +
            "AND l.languageCode = :lang " +
            "AND l.level = :level")
    Integer findMaxLessonIndex(@Param("userId") Long userId,
                               @Param("lang") String languageCode,
                               @Param("level") String level);

    List<LessonEntity> findByUserIdAndLanguageCodeAndLevelOrderByLessonIndex(
            Long userId, String languageCode, String level
    );

    LessonEntity findByUserIdAndLanguageCodeAndLevelAndLessonIndex(
            Long userId, String languageCode, String level, Integer lessonIndex
    );



    @Query("""
    SELECT l FROM LessonEntity l
    WHERE l.user.id = :userId
      AND l.nextReviewAt IS NOT NULL
      AND l.nextReviewAt <= :until
    ORDER BY l.nextReviewAt ASC
    """)
    List<LessonEntity> findDueLessons(@Param("userId") Long userId,
                                      @Param("until") LocalDateTime until);

}

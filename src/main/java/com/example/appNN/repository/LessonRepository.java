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
                               @Param("lang") String languageCode);

    List<LessonEntity> findByUserIdAndLanguageCodeAndLevelOrderByLessonIndex(
            Long userId, String languageCode, String level
    );

    @Query("SELECT l FROM LessonEntity l " +
           "WHERE l.user.id = :userId " +
           "AND l.languageCode = :lang " +
           "AND l.level = :level " +
           "AND l.lessonIndex = :lessonIndex")
    LessonEntity findByUserIdAndLanguageCodeAndLessonIndex(
            @Param("userId") Long userId, 
            @Param("lang") String languageCode,
            @Param("lessonIndex") Integer lessonIndex
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
    
    // Lấy tất cả bài tập có hạn <= deadline (bao gồm quá hạn và sắp tới)
    @Query("""
    SELECT l FROM LessonEntity l
    WHERE l.user.id = :userId
      AND l.nextReviewAt IS NOT NULL
      AND l.nextReviewAt <= :deadline
    ORDER BY l.nextReviewAt ASC
    """)
    List<LessonEntity> findAllDueLessons(
            @Param("userId") Long userId,
            @Param("deadline") LocalDateTime deadline
    );
    
    // Đếm số bài tập quá hạn
    @Query("""
    SELECT COUNT(l) FROM LessonEntity l
    WHERE l.user.id = :userId
      AND l.nextReviewAt IS NOT NULL
      AND l.nextReviewAt < :now
    """)
    long countOverdueLessons(@Param("userId") Long userId, @Param("now") LocalDateTime now);

    // Tìm kiếm lesson theo số lượng từ
    List<LessonEntity> findByUserIdAndVocabularyCountBetween(
            Long userId, Integer minCount, Integer maxCount
    );
    
    List<LessonEntity> findByUserIdAndVocabularyCountGreaterThanEqual(
            Long userId, Integer minCount
    );
    
    List<LessonEntity> findByUserIdAndVocabularyCountLessThanEqual(
            Long userId, Integer maxCount
    );
    
    // Tìm tất cả lesson của user
    List<LessonEntity> findByUserId(Long userId);
    
    // Đếm số lesson của user theo ngôn ngữ và level
    Long countByUserIdAndLanguageCode(Long userId, String languageCode);
    
    // Tìm lesson theo type
    List<LessonEntity> findByUserIdAndLessonType(Long userId, String lessonType);
    
    List<LessonEntity> findByUserIdAndLessonTypeOrderByCreatedAtDesc(Long userId, String lessonType);

}

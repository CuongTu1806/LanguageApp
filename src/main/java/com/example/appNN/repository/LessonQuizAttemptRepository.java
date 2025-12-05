// com.example.appNN.repository.LessonQuizAttemptRepository
package com.example.appNN.repository;

import com.example.appNN.entity.LessonQuizAttemptEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface LessonQuizAttemptRepository extends JpaRepository<LessonQuizAttemptEntity, Long> {

    // Lịch sử quiz của 1 bài cho 1 user
    @Query("""
        SELECT a FROM LessonQuizAttemptEntity a
        WHERE a.user.id = :userId
          AND a.lesson.languageCode = :lang
          AND a.lesson.level = :level
          AND a.lesson.lessonIndex = :lessonNo
        ORDER BY a.createdAt DESC
    """)
    List<LessonQuizAttemptEntity> findByUserAndLesson(
            @Param("userId") Long userId,
            @Param("lang") String lang,
            @Param("level") String level,
            @Param("lessonNo") Integer lessonNo
    );
}

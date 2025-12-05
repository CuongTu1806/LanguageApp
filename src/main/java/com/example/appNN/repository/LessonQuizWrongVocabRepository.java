// com.example.appNN.repository.LessonQuizWrongVocabRepository
package com.example.appNN.repository;

import com.example.appNN.entity.LessonQuizWrongVocabEntity;
import com.example.appNN.entity.LessonQuizWrongVocabId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface LessonQuizWrongVocabRepository
        extends JpaRepository<LessonQuizWrongVocabEntity, LessonQuizWrongVocabId> {

    // Lấy danh sách vocab sai (distinct) cho 1 user + 1 bài
    @Query("""
        SELECT DISTINCT w.vocabulary.id
        FROM LessonQuizWrongVocabEntity w
        WHERE w.attempt.user.id = :userId
          AND w.attempt.lesson.languageCode = :lang
          AND w.attempt.lesson.level = :level
          AND w.attempt.lesson.lessonIndex = :lessonNo
    """)
    List<Long> findDistinctWrongVocabIdsForLesson(
            @Param("userId") Long userId,
            @Param("lang") String lang,
            @Param("level") String level,
            @Param("lessonNo") Integer lessonNo
    );
}

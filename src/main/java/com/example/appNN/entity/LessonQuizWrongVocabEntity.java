// com.example.appNN.entity.LessonQuizWrongVocabEntity
package com.example.appNN.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "lesson_quiz_wrong_vocab")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LessonQuizWrongVocabEntity {

    @EmbeddedId
    private LessonQuizWrongVocabId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("attemptId")
    @JoinColumn(name = "attempt_id")
    private LessonQuizAttemptEntity attempt;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("vocabId")
    @JoinColumn(name = "vocab_id")
    private VocabularyEntity vocabulary;
}

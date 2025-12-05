// com.example.appNN.entity.LessonQuizWrongVocabId
package com.example.appNN.entity;

import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LessonQuizWrongVocabId implements Serializable {
    private Long attemptId;
    private Long vocabId;
}

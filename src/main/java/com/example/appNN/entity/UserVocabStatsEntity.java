// com.example.appNN.entity.UserVocabStatsEntity
package com.example.appNN.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_vocab_stats")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class UserVocabStatsEntity {

    @EmbeddedId
    private UserVocabStatsId id;

    @Column(name = "total_attempts")
    private Integer totalAttempts;

    @Column(name = "wrong_count")
    private Integer wrongCount;

    @Column(name = "last_result")
    private String lastResult; // 'CORRECT' / 'WRONG'

    @Column(name = "last_attempt_at")
    private LocalDateTime lastAttemptAt;

    @Column(name = "last_wrong_at")
    private LocalDateTime lastWrongAt;

    @Column(name = "last_correct_at")
    private LocalDateTime lastCorrectAt;
}

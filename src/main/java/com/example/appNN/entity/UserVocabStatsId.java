// com.example.appNN.entity.UserVocabStatsId
package com.example.appNN.entity;

import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserVocabStatsId implements Serializable {

    private Long userId;
    private Long vocabId;
}

// com.example.appNN.dto.QuizResultDto
package com.example.appNN.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class QuizResultDto {
    private int score;
    private int total;
    private List<Long> wrongVocabIds;
}

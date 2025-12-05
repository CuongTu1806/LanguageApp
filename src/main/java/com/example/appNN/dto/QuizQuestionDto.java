package com.example.appNN.dto;

import java.util.List;

public class QuizQuestionDto {
    private Long vocabId;
    private String questionText;      // câu hỏi hiển thị
    private List<String> options;     // 4 lựa chọn
    private String correctAnswer;     // đáp án đúng

    // getters / setters
    public Long getVocabId() { return vocabId; }
    public void setVocabId(Long vocabId) { this.vocabId = vocabId; }

    public String getQuestionText() { return questionText; }
    public void setQuestionText(String questionText) { this.questionText = questionText; }

    public List<String> getOptions() { return options; }
    public void setOptions(List<String> options) { this.options = options; }

    public String getCorrectAnswer() { return correctAnswer; }
    public void setCorrectAnswer(String correctAnswer) { this.correctAnswer = correctAnswer; }
}

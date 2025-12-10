package com.example.appNN.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO để thêm vocabulary mới vào custom lesson
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AddVocabularyRequest {
    
    private String word;              // Từ Hán (bắt buộc)
    private String pronunciation;     // Phiên âm pinyin
    private String pos;               // Từ loại (n, v, adj, adv...)
    private String meaning;           // Nghĩa tiếng Việt (bắt buộc)
    private String exampleSrc;        // Ví dụ bằng tiếng Trung
    private String exampleVi;         // Ví dụ dịch tiếng Việt
    private String languageCode;      // cn, en, jp...
    private String level;             // A1, A2, B1...
    private Long customLessonId;      // ID của custom lesson cần thêm vào
}

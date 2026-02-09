package com.example.appNN.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response khi kiểm tra vocabulary đã tồn tại
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class VocabularyCheckResponse {
    
    private boolean exists;                    // Từ đã tồn tại trong DB?
    private VocabularyDto existingVocab;       // Thông tin từ đã tồn tại (nếu có)
    private String message;                    // Thông báo cho user
}

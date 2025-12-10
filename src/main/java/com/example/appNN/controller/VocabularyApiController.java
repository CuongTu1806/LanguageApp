package com.example.appNN.controller;

import com.example.appNN.entity.VocabularyEntity;
import com.example.appNN.repository.VocabularyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST API Controller để lấy chi tiết từ vựng
 * Dùng cho giao diện 2 cột: click từ bên trái -> hiển thị chi tiết bên phải
 */
@RestController
@RequestMapping("/api/vocabulary")
@RequiredArgsConstructor
public class VocabularyApiController {

    private final VocabularyRepository vocabularyRepository;

    /**
     * Lấy chi tiết một từ vựng
     * GET /api/vocabulary/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<VocabularyEntity> getVocabularyDetail(@PathVariable Long id) {
        return vocabularyRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}

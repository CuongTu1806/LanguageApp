package com.example.appNN.controller;

import com.example.appNN.entity.VocabularyEntity;
import com.example.appNN.entity.LessonEntity;
import com.example.appNN.entity.CustomLessonEntity;
import com.example.appNN.repository.VocabularyRepository;
import com.example.appNN.repository.LessonRepository;
import com.example.appNN.repository.CustomLessonRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api")
public class SearchController {
    
    @Autowired
    private VocabularyRepository vocabularyRepository;
    
    @Autowired
    private LessonRepository lessonRepository;
    
    @Autowired
    private CustomLessonRepository customLessonRepository;
    
    /**
     * Tìm kiếm từ vựng theo từ và ngôn ngữ
     */
    @GetMapping("/vocabulary/search")
    public ResponseEntity<VocabularyEntity> searchVocabulary(
            @RequestParam String word,
            @RequestParam String languageCode) {
        
        Optional<VocabularyEntity> vocab = vocabularyRepository
                .findByWordAndLanguageCode(word, languageCode);
        
        return vocab.map(ResponseEntity::ok)
                   .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * Tìm kiếm bài học tự động (regular lessons)
     */
    @GetMapping("/lessons/search")
    public ResponseEntity<List<LessonEntity>> searchLessons(
            @RequestParam String languageCode,
            @RequestParam Long userId,
            @RequestParam(required = false) String level,
            @RequestParam(required = false) Integer minCount,
            @RequestParam(required = false) Integer maxCount) {
        
        List<LessonEntity> lessons;
        
        // Lọc theo số lượng từ
        if (minCount != null && maxCount != null) {
            lessons = lessonRepository.findByUserIdAndVocabularyCountBetween(userId, minCount, maxCount);
        } else if (minCount != null) {
            lessons = lessonRepository.findByUserIdAndVocabularyCountGreaterThanEqual(userId, minCount);
        } else if (maxCount != null) {
            lessons = lessonRepository.findByUserIdAndVocabularyCountLessThanEqual(userId, maxCount);
        } else {
            // Không lọc theo số lượng
            lessons = lessonRepository.findByUserId(userId);
        }
        
        // Lọc thêm theo ngôn ngữ và level
        List<LessonEntity> filtered = lessons.stream()
            .filter(lesson -> lesson.getLanguageCode().equals(languageCode))
            .filter(lesson -> level == null || lesson.getLevel().equals(level))
            .toList();
        
        return ResponseEntity.ok(filtered);
    }
    
    /**
     * Tìm kiếm bài học tự tạo (custom lessons)
     */
    @GetMapping("/lessons/search/custom")
    public ResponseEntity<List<CustomLessonEntity>> searchCustomLessons(
            @RequestParam String languageCode,
            @RequestParam Long userId,
            @RequestParam(required = false) String level,
            @RequestParam(required = false) Integer minCount,
            @RequestParam(required = false) Integer maxCount) {
        
        List<CustomLessonEntity> lessons;
        
        // Lọc theo số lượng từ
        if (minCount != null && maxCount != null) {
            lessons = customLessonRepository.findByUserIdAndVocabularyCountBetween(userId, minCount, maxCount);
        } else if (minCount != null) {
            lessons = customLessonRepository.findByUserIdAndVocabularyCountGreaterThanEqual(userId, minCount);
        } else if (maxCount != null) {
            lessons = customLessonRepository.findByUserIdAndVocabularyCountLessThanEqual(userId, maxCount);
        } else {
            // Không lọc theo số lượng
            lessons = customLessonRepository.findByUserId(userId);
        }
        
        // Lọc thêm theo ngôn ngữ và level
        List<CustomLessonEntity> filtered = lessons.stream()
            .filter(lesson -> lesson.getLanguageCode().equals(languageCode))
            .filter(lesson -> level == null || lesson.getLevel().equals(level))
            .toList();
        
        return ResponseEntity.ok(filtered);
    }
}

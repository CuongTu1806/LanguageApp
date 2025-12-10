package com.example.appNN.controller;

import com.example.appNN.dto.AddVocabularyRequest;
import com.example.appNN.dto.VocabularyCheckResponse;
import com.example.appNN.entity.CustomLessonEntity;
import com.example.appNN.entity.VocabularyEntity;
import com.example.appNN.service.CustomLessonService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Controller quản lý custom vocabulary và custom lesson
 */
@Slf4j
@RestController
@RequestMapping("/api/custom-vocabulary")
@RequiredArgsConstructor
public class CustomVocabularyController {
    
    private final CustomLessonService customLessonService;
    
    /**
     * Kiểm tra từ đã tồn tại trong DB chưa
     * POST /api/custom-vocabulary/check
     * 
     * Request body: {"word": "茶", "languageCode": "cn"}
     */
    @PostMapping("/check")
    public ResponseEntity<VocabularyCheckResponse> checkVocabularyExists(
            @RequestBody Map<String, String> request) {
        
        String word = request.get("word");
        String languageCode = request.get("languageCode");
        
        if (word == null || word.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(
                new VocabularyCheckResponse(false, null, "Word is required")
            );
        }
        
        VocabularyCheckResponse response = customLessonService.checkVocabularyExists(word, languageCode);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Thêm vocabulary vào custom lesson
     * Nếu từ chưa có trong DB → tạo mới
     * Nếu từ đã có trong DB → dùng từ cũ (nếu user accept)
     * 
     * POST /api/custom-vocabulary/add
     */
    @PostMapping("/add")
    public ResponseEntity<Map<String, Object>> addVocabularyToCustomLesson(
            @RequestBody AddVocabularyRequest request) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Validate input
            if (request.getWord() == null || request.getWord().trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "Word is required");
                return ResponseEntity.badRequest().body(response);
            }
            
            if (request.getMeaning() == null || request.getMeaning().trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "Meaning is required");
                return ResponseEntity.badRequest().body(response);
            }
            
            if (request.getCustomLessonId() == null) {
                response.put("success", false);
                response.put("message", "Custom lesson ID is required");
                return ResponseEntity.badRequest().body(response);
            }
            
            // Thêm vocabulary vào custom lesson
            VocabularyEntity vocab = customLessonService.addVocabularyToCustomLesson(request);
            
            response.put("success", true);
            response.put("message", "Vocabulary added successfully");
            response.put("vocabulary", vocab);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error adding vocabulary to custom lesson", e);
            response.put("success", false);
            response.put("message", "Error: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    /**
     * Tạo custom lesson mới
     * POST /api/custom-vocabulary/lesson/create
     * 
     * Request body: {
     *   "userId": 1,
     *   "title": "Bài học của tôi",
     *   "description": "Mô tả",
     *   "languageCode": "cn",
     *   "level": "A1"
     * }
     */
    @PostMapping("/lesson/create")
    public ResponseEntity<Map<String, Object>> createCustomLesson(
            @RequestBody Map<String, Object> request) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            Long userId = Long.valueOf(request.get("userId").toString());
            String title = (String) request.get("title");
            String description = (String) request.get("description");
            String languageCode = (String) request.get("languageCode");
            String level = (String) request.get("level");
            
            CustomLessonEntity lesson = customLessonService.createCustomLesson(
                userId, title, description, languageCode, level
            );
            
            response.put("success", true);
            response.put("message", "Custom lesson created");
            response.put("lesson", lesson);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error creating custom lesson", e);
            response.put("success", false);
            response.put("message", "Error: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    /**
     * Lấy danh sách custom lesson của user
     * GET /api/custom-vocabulary/lesson/my-lessons?userId=1
     */
    @GetMapping("/lesson/my-lessons")
    public ResponseEntity<Map<String, Object>> getMyCustomLessons(
            @RequestParam Long userId) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            var lessons = customLessonService.getMyCustomLessons(userId);
            
            response.put("success", true);
            response.put("lessons", lessons);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error getting custom lessons", e);
            response.put("success", false);
            response.put("message", "Error: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    /**
     * Xem chi tiết custom lesson (bao gồm danh sách vocabulary)
     * GET /api/custom-vocabulary/lesson/{lessonId}
     */
    @GetMapping("/lesson/{lessonId}")
    public ResponseEntity<Map<String, Object>> getCustomLessonDetail(
            @PathVariable Long lessonId) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            CustomLessonEntity lesson = customLessonService.getCustomLessonById(lessonId);
            
            if (lesson == null) {
                response.put("success", false);
                response.put("message", "Custom lesson not found");
                return ResponseEntity.notFound().build();
            }
            
            response.put("success", true);
            response.put("lesson", lesson);
            response.put("vocabularies", lesson.getVocabularies());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error getting custom lesson detail", e);
            response.put("success", false);
            response.put("message", "Error: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
}

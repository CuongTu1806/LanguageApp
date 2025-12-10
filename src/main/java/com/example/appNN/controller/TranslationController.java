package com.example.appNN.controller;

import com.example.appNN.service.TranslationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Controller để thêm nghĩa tiếng Anh vào từ vựng
 */
@RestController
@RequestMapping("/admin/translation")
@RequiredArgsConstructor
public class TranslationController {

    private final TranslationService translationService;

    /**
     * Thêm nghĩa tiếng Anh cho TẤT CẢ từ vựng
     * WARNING: Mất khoảng 15 phút cho 896 từ
     * 
     * POST http://localhost:8080/admin/translation/add-english-all
     */
    @PostMapping("/add-english-all")
    public ResponseEntity<Map<String, Object>> addEnglishToAll() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Chạy trong thread riêng
            new Thread(() -> {
                translationService.addEnglishMeaningToAll();
            }).start();
            
            response.put("status", "started");
            response.put("message", "Translation process started in background. Check logs for progress.");
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * Thêm nghĩa tiếng Anh cho một bài học cụ thể
     * 
     * POST http://localhost:8080/admin/translation/add-english-lesson/cn/A1/1
     */
    @PostMapping("/add-english-lesson/{lang}/{level}/{lessonNo}")
    public ResponseEntity<Map<String, Object>> addEnglishToLesson(
            @PathVariable String lang,
            @PathVariable String level,
            @PathVariable Integer lessonNo) {
        
        Map<String, Object> response = new HashMap<>();
        
        int updated = translationService.addEnglishMeaningToLesson(lang, level, lessonNo);
        
        response.put("updated", updated);
        response.put("lesson", String.format("%s-%s-L%d", lang, level, lessonNo));
        response.put("message", String.format("Added English meanings to %d vocabularies", updated));
        
        return ResponseEntity.ok(response);
    }

    /**
     * Test dịch một từ Hanzi sang tiếng Anh
     * 
     * GET http://localhost:8080/admin/translation/test?word=茶&meaning=trà
     */
    @GetMapping("/test")
    public ResponseEntity<Map<String, String>> testTranslation(
            @RequestParam String word,
            @RequestParam(required = false) String meaning) {
        Map<String, String> response = new HashMap<>();
        
        String translation = translationService.translateZhToEn(word, meaning);
        
        response.put("chinese", word);
        response.put("vietnamese", meaning);
        response.put("english", translation);
        
        return ResponseEntity.ok(response);
    }
}

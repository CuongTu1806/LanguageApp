package com.example.appNN.controller;

import com.example.appNN.service.VocabularyEnrichmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Controller để quản lý việc enrichment dữ liệu từ vựng
 * (Lấy hình ảnh, ví dụ, v.v.)
 */
@RestController
@RequestMapping("/admin/vocabulary")
@RequiredArgsConstructor
public class VocabularyEnrichmentController {

    private final VocabularyEnrichmentService enrichmentService;

    /**
     * Endpoint để tự động lấy hình ảnh cho TẤT CẢ từ vựng
     * WARNING: Process này có thể mất nhiều thời gian (896 từ × 1 giây = ~15 phút)
     * 
     * Cách dùng: POST http://localhost:8080/admin/vocabulary/enrich-all
     */
    @PostMapping("/enrich-all")
    public ResponseEntity<Map<String, String>> enrichAll() {
        Map<String, String> response = new HashMap<>();
        
        try {
            // Chạy trong thread riêng để không block request
            new Thread(() -> {
                enrichmentService.enrichAllVocabulary();
            }).start();
            
            response.put("status", "started");
            response.put("message", "Enrichment process started in background. Check logs for progress.");
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * Endpoint để lấy hình ảnh cho một từ vựng cụ thể
     * 
     * Cách dùng: POST http://localhost:8080/admin/vocabulary/enrich/{vocabId}
     */
    @PostMapping("/enrich/{vocabId}")
    public ResponseEntity<Map<String, Object>> enrichSingle(@PathVariable Long vocabId) {
        Map<String, Object> response = new HashMap<>();
        
        boolean success = enrichmentService.enrichSingleVocabulary(vocabId);
        
        response.put("success", success);
        response.put("vocabId", vocabId);
        
        if (success) {
            response.put("message", "Successfully enriched vocabulary");
            return ResponseEntity.ok(response);
        } else {
            response.put("message", "Failed to enrich vocabulary");
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Endpoint để lấy hình ảnh cho một bài học cụ thể
     * 
     * Cách dùng: POST http://localhost:8080/admin/vocabulary/enrich-lesson/cn/A1/1
     */
    @PostMapping("/enrich-lesson/{lang}/{level}/{lessonNo}")
    public ResponseEntity<Map<String, Object>> enrichLesson(
            @PathVariable String lang,
            @PathVariable String level,
            @PathVariable Integer lessonNo,
            @RequestParam(defaultValue = "1") Long userId) {
        
        Map<String, Object> response = new HashMap<>();
        
        int success = enrichmentService.enrichLesson(userId, lang, level, lessonNo);
        
        response.put("success", success);
        response.put("lesson", String.format("%s-%s-L%d", lang, level, lessonNo));
        response.put("message", String.format("Enriched %d vocabularies", success));
        
        return ResponseEntity.ok(response);
    }
}

package com.example.appNN.controller;

import com.example.appNN.service.LessonService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST API cho assignments
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/assignments")
public class AssignmentApiController {
    
    private final LessonService lessonService;
    
    @GetMapping("/overdue-count")
    public ResponseEntity<Long> getOverdueCount(@RequestParam Long userId) {
        long count = lessonService.countOverdueLessons(userId);
        return ResponseEntity.ok(count);
    }
}

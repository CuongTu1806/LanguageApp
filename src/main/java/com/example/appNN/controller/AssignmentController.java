package com.example.appNN.controller;

import com.example.appNN.entity.LessonEntity;
import com.example.appNN.service.LessonService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Controller cho quản lý bài tập tới hạn
 */
@Controller
@RequiredArgsConstructor
@RequestMapping("/assignments")
public class AssignmentController {

    private final LessonService lessonService;

    private Long getCurrentUserId() {
        return 1L; // TODO: Lấy từ session/authentication
    }

    /**
     * Trang danh sách bài tập tới hạn
     */
    @GetMapping
    public String assignmentsList(Model model) {
        Long userId = getCurrentUserId();
        
        // Lấy bài tập tới hạn trong 7 ngày tới (cả system và personal lessons)
        List<LessonEntity> upcomingLessons = lessonService.getUpcomingDueLessons(userId, 7);
        
        // Đếm tổng số bài quá hạn
        long overdueCount = lessonService.countOverdueLessons(userId);
        
        model.addAttribute("upcomingLessons", upcomingLessons);
        model.addAttribute("overdueCount", overdueCount);
        model.addAttribute("userId", userId);
        
        return "assignments";
    }
}

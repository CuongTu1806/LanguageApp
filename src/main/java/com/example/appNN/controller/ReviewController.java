package com.example.appNN.controller;

import com.example.appNN.entity.LessonEntity;
import com.example.appNN.service.LessonService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/reviews")
public class ReviewController {

    private final LessonService lessonService;

    private Long getCurrentUserId() {
        return 1L; // sau này lấy từ user đăng nhập
    }

    @GetMapping
    public String reviewSchedule(Model model) {
        Long userId = getCurrentUserId();
        List<LessonEntity> dueLessons = lessonService.getDueLessons(userId);

        model.addAttribute("lessons", dueLessons);
        return "review_schedule";
    }
}

package com.example.appNN.controller;

import com.example.appNN.entity.CustomLessonEntity;
import com.example.appNN.service.CustomLessonService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

/**
 * Controller cho custom lesson - Người dùng tự tạo bài học
 */
@Controller
@RequiredArgsConstructor
@RequestMapping("/custom-lessons")
public class CustomLessonController {

    private final CustomLessonService customLessonService;

    private Long getCurrentUserId() {
        return 1L; // TODO: Lấy từ session/authentication
    }

    /**
     * Trang danh sách bài học tự tạo của user
     */
    @GetMapping
    public String listMyCustomLessons(Model model) {
        Long userId = getCurrentUserId();
        List<CustomLessonEntity> lessons = customLessonService.getMyCustomLessons(userId);
        
        model.addAttribute("lessons", lessons);
        return "custom_lesson_list";
    }

    /**
     * Trang tạo bài học mới
     */
    @GetMapping("/create")
    public String showCreateForm(Model model) {
        model.addAttribute("userId", getCurrentUserId());
        return "custom_lesson_create";
    }

    /**
     * Tạo custom lesson mới (chỉ tạo lesson rỗng, chưa có từ)
     */
    @PostMapping("/create")
    public String createCustomLesson(
            @RequestParam("title") String title,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "languageCode", defaultValue = "cn") String languageCode,
            @RequestParam(value = "level", defaultValue = "A1") String level,
            RedirectAttributes redirectAttributes) {

        Long userId = getCurrentUserId();
        
        CustomLessonEntity lesson = customLessonService.createCustomLesson(
            userId, title, description, languageCode, level
        );

        redirectAttributes.addFlashAttribute("message", 
            "Tạo bài học '" + lesson.getTitle() + "' thành công! Hãy thêm từ vào bài.");
        
        return "redirect:/custom-lessons/" + lesson.getId() + "/edit";
    }

    /**
     * Trang chỉnh sửa bài học (thêm/xóa từ)
     */
    @GetMapping("/{lessonId}/edit")
    public String editCustomLesson(@PathVariable Long lessonId, Model model) {
        CustomLessonEntity lesson = customLessonService.getCustomLessonById(lessonId);
        
        model.addAttribute("lesson", lesson);
        model.addAttribute("vocabularies", lesson.getVocabularies());
        
        return "custom_lesson_edit";
    }

    /**
     * Xem nội dung bài học custom (giống lesson_view)
     */
    @GetMapping("/{lessonId}/view")
    public String viewCustomLesson(@PathVariable Long lessonId, Model model) {
        CustomLessonEntity lesson = customLessonService.getCustomLessonById(lessonId);
        
        model.addAttribute("lesson", lesson);
        model.addAttribute("words", lesson.getVocabularies());
        
        return "custom_lesson_view";
    }
}

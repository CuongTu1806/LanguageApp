package com.example.appNN.controller;

import com.example.appNN.entity.VocabularyEntity;
import com.example.appNN.service.LessonService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/courses")
public class LessonController {

    private final LessonService lessonService;

    // Tạm hard-code userId = 1, sau này lấy từ user đăng nhập
    private Long getCurrentUserId() {
        return 1L;
    }

    // Trang danh sách bài cho 1 khóa (lang + level)
    @GetMapping("/{lang}/{level}/lessons")
    public String listLessons(@PathVariable String lang,
                              @PathVariable String level,
                              Model model,
                              @ModelAttribute("message") String message) {

        Long userId = getCurrentUserId();
        List<Integer> lessons = lessonService.listLessons(userId, lang, level);

        model.addAttribute("lang", lang);
        model.addAttribute("level", level);
        model.addAttribute("lessons", lessons);
        model.addAttribute("message", message);

        return "lesson_list";
    }

    // Tạo bài mới
    @PostMapping("/{lang}/{level}/lessons/new")
    public String createLesson(@PathVariable String lang,
                               @PathVariable String level,
                               @RequestParam("size") int size,
                               RedirectAttributes redirectAttributes) {

        Long userId = getCurrentUserId();
        Integer newLessonNo = lessonService.createNewLesson(userId, lang, level, size);

        if (newLessonNo == null) {
            redirectAttributes.addFlashAttribute("message",
                    "Không còn đủ từ để tạo bài mới cho khóa này.");
        } else {
            redirectAttributes.addFlashAttribute("message",
                    "Tạo bài học số " + newLessonNo + " thành công!");
        }

        return "redirect:/courses/" + lang + "/" + level + "/lessons";
    }

    // Xem nội dung 1 bài
    @GetMapping("/{lang}/{level}/lessons/{lessonNo}")
    public String viewLesson(@PathVariable String lang,
                             @PathVariable String level,
                             @PathVariable Integer lessonNo,
                             Model model) {

        Long userId = getCurrentUserId();
        List<VocabularyEntity> words =
                lessonService.getLessonWords(userId, lang, level, lessonNo);

        model.addAttribute("lang", lang);
        model.addAttribute("level", level);
        model.addAttribute("lessonNo", lessonNo);
        model.addAttribute("words", words);

        return "lesson_view";
    }
}

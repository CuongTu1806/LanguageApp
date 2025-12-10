// com.example.appNN.controller.CourseController
package com.example.appNN.controller;

import com.example.appNN.entity.LessonEntity;
import com.example.appNN.service.LessonService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/courses")
@RequiredArgsConstructor
public class CourseController {

    private final LessonService lessonService;

    // tạm: user 1, sau này lấy từ login
    private Long getCurrentUserId() {
        return 1L;
    }

    // Trang chọn khóa học + khung bài đến hạn ôn
    @GetMapping("/select")
    public String showCourseSelect(Model model) {

        // phần form chọn khóa
        model.addAttribute("selectedLang", "cn");
        model.addAttribute("selectedLevel", "A1");

        // Lấy danh sách bài đến hạn ôn
        Long userId = getCurrentUserId();
        List<LessonEntity> dueLessons = lessonService.getDueLessons(userId);
        model.addAttribute("dueLessons", dueLessons);

        return "course_select";
    }

    @PostMapping("/select")
    public String handleCourseSelect(@RequestParam("language") String lang,
                                     @RequestParam("level") String level) {

        return "redirect:/courses/" + lang + "/" + level + "/lessons";
    }
}

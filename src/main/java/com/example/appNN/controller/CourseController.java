// com.example.appNN.controller.CourseController
package com.example.appNN.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/courses")
public class CourseController {

    // Hiển thị form chọn khóa học
    @GetMapping("/select")
    public String showCourseSelect(Model model) {
        // giá trị mặc định
        model.addAttribute("selectedLang", "cn");
        model.addAttribute("selectedLevel", "A1");
        return "course_select";   // file course_select.html
    }

    // Xử lý submit form
    @PostMapping("/select")
    public String handleCourseSelect(@RequestParam("language") String lang,
                                     @RequestParam("level") String level) {

        // redirect sang danh sách bài học của khóa đó
        return "redirect:/courses/" + lang + "/" + level + "/lessons";
    }
}

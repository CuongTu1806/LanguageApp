package com.example.appNN.controller;

import com.example.appNN.entity.UserVocabStatsEntity;
import com.example.appNN.entity.VocabularyEntity;
import com.example.appNN.repository.UserVocabStatsRepository;
import com.example.appNN.repository.VocabularyRepository;
import com.example.appNN.service.LessonService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
@RequestMapping("/courses")
public class LessonController {

    private final LessonService lessonService;
    private final UserVocabStatsRepository userVocabStatsRepository;
    private final VocabularyRepository vocabularyRepository;

    // Tạm hard-code userId = 1, sau này lấy từ user đăng nhập
    private Long getCurrentUserId() {
        return 1L;
    }

    // Trang tạo bài học tùy chỉnh
    @GetMapping("/custom-lesson/create")
    public String customLessonCreatePage(Model model) {
        model.addAttribute("userId", getCurrentUserId());
        return "custom_lesson_create";
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
        
        // Lấy từ LessonEntity (ManyToMany relationship)
        List<VocabularyEntity> words = lessonService.getLessonWords(userId, lang, level, lessonNo);

        // Lấy danh sách id từ vựng
        List<Long> vocabIds = words.stream()
                .map(VocabularyEntity::getId)
                .toList();

        // Lấy thống kê theo user + các vocab của bài này
        List<UserVocabStatsEntity> statsList =
                userVocabStatsRepository.findByIdUserIdAndIdVocabIdIn(userId, vocabIds);

        // Đưa về dạng Map<vocabId, stats> cho dễ xử lý ở Thymeleaf
        Map<Long, UserVocabStatsEntity> statsMap = new HashMap<>();
        for (UserVocabStatsEntity s : statsList) {
            statsMap.put(s.getId().getVocabId(), s);
        }

        model.addAttribute("lang", lang);
        model.addAttribute("level", level);
        model.addAttribute("lessonNo", lessonNo);
        model.addAttribute("words", words);
        model.addAttribute("statsMap", statsMap);

        return "lesson_view";
    }
}

package com.example.appNN.controller;

import com.example.appNN.dto.VocabularyDto;
import com.example.appNN.entity.LessonEntity;
import com.example.appNN.entity.UserVocabStatsEntity;
import com.example.appNN.repository.UserVocabStatsRepository;
import com.example.appNN.service.LessonService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
@RequestMapping("/lessons")
public class LessonController {

    private final LessonService lessonService;
    private final UserVocabStatsRepository userVocabStatsRepository;

    // set the temporary userId to 1
    private Long getCurrentUserId() {
        return 1L;
    }

    // API page "crate personalized lessons"
    @GetMapping("/custom-lesson/create")
    public String customLessonCreatePage(Model model) {
        model.addAttribute("userId", getCurrentUserId());
        return "custom_lesson_create";
    }

    // Get the list of lessons
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

    // API create New System Lesson
    @PostMapping("/{lang}/lessons/create")
    @ResponseBody
    public Map<String, Object> createLesson(@PathVariable String lang,
                                            @RequestParam("size") int size) {

        Long userId = getCurrentUserId();
        Integer newLessonNo = lessonService.createNewLesson(userId, lang, size);

        Map<String, Object> response = new HashMap<>();
        
        if (newLessonNo == null) {
            response.put("success", false);
            response.put("message", "Không còn đủ từ để tạo bài mới cho khóa này.");
        } else {
            response.put("success", true);
            response.put("message", "Tạo bài học số " + newLessonNo + " thành công!");
            response.put("lessonNo", newLessonNo);
        }

        return response;
    }

    // Display all word in lesson, that user choose
    @GetMapping("/{lessonId}")
    public String viewLesson(@PathVariable Long lessonId, Model model) {

        Long userId = getCurrentUserId();
        
        // Lấy lesson entity
        LessonEntity lesson = lessonService.findById(lessonId)
                .orElseThrow(() -> new RuntimeException("Lesson not found: " + lessonId));
        
        // Lấy từ LessonEntity (sử dụng DTO để tránh trùng ID)
        List<VocabularyDto> words = lessonService.getLessonWords(lessonId);

        // Lấy danh sách id từ vựng (phân biệt system và user vocab)
        List<Long> systemVocabIds = words.stream()
                .filter(w -> w.getSystemVocabId() != null)
                .map(VocabularyDto::getSystemVocabId)
                .toList();
        
        List<Long> userVocabIds = words.stream()
                .filter(w -> w.getUserVocabId() != null)
                .map(VocabularyDto::getUserVocabId)
                .toList();

        // Tạo list tổng hợp để query stats (không trùng lắp)
        List<Long> allVocabIds = new ArrayList<>();
        allVocabIds.addAll(systemVocabIds);
        allVocabIds.addAll(userVocabIds);

        // Lấy thống kê theo user + các vocab của bài này
        List<UserVocabStatsEntity> statsList =
                userVocabStatsRepository.findByIdUserIdAndIdVocabIdIn(userId, allVocabIds);

        // Đưa về dạng Map<vocabId, stats> cho dễ xử lý ở Thymeleaf
        Map<Long, UserVocabStatsEntity> statsMap = new HashMap<>();
        for (UserVocabStatsEntity s : statsList) {
            statsMap.put(s.getId().getVocabId(), s);
        }

        model.addAttribute("lesson", lesson);
        model.addAttribute("words", words);
        model.addAttribute("statsMap", statsMap);

        return "lesson_view";
    }
}

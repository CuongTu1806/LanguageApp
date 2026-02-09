package com.example.appNN.controller;

import com.example.appNN.dto.QuizQuestionDto;
import com.example.appNN.dto.QuizResultDto;
import com.example.appNN.dto.VocabularyDto;
import com.example.appNN.entity.LessonEntity;
import com.example.appNN.model.QuizMode;
import com.example.appNN.service.LessonService;
import com.example.appNN.service.QuizService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controller cho personal lesson - Người dùng tự tạo bài học
 */
@Controller
@RequiredArgsConstructor
@RequestMapping("/custom-lessons")
public class CustomLessonController {

    private final LessonService lessonService;
    private final QuizService quizService;

    private Long getCurrentUserId() {
        return 1L; // TODO: Lấy từ session/authentication
    }

    /**
     * Trang danh sách bài học tự tạo của user
     */
    @GetMapping
    public String listMyCustomLessons(Model model) {
        Long userId = getCurrentUserId();
        List<LessonEntity> lessons = lessonService.getPersonalLessons(userId);
        
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
        
        LessonEntity lesson = lessonService.createPersonalLesson(
            userId, title, description, languageCode, level
        );

        redirectAttributes.addFlashAttribute("success", 
            "Tạo bài học '" + lesson.getTitle() + "' thành công! Hãy thêm từ vào bài.");
        
        return "redirect:/custom-lessons/" + lesson.getId() + "/edit";
    }

    /**
     * API: Thêm từ vào custom lesson
     */
    @PostMapping("/{lessonId}/vocabulary/add")
    @ResponseBody
    public Map<String, Object> addVocabularyToLesson(
            @PathVariable Long lessonId,
            @RequestParam String word,
            @RequestParam(required = false) String pronunciation,
            @RequestParam(required = false) String pos,
            @RequestParam(required = false) String meaning,
            @RequestParam(required = false) String exampleSrc,
            @RequestParam(required = false) String exampleVi,
            @RequestParam(required = false) String languageCode,
            @RequestParam(required = false) String level) {
        
        Long userId = getCurrentUserId();
        Map<String, Object> response = new HashMap<>();
        
        try {
            com.example.appNN.dto.AddVocabularyRequest request = new com.example.appNN.dto.AddVocabularyRequest();
            request.setCustomLessonId(lessonId);
            request.setWord(word);
            request.setPronunciation(pronunciation);
            request.setPos(pos);
            request.setMeaning(meaning);
            request.setExampleSrc(exampleSrc);
            request.setExampleVi(exampleVi);
            request.setLanguageCode(languageCode != null ? languageCode : "cn");
            request.setLevel(level != null ? level : "A1");
            
            lessonService.addVocabularyToPersonalLesson(request);
            response.put("success", true);
            response.put("message", "Đã thêm từ '" + word + "' vào bài học");
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Lỗi: " + e.getMessage());
        }
        
        return response;
    }

    /**
     * API: Xóa từ khỏi custom lesson
     */
    @DeleteMapping("/{lessonId}/vocabulary/{vocabId}")
    @ResponseBody
    public Map<String, Object> removeVocabularyFromLesson(
            @PathVariable Long lessonId,
            @PathVariable Long vocabId,
            @RequestParam(required = false, defaultValue = "false") boolean isUserVocab) {
        
        Long userId = getCurrentUserId();
        Map<String, Object> response = new HashMap<>();
        
        try {
            lessonService.removeVocabularyFromPersonalLesson(lessonId, userId, vocabId, isUserVocab);
            response.put("success", true);
            response.put("message", "Đã xóa từ khỏi bài học");
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Lỗi: " + e.getMessage());
        }
        
        return response;
    }

    /**
     * API: Cập nhật thông tin từ vựng user-created
     */
    @PutMapping("/{lessonId}/vocabulary/{vocabId}")
    @ResponseBody
    public Map<String, Object> updateUserVocabulary(
            @PathVariable Long lessonId,
            @PathVariable Long vocabId,
            @RequestParam String word,
            @RequestParam(required = false) String pronunciation,
            @RequestParam(required = false) String pos,
            @RequestParam(required = false) String meaning,
            @RequestParam(required = false) String exampleSrc,
            @RequestParam(required = false) String exampleVi,
            @RequestParam(required = false) String level) {
        
        Long userId = getCurrentUserId();
        Map<String, Object> response = new HashMap<>();
        
        try {
            lessonService.updateUserVocabulary(
                vocabId, userId, word, pronunciation, pos, meaning,
                exampleSrc, exampleVi, level
            );
            response.put("success", true);
            response.put("message", "Đã cập nhật từ '" + word + "' thành công");
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Lỗi: " + e.getMessage());
        }
        
        return response;
    }

    /**
     * Trang chỉnh sửa bài học (thêm/xóa từ)
     */
    @GetMapping("/{lessonId}/edit")
    public String editCustomLesson(@PathVariable Long lessonId, Model model) {
        LessonEntity lesson = lessonService.findById(lessonId)
            .orElseThrow(() -> new RuntimeException("Lesson not found"));
        
        // Lấy tất cả vocabulary dưới dạng DTO (tránh circular reference)
        List<VocabularyDto> vocabularies = lessonService.getAllVocabulariesForLessonAsDto(lessonId);
        
        model.addAttribute("lesson", lesson);
        model.addAttribute("vocabularies", vocabularies);
        
        return "custom_lesson_edit";
    }

    /**
     * Xem nội dung bài học custom (giống lesson_view)
     */
    @GetMapping("/{lessonId}/view")
    public String viewCustomLesson(@PathVariable Long lessonId, Model model) {
        Long userId = getCurrentUserId();
        LessonEntity lesson = lessonService.findById(lessonId)
            .orElseThrow(() -> new RuntimeException("Lesson not found"));
        
        // Lấy tất cả personal lessons của user để tính index
        List<LessonEntity> allLessons = lessonService.getPersonalLessons(userId);
        int lessonIndex = 1; // Mặc định
        
        for (int i = 0; i < allLessons.size(); i++) {
            if (allLessons.get(i).getId().equals(lessonId)) {
                lessonIndex = i + 1;
                break;
            }
        }
        
        // Lấy tất cả vocabulary dưới dạng DTO (tránh circular reference khi serialize JSON)
        List<VocabularyDto> vocabularies = lessonService.getAllVocabulariesForLessonAsDto(lessonId);
        
        model.addAttribute("lesson", lesson);
        model.addAttribute("words", vocabularies);
        model.addAttribute("lessonIndex", lessonIndex);
        
        return "custom_lesson_view";
    }

    /**
     * Bắt đầu quiz cho custom lesson
     */
    @GetMapping("/{lessonId}/quiz")
    public String startCustomQuiz(@PathVariable Long lessonId,
                                  @RequestParam(name = "mode", defaultValue = "1") int mode,
                                  Model model,
                                  HttpSession session) {
        Long userId = getCurrentUserId();
        LessonEntity lesson = lessonService.findById(lessonId)
            .orElseThrow(() -> new RuntimeException("Lesson not found"));

        QuizMode quizMode;
        switch (mode) {
            case 1 -> quizMode = QuizMode.WORD_PRON_TO_MEANING;
            case 2 -> quizMode = QuizMode.MEANING_TO_WORD;
            case 3 -> quizMode = QuizMode.WORD_TO_MEANING;
            case 4 -> quizMode = QuizMode.FILL_IN;
            default -> quizMode = QuizMode.WORD_PRON_TO_MEANING;
        }

        // Build quiz from personal lesson vocabularies
        List<QuizQuestionDto> questions = quizService.buildQuizForPersonalLesson(
            userId, lesson, quizMode
        );

        // Lưu vào session để chấm điểm
        session.setAttribute("quizQuestions", questions);
        session.setAttribute("quizMode", quizMode);
        session.setAttribute("customLessonId", lessonId);

        model.addAttribute("lang", lesson.getLanguageCode());
        model.addAttribute("level", lesson.getLevel());
        model.addAttribute("lessonNo", lessonId); // Use lessonId as identifier
        model.addAttribute("questions", questions);
        model.addAttribute("mode", mode);
        model.addAttribute("isCustomLesson", true);

        // Mode 4 sử dụng template khác (hiển thị tất cả câu hỏi)
        if (mode == 4) {
            return "quiz_fill";
        }
        return "quiz_do";
    }

    /**
     * Nộp bài quiz cho custom lesson
     */
    @PostMapping("/{lessonId}/quiz/submit")
    public String submitCustomQuiz(@PathVariable Long lessonId,
                                   @RequestParam Map<String, String> params,
                                   HttpSession session,
                                   Model model) {
        Long userId = getCurrentUserId();
        LessonEntity lesson = lessonService.findById(lessonId)
            .orElseThrow(() -> new RuntimeException("Lesson not found"));

        @SuppressWarnings("unchecked")
        List<QuizQuestionDto> questions = (List<QuizQuestionDto>) session.getAttribute("quizQuestions");

        if (questions == null) {
            return "redirect:/custom-lessons/" + lessonId + "/view";
        }

        // Thu thập đáp án
        List<String> answersList = new ArrayList<>();
        for (int i = 0; i < questions.size(); i++) {
            String answer = params.get("answer_" + i);
            answersList.add(answer != null ? answer : "");
        }

        // Evaluate
        QuizResultDto result = quizService.evaluate(userId, questions, answersList);

        // Lưu kết quả quiz
        QuizMode quizMode = (QuizMode) session.getAttribute("quizMode");
        if (quizMode == null) quizMode = QuizMode.WORD_PRON_TO_MEANING;
        
        quizService.savePersonalLessonAttempt(userId, lessonId, quizMode, result, "REVIEW_TEST");
        
        // Cập nhật review schedule
        lessonService.updateLessonReviewScheduleById(lessonId, result.getScore(), result.getTotal());

        // Clear session
        session.removeAttribute("quizQuestions");
        session.removeAttribute("quizMode");
        session.removeAttribute("customLessonId");

        // Convert answersList to Map for template
        Map<Integer, String> answersMap = new HashMap<>();
        for (int i = 0; i < answersList.size(); i++) {
            if (answersList.get(i) != null && !answersList.get(i).isBlank()) {
                answersMap.put(i, answersList.get(i));
            }
        }

        model.addAttribute("lang", lesson.getLanguageCode());
        model.addAttribute("level", lesson.getLevel());
        model.addAttribute("lessonNo", lessonId);
        model.addAttribute("questions", questions);
        model.addAttribute("answers", answersMap);
        model.addAttribute("score", result.getScore());
        model.addAttribute("total", result.getTotal());
        model.addAttribute("isCustomLesson", true);

        return "quiz_result";
    }
}

// com.example.appNN.controller.QuizController
package com.example.appNN.controller;

import com.example.appNN.dto.QuizQuestionDto;
import com.example.appNN.dto.QuizResultDto;
import com.example.appNN.entity.LessonQuizAttemptEntity;
import com.example.appNN.model.QuizMode;
import com.example.appNN.service.LessonService;
import com.example.appNN.service.QuizService;
import com.example.appNN.service.UserVocabStatsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpSession;

import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
@RequestMapping("/lessons")
public class QuizController {

    private final QuizService quizService;
    private final UserVocabStatsService userVocabStatsService;
    private final LessonService lessonService;

    // Tạm hard-code user 1 (giống LessonController)
    private Long getCurrentUserId() {
        return 1L;
    }

    /**
     * Bắt đầu quiz cho 1 bài học.
     * mode: 1,2,3 tương ứng 3 kiểu bạn mô tả.
     */
    @GetMapping("/{lessonId}/quiz")
    public String startQuiz(@PathVariable Long lessonId,
                            @RequestParam(name = "mode", defaultValue = "1") int mode,                            @RequestParam(name = "isReviewTest", defaultValue = "false") boolean isReviewTest,                            Model model,
                            HttpSession session) {

        QuizMode quizMode;
        switch (mode) {
            case 1 -> quizMode = QuizMode.WORD_PRON_TO_MEANING;
            case 2 -> quizMode = QuizMode.WORD_TO_MEANING;
            case 3 -> quizMode = QuizMode.MEANING_TO_WORD;
            case 4 -> quizMode = QuizMode.FILL_IN;
            default -> quizMode = QuizMode.WORD_PRON_TO_MEANING;
        }

        List<QuizQuestionDto> questions =
                quizService.buildQuizForLesson(lessonId, quizMode);

        // Lưu vào session để chấm điểm
        session.setAttribute("quizQuestions", questions);
        session.setAttribute("quizMode", quizMode);
        session.setAttribute("lessonId", lessonId);

        model.addAttribute("lessonId", lessonId);
        model.addAttribute("questions", questions);
        model.addAttribute("mode", mode);
        model.addAttribute("isPractice", !isReviewTest); // Nếu là reviewTest thì không phải practice

        // Mode 4 sử dụng template khác (hiển thị tất cả câu hỏi)
        if (mode == 4) {
            return "quiz_fill";
        }
        return "quiz_do";  // quiz_do.html
    }

    /**
     * Nộp bài quiz.
     */

    @PostMapping("/{lessonId}/quiz/submit")
    public String submitQuiz(@PathVariable Long lessonId,
                             @RequestParam(name = "isPractice", defaultValue = "false") boolean isPractice,
                             @RequestParam Map<String, String> params,
                             Model model,
                             HttpSession session) {

        @SuppressWarnings("unchecked")
        List<QuizQuestionDto> questions = (List<QuizQuestionDto>) session.getAttribute("quizQuestions");
        QuizMode mode = (QuizMode) session.getAttribute("quizMode");

        if (questions == null || mode == null) {
            return "redirect:/lessons/" + lessonId;
        }

        // Build lại list answers theo thứ tự câu hỏi
        List<String> answers = new java.util.ArrayList<>();
        for (int i = 0; i < questions.size(); i++) {
            String key = "answer" + i;
            String val = params.get(key);
            answers.add(val); // có thể null nếu người dùng không chọn, nhưng bạn đang để required nên ổn
        }

        System.out.println("[DEBUG] answers = " + answers);
        Long userId = getCurrentUserId(); // tạm 1L

        // ✅ CẬP NHẬT THỐNG KÊ TỪNG CÂU
        for (int i = 0; i < questions.size(); i++) {
            QuizQuestionDto q = questions.get(i);
            String ans = answers.get(i);
            boolean correct = q.getCorrectAnswer().equals(ans);

            userVocabStatsService.updateStats(userId, q.getVocabId(), correct);
        }

        // Chấm điểm
        QuizResultDto result = quizService.evaluate(userId, questions, answers);

        // Xác định loại bài kiểm tra
        String attemptType = isPractice ? "PRACTICE" : "REVIEW_TEST";
        
        System.out.println("[DEBUG] isPractice from request param: " + isPractice);
        System.out.println("[DEBUG] attemptType: " + attemptType);

        quizService.saveAttempt(userId, lessonId, mode, result, attemptType);
        
        // Chỉ cập nhật review schedule nếu là REVIEW_TEST
        if ("REVIEW_TEST".equals(attemptType)) {
            lessonService.updateLessonReviewScheduleById(
                    lessonId,
                    result.getScore(), result.getTotal()
            );
        }
        // truyền ra view
        model.addAttribute("lessonId", lessonId);
        model.addAttribute("questions", questions);
        model.addAttribute("answers", answers);
        model.addAttribute("score", result.getScore());
        model.addAttribute("total", result.getTotal());

        session.removeAttribute("quizQuestions");
        session.removeAttribute("quizMode");

        return "quiz_result";
    }


    @GetMapping("/{lessonId}/quiz/history")
    public String viewQuizHistory(@PathVariable Long lessonId, Model model) {

        List<LessonQuizAttemptEntity> attempts =
                quizService.getAttemptsForLesson(lessonId);

        model.addAttribute("lessonId", lessonId);
        model.addAttribute("attempts", attempts);

        return "quiz_history"; // template mới
    }

    @GetMapping("/{lessonId}/quiz/practice-wrong")
    public String practiceWrong(@PathVariable Long lessonId,
                                @RequestParam(name = "mode", defaultValue = "1") int mode,
                                Model model,
                                HttpSession session) {

        Long userId = getCurrentUserId();

        QuizMode quizMode = switch (mode) {
            case 2 -> QuizMode.WORD_TO_MEANING;
            case 3 -> QuizMode.MEANING_TO_WORD;
            case 4 -> QuizMode.FILL_IN;
            default -> QuizMode.WORD_PRON_TO_MEANING;
        };

        List<QuizQuestionDto> questions =
                quizService.buildPracticeFromWrong(userId, lessonId, quizMode);

        if (questions.isEmpty()) {
            // không có từ sai → quay lại bài
            return "redirect:/lessons/" + lessonId;
        }

        session.setAttribute("quizQuestions", questions);
        session.setAttribute("quizMode", quizMode);
        session.setAttribute("isPractice", true); // Đánh dấu là ôn tập
        session.setAttribute("lessonId", lessonId);
        
        System.out.println("[DEBUG] Set isPractice=true in practiceWrong");

        model.addAttribute("lessonId", lessonId);
        model.addAttribute("questions", questions);
        model.addAttribute("mode", mode);
        model.addAttribute("isPractice", true); // Truyền vào template
        model.addAttribute("practiceWrong", true);

        return "quiz_do";
    }



}

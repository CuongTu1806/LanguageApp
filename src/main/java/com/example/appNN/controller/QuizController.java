// com.example.appNN.controller.QuizController
package com.example.appNN.controller;

import com.example.appNN.dto.QuizQuestionDto;
import com.example.appNN.dto.QuizResultDto;
import com.example.appNN.entity.LessonQuizAttemptEntity;
import com.example.appNN.model.QuizMode;
import com.example.appNN.service.QuizService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpSession;

import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
@RequestMapping("/courses")
public class QuizController {

    private final QuizService quizService;

    // Tạm hard-code user 1 (giống LessonController)
    private Long getCurrentUserId() {
        return 1L;
    }

    /**
     * Bắt đầu quiz cho 1 bài học.
     * mode: 1,2,3 tương ứng 3 kiểu bạn mô tả.
     */
    @GetMapping("/{lang}/{level}/lessons/{lessonNo}/quiz")
    public String startQuiz(@PathVariable String lang,
                            @PathVariable String level,
                            @PathVariable Integer lessonNo,
                            @RequestParam(name = "mode", defaultValue = "1") int mode,
                            Model model,
                            HttpSession session) {

        Long userId = getCurrentUserId();

        QuizMode quizMode;
        switch (mode) {
            case 1 -> quizMode = QuizMode.WORD_PRON_TO_MEANING;
            case 2 -> quizMode = QuizMode.WORD_TO_MEANING;
            case 3 -> quizMode = QuizMode.MEANING_TO_WORD;
            default -> quizMode = QuizMode.WORD_PRON_TO_MEANING;
        }

        List<QuizQuestionDto> questions =
                quizService.buildQuizForLesson(userId, lang, level, lessonNo, quizMode);

        // Lưu vào session để chấm điểm
        session.setAttribute("quizQuestions", questions);
        session.setAttribute("quizMode", quizMode);

        model.addAttribute("lang", lang);
        model.addAttribute("level", level);
        model.addAttribute("lessonNo", lessonNo);
        model.addAttribute("questions", questions);
        model.addAttribute("mode", mode);

        return "quiz_do";  // quiz_do.html
    }

    /**
     * Nộp bài quiz.
     */

    @PostMapping("/{lang}/{level}/lessons/{lessonNo}/quiz/submit")
    public String submitQuiz(@PathVariable String lang,
                             @PathVariable String level,
                             @PathVariable Integer lessonNo,
                             @RequestParam Map<String, String> params,
                             Model model,
                             HttpSession session) {

        @SuppressWarnings("unchecked")
        List<QuizQuestionDto> questions =
                (List<QuizQuestionDto>) session.getAttribute("quizQuestions");
        QuizMode mode = (QuizMode) session.getAttribute("quizMode");

        if (questions == null || mode == null) {
            return "redirect:/courses/" + lang + "/" + level + "/lessons/" + lessonNo;
        }

        // Build lại list answers theo thứ tự câu hỏi
        List<String> answers = new java.util.ArrayList<>();
        for (int i = 0; i < questions.size(); i++) {
            String key = "answer" + i;
            String val = params.get(key);
            answers.add(val); // có thể null nếu người dùng không chọn, nhưng bạn đang để required nên ổn
        }

        System.out.println("[DEBUG] answers = " + answers);

        // Chấm điểm
        QuizResultDto result = quizService.evaluate(questions, answers);

        Long userId = getCurrentUserId(); // tạm 1L
        quizService.saveAttempt(userId, lang, level, lessonNo, mode, result);

        model.addAttribute("lang", lang);
        model.addAttribute("level", level);
        model.addAttribute("lessonNo", lessonNo);
        model.addAttribute("questions", questions);
        model.addAttribute("answers", answers);
        model.addAttribute("score", result.getScore());
        model.addAttribute("total", result.getTotal());

        session.removeAttribute("quizQuestions");
        session.removeAttribute("quizMode");

        return "quiz_result";
    }


    @GetMapping("/{lang}/{level}/lessons/{lessonNo}/quiz/history")
    public String viewQuizHistory(@PathVariable String lang,
                                  @PathVariable String level,
                                  @PathVariable Integer lessonNo,
                                  Model model) {

        Long userId = getCurrentUserId();
        List<LessonQuizAttemptEntity> attempts =
                quizService.getAttemptsForLesson(userId, lang, level, lessonNo);

        model.addAttribute("lang", lang);
        model.addAttribute("level", level);
        model.addAttribute("lessonNo", lessonNo);
        model.addAttribute("attempts", attempts);

        return "quiz_history"; // template mới
    }

    @GetMapping("/{lang}/{level}/lessons/{lessonNo}/quiz/practice-wrong")
    public String practiceWrong(@PathVariable String lang,
                                @PathVariable String level,
                                @PathVariable Integer lessonNo,
                                @RequestParam(name = "mode", defaultValue = "1") int mode,
                                Model model,
                                HttpSession session) {

        Long userId = getCurrentUserId();

        QuizMode quizMode = switch (mode) {
            case 2 -> QuizMode.WORD_TO_MEANING;
            case 3 -> QuizMode.MEANING_TO_WORD;
            default -> QuizMode.WORD_PRON_TO_MEANING;
        };

        List<QuizQuestionDto> questions =
                quizService.buildPracticeFromWrong(userId, lang, level, lessonNo, quizMode);

        if (questions.isEmpty()) {
            // không có từ sai → quay lại bài
            return "redirect:/courses/" + lang + "/" + level + "/lessons/" + lessonNo;
        }

        session.setAttribute("quizQuestions", questions);
        session.setAttribute("quizMode", quizMode);

        model.addAttribute("lang", lang);
        model.addAttribute("level", level);
        model.addAttribute("lessonNo", lessonNo);
        model.addAttribute("questions", questions);
        model.addAttribute("mode", mode);
        model.addAttribute("practiceWrong", true);

        return "quiz_do";
    }



}

// com.example.appNN.service.QuizService
package com.example.appNN.service;

import com.example.appNN.dto.QuizQuestionDto;
import com.example.appNN.dto.QuizResultDto;
import com.example.appNN.entity.*;
import com.example.appNN.model.QuizMode;
import com.example.appNN.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
public class QuizService {

    private final LessonService lessonService;
    private final VocabularyRepository vocabularyRepository;
    private final LessonRepository lessonRepository;
    private final AppUserRepository appUserRepository;
    private final LessonQuizAttemptRepository attemptRepository;
    private final LessonQuizWrongVocabRepository wrongVocabRepository;

    /**
     * Tạo danh sách câu hỏi quiz cho 1 bài học.
     */
    public List<QuizQuestionDto> buildQuizForLesson(Long userId,
                                                    String lang,
                                                    String level,
                                                    Integer lessonNo,
                                                    QuizMode mode) {

        // Từ của bài này
        List<VocabularyEntity> lessonWords =
                lessonService.getLessonWords(userId, lang, level, lessonNo);

        // Toàn bộ từ trong course (để chọn đáp án sai)
        List<VocabularyEntity> pool =
                vocabularyRepository.findByLanguageCodeAndLevel(lang, level);

        Random random = new Random();
        List<QuizQuestionDto> questions = new ArrayList<>();

        for (VocabularyEntity v : lessonWords) {
            QuizQuestionDto q = new QuizQuestionDto();
            q.setVocabId(v.getId());

            // Xác định câu hỏi + đáp án đúng theo mode
            String questionText;
            String correct;

            switch (mode) {
                case WORD_PRON_TO_MEANING:
                    // C1: word + pronunciation -> meaning
                    questionText = v.getWord()
                            + (v.getPronunciation() != null && !v.getPronunciation().isBlank()
                            ? " (" + v.getPronunciation() + ")"
                            : "");
                    correct = v.getMeaning();
                    break;

                case WORD_TO_MEANING:
                    // C2: word -> meaning
                    questionText = v.getWord();
                    correct = v.getMeaning();
                    break;

                case MEANING_TO_WORD:
                    // C3: meaning -> word
                    questionText = v.getMeaning();
                    correct = v.getWord();
                    break;

                default:
                    questionText = v.getWord();
                    correct = v.getMeaning();
            }

            q.setQuestionText(questionText);
            q.setCorrectAnswer(correct);

            // Tạo 4 lựa chọn
            Set<String> optionsSet = new LinkedHashSet<>();
            optionsSet.add(correct);

            while (optionsSet.size() < 4 && optionsSet.size() < pool.size()) {
                VocabularyEntity candidate = pool.get(random.nextInt(pool.size()));
                if (Objects.equals(candidate.getId(), v.getId())) continue; // tránh chính nó

                String wrong;
                switch (mode) {
                    case MEANING_TO_WORD:
                        wrong = candidate.getWord();
                        break;
                    default:
                        wrong = candidate.getMeaning();
                }

                if (wrong != null && !wrong.isBlank()) {
                    optionsSet.add(wrong);
                }
            }

            List<String> options = new ArrayList<>(optionsSet);
            Collections.shuffle(options, random);
            q.setOptions(options);

            questions.add(q);
        }

        return questions;
    }

    /**
     * Chấm điểm: so sánh đáp án người dùng với correctAnswer.
     */
    public int score(List<QuizQuestionDto> questions, List<String> answers) {
        int correctCount = 0;
        for (int i = 0; i < questions.size() && i < answers.size(); i++) {
            if (Objects.equals(questions.get(i).getCorrectAnswer(), answers.get(i))) {
                correctCount++;
            }
        }
        return correctCount;
    }

    // chấm điểm + lấy danh sách từ sai
    public QuizResultDto evaluate(List<QuizQuestionDto> questions, List<String> answers) {
        int correctCount = 0;
        List<Long> wrongIds = new java.util.ArrayList<>();

        for (int i = 0; i < questions.size() && i < answers.size(); i++) {
            QuizQuestionDto q = questions.get(i);
            String userAns = answers.get(i);

            if (java.util.Objects.equals(q.getCorrectAnswer(), userAns)) {
                correctCount++;
            } else {
                wrongIds.add(q.getVocabId());
            }
        }
        return new QuizResultDto(correctCount, questions.size(), wrongIds);
    }

    @Transactional
    public LessonQuizAttemptEntity saveAttempt(Long userId,
                                               String lang,
                                               String level,
                                               Integer lessonNo,
                                               QuizMode mode,
                                               QuizResultDto result) {

        AppUserEntity user = appUserRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));

        LessonEntity lesson = lessonRepository
                .findByUserIdAndLanguageCodeAndLevelAndLessonIndex(userId, lang, level, lessonNo);

        if (lesson == null) {
            throw new RuntimeException("Lesson not found for quiz");
        }

        LessonQuizAttemptEntity attempt = new LessonQuizAttemptEntity();
        attempt.setUser(user);
        attempt.setLesson(lesson);
        attempt.setMode(mode.name());
        attempt.setScore(result.getScore());
        attempt.setTotal(result.getTotal());
        attempt.setCreatedAt(java.time.LocalDateTime.now());

        attempt = attemptRepository.save(attempt);

        // Lưu các từ sai
        for (Long vocabId : result.getWrongVocabIds()) {
            VocabularyEntity vocab = vocabularyRepository.findById((long) vocabId.intValue())
                    .orElse(null);   // tuỳ bạn đã sửa id sang Long chưa

            if (vocab == null) continue;

            LessonQuizWrongVocabId id = new LessonQuizWrongVocabId(attempt.getId(), vocab.getId());
            LessonQuizWrongVocabEntity w = new LessonQuizWrongVocabEntity();
            w.setId(id);
            w.setAttempt(attempt);
            w.setVocabulary(vocab);

            wrongVocabRepository.save(w);
        }

        return attempt;
    }

    // Lấy lịch sử theo bài
    public java.util.List<LessonQuizAttemptEntity> getAttemptsForLesson(
            Long userId, String lang, String level, Integer lessonNo) {
        return attemptRepository.findByUserAndLesson(userId, lang, level, lessonNo);
    }

    // Luyện tập các từ sai trong bài
    public java.util.List<QuizQuestionDto> buildPracticeFromWrong(
            Long userId, String lang, String level, Integer lessonNo, QuizMode mode) {

        java.util.List<Long> wrongIds =
                wrongVocabRepository.findDistinctWrongVocabIdsForLesson(userId, lang, level, lessonNo);

        if (wrongIds.isEmpty()) {
            return java.util.List.of();
        }

        List<VocabularyEntity> wrongWords = vocabularyRepository.findAllById(wrongIds);

        // pool để lấy đáp án sai vẫn là toàn bộ từ trong course
        java.util.List<VocabularyEntity> pool =
                vocabularyRepository.findByLanguageCodeAndLevel(lang, level);

        java.util.List<QuizQuestionDto> questions = new java.util.ArrayList<>();
        java.util.Random random = new java.util.Random();

        for (VocabularyEntity v : wrongWords) {
            QuizQuestionDto q = new QuizQuestionDto();
            q.setVocabId(v.getId());

            String questionText;
            String correct;

            switch (mode) {
                case MEANING_TO_WORD -> {
                    questionText = v.getMeaning();
                    correct = v.getWord();
                }
                case WORD_TO_MEANING -> {
                    questionText = v.getWord();
                    correct = v.getMeaning();
                }
                case WORD_PRON_TO_MEANING -> {
                    questionText = v.getWord()
                            + ((v.getPronunciation() != null && !v.getPronunciation().isBlank())
                            ? " (" + v.getPronunciation() + ")" : "");
                    correct = v.getMeaning();
                }
                default -> {
                    questionText = v.getWord();
                    correct = v.getMeaning();
                }
            }

            q.setQuestionText(questionText);
            q.setCorrectAnswer(correct);

            java.util.Set<String> opts = new java.util.LinkedHashSet<>();
            opts.add(correct);

            while (opts.size() < 4 && opts.size() < pool.size()) {
                VocabularyEntity c = pool.get(random.nextInt(pool.size()));
                if (java.util.Objects.equals(c.getId(), v.getId())) continue;
                String wrong;
                switch (mode) {
                    case MEANING_TO_WORD -> wrong = c.getWord();
                    default -> wrong = c.getMeaning();
                }
                if (wrong != null && !wrong.isBlank()) opts.add(wrong);
            }

            java.util.List<String> options = new java.util.ArrayList<>(opts);
            java.util.Collections.shuffle(options, random);
            q.setOptions(options);

            questions.add(q);
        }

        return questions;
    }
}

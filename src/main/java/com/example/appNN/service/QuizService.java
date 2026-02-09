// com.example.appNN.service.QuizService
package com.example.appNN.service;

import com.example.appNN.dto.QuizQuestionDto;
import com.example.appNN.dto.QuizResultDto;
import com.example.appNN.dto.VocabularyDto;
import com.example.appNN.entity.*;
import com.example.appNN.model.QuizMode;
import com.example.appNN.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
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
    private final UserVocabularyRepository userVocabularyRepository;
    private final LessonVocabularyRepository lessonVocabularyRepository;

    /**
     * Tạo danh sách câu hỏi quiz cho 1 bài học.
     */
    public List<QuizQuestionDto> buildQuizForLesson(Long lessonId, QuizMode mode) {
        // Lấy lesson để biết languageCode
        LessonEntity lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new RuntimeException("Lesson not found: " + lessonId));
        
        // Từ của bài này
        List<VocabularyDto> lessonWords = lessonService.getLessonWords(lessonId);

        // Toàn bộ từ trong course (để chọn đáp án sai)
        List<VocabularyEntity> pool =
                vocabularyRepository.findByLanguageCode(lesson.getLanguageCode());

        Random random = new Random();
        List<QuizQuestionDto> questions = new ArrayList<>();

        for (VocabularyDto v : lessonWords) {
            QuizQuestionDto q = new QuizQuestionDto();
            q.setVocabId(v.getRealVocabId());

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
                case FILL_IN:
                    // C3: meaning -> word
                    // C4: meaning -> word (fill in)
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
                // Tránh trùng với từ hiện tại - so sánh với ID thật của vocab
                if (Objects.equals(candidate.getId(), v.getSystemVocabId())) continue;

                String wrong;
                switch (mode) {
                    case MEANING_TO_WORD:
                    case FILL_IN:
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
    @Autowired
    private UserVocabStatsService userVocabStatsService;

    public QuizResultDto evaluate(Long userId,
                                  List<QuizQuestionDto> questions,
                                  List<String> answers) {

        int correctCount = 0;
        List<Long> wrongIds = new ArrayList<>();

        for (int i = 0; i < questions.size(); i++) {

            QuizQuestionDto q = questions.get(i);
            String ans = answers.get(i);

            boolean correct = q.getCorrectAnswer().equals(ans);

            if (!correct) {
                wrongIds.add(q.getVocabId());
            }
            if (correct) correctCount++;

            userVocabStatsService.updateStats(userId, q.getVocabId(), correct);
        }

        return new QuizResultDto(correctCount, questions.size(), wrongIds);
    }


    @Transactional
    public LessonQuizAttemptEntity saveAttempt(Long userId,
                                               Long lessonId,
                                               QuizMode mode,
                                               QuizResultDto result,
                                               String attemptType) {

        AppUserEntity user = appUserRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));

        LessonEntity lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new RuntimeException("Lesson not found: " + lessonId));

        LessonQuizAttemptEntity attempt = new LessonQuizAttemptEntity();
        attempt.setUser(user);
        attempt.setLesson(lesson);
        attempt.setMode(mode.name());
        attempt.setAttemptType(attemptType); // REVIEW_TEST hoặc PRACTICE
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
    
    // Lấy lịch sử theo lessonId
    public java.util.List<LessonQuizAttemptEntity> getAttemptsForLesson(Long lessonId) {
        return attemptRepository.findByLessonId(lessonId);
    }

    // Luyện tập các từ sai trong bài
    public java.util.List<QuizQuestionDto> buildPracticeFromWrong(
            Long userId, Long lessonId, QuizMode mode) {
        
        // Lấy lesson để biết languageCode
        LessonEntity lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new RuntimeException("Lesson not found: " + lessonId));

        java.util.List<Long> wrongIds =
                wrongVocabRepository.findDistinctWrongVocabIdsForLessonId(lessonId);

        if (wrongIds.isEmpty()) {
            return java.util.List.of();
        }

        List<VocabularyEntity> wrongWords = vocabularyRepository.findAllById(wrongIds);

        // pool để lấy đáp án sai vẫn là toàn bộ từ trong course
        java.util.List<VocabularyEntity> pool =
                vocabularyRepository.findByLanguageCode(lesson.getLanguageCode());

        java.util.List<QuizQuestionDto> questions = new java.util.ArrayList<>();
        java.util.Random random = new java.util.Random();

        for (VocabularyEntity v : wrongWords) {
            QuizQuestionDto q = new QuizQuestionDto();
            q.setVocabId(v.getId());

            String questionText;
            String correct;

            switch (mode) {
                case MEANING_TO_WORD, FILL_IN -> {
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
                    case MEANING_TO_WORD, FILL_IN -> wrong = c.getWord();
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

    /**
     * Tạo quiz cho personal lesson (thay thế buildQuizForCustomLesson)
     */
    public List<QuizQuestionDto> buildQuizForPersonalLesson(Long userId, LessonEntity lesson, QuizMode mode) {
        // Lấy tất cả vocabulary của personal lesson từ LessonService
        List<VocabularyDto> vocabDtos = lessonService.getAllVocabulariesForLessonAsDto(lesson.getId());
        
        if (vocabDtos.isEmpty()) {
            return new ArrayList<>();
        }

        // Toàn bộ từ trong cùng languageCode và level để chọn đáp án sai
        List<VocabularyEntity> pool = vocabularyRepository.findByLanguageCode(
            lesson.getLanguageCode()
        );

        Random random = new Random();
        List<QuizQuestionDto> questions = new ArrayList<>();

        for (VocabularyDto dto : vocabDtos) {
            QuizQuestionDto q = new QuizQuestionDto();
            q.setVocabId(dto.getId());

            // Xác định câu hỏi + đáp án đúng theo mode
            String questionText;
            String correct;

            switch (mode) {
                case WORD_PRON_TO_MEANING:
                    questionText = dto.getWord()
                            + (dto.getPronunciation() != null && !dto.getPronunciation().isBlank()
                            ? " (" + dto.getPronunciation() + ")"
                            : "");
                    correct = dto.getMeaning();
                    break;

                case WORD_TO_MEANING:
                    questionText = dto.getWord();
                    correct = dto.getMeaning();
                    break;

                case MEANING_TO_WORD:
                case FILL_IN:
                    questionText = dto.getMeaning();
                    correct = dto.getWord();
                    break;

                default:
                    questionText = dto.getWord();
                    correct = dto.getMeaning();
            }

            q.setQuestionText(questionText);
            q.setCorrectAnswer(correct);

            // Tạo 4 lựa chọn
            Set<String> optionsSet = new LinkedHashSet<>();
            optionsSet.add(correct);

            while (optionsSet.size() < 4 && optionsSet.size() < pool.size()) {
                VocabularyEntity candidate = pool.get(random.nextInt(pool.size()));
                if (Objects.equals(candidate.getId(), dto.getId())) continue;

                String wrong;
                switch (mode) {
                    case MEANING_TO_WORD:
                    case FILL_IN:
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
     * Lưu kết quả quiz cho personal lesson (thay thế saveCustomLessonAttempt)
     */
    @Transactional
    public LessonQuizAttemptEntity savePersonalLessonAttempt(Long userId,
                                                              Long lessonId,
                                                              QuizMode mode,
                                                              QuizResultDto result,
                                                              String attemptType) {

        AppUserEntity user = appUserRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));

        LessonEntity lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new RuntimeException("Lesson not found: " + lessonId));

        LessonQuizAttemptEntity attempt = new LessonQuizAttemptEntity();
        attempt.setUser(user);
        attempt.setLesson(lesson);
        attempt.setMode(mode.name());
        attempt.setAttemptType(attemptType); // REVIEW_TEST hoặc PRACTICE
        attempt.setScore(result.getScore());
        attempt.setTotal(result.getTotal());
        attempt.setCreatedAt(java.time.LocalDateTime.now());

        attempt = attemptRepository.save(attempt);

        // Lưu các từ sai
        for (Long vocabId : result.getWrongVocabIds()) {
            VocabularyEntity vocab = vocabularyRepository.findById(vocabId)
                    .orElse(null);

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
}

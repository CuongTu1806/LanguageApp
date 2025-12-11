package com.example.appNN.service;

import com.example.appNN.entity.AppUserEntity;
import com.example.appNN.entity.LessonEntity;
import com.example.appNN.entity.VocabularyEntity;
import com.example.appNN.repository.AppUserRepository;
import com.example.appNN.repository.LessonRepository;
import com.example.appNN.repository.VocabularyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class LessonService {

    private final LessonRepository lessonRepository;
    private final VocabularyRepository vocabularyRepository;
    private final AppUserRepository appUserRepository;

    /**
     * Tạo bài mới cho user với lang + level + số từ mong muốn.
     * @return lessonIndex mới (Bài số mấy), hoặc null nếu không còn từ.
     */
    @Transactional
    public Integer createNewLesson(Long userId, String languageCode, String level, int size) {

        // 1. Lấy user
        AppUserEntity user = appUserRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User không tồn tại: " + userId));

        // 2. Xác định lesson_index tiếp theo
        Integer maxIndex = lessonRepository.findMaxLessonIndex(userId, languageCode, level);
        int nextIndex = (maxIndex == null ? 1 : maxIndex + 1);

        // 3. Random các từ chưa từng có trong bài nào của user
        List<VocabularyEntity> candidates =
                vocabularyRepository.findRandomUnlearnedForUser(userId, languageCode, level, size);

        if (candidates.isEmpty()) {
            return null; // Không còn từ để tạo bài mới
        }

        // 4. Tạo lesson mới
        LessonEntity lesson = new LessonEntity();
        lesson.setUser(user);
        lesson.setLanguageCode(languageCode);
        lesson.setLevel(level);
        lesson.setLessonIndex(nextIndex);
        lesson.setCreatedAt(LocalDateTime.now());
        lesson.setReviewStage(0);  // Bài mới bắt đầu từ stage 0
        lesson.getVocabularies().addAll(candidates);
        lesson.setVocabularyCount(candidates.size()); // Set số lượng từ

        lessonRepository.save(lesson);

        return nextIndex;
    }

    public List<Integer> listLessons(Long userId, String lang, String level) {
        List<LessonEntity> lessons =
                lessonRepository.findByUserIdAndLanguageCodeAndLevelOrderByLessonIndex(userId, lang, level);
        List<Integer> result = new ArrayList<>();
        for (LessonEntity l : lessons) {
            result.add(l.getLessonIndex());
        }
        return result;
    }

    @Transactional(readOnly = true)
    public List<VocabularyEntity> getLessonWords(Long userId, String lang, String level, Integer lessonIndex) {
        LessonEntity lesson =
                lessonRepository.findByUserIdAndLanguageCodeAndLevelAndLessonIndex(userId, lang, level, lessonIndex);
        if (lesson == null) {
            System.out.println("[DEBUG] getLessonWords: Lesson not found for userId=" + userId + 
                ", lang=" + lang + ", level=" + level + ", lessonIndex=" + lessonIndex);
            return List.of();
        }

        // Force initialize the collection within transaction
        Set<VocabularyEntity> vocabs = lesson.getVocabularies();
        int actualSize = vocabs.size(); // This triggers lazy loading
        
        // Log lesson info
        System.out.println("[DEBUG] getLessonWords: Found lesson id=" + lesson.getId() + 
            ", vocabularyCount field=" + lesson.getVocabularyCount() +
            ", actual vocabularies size=" + actualSize);
        
        // Sắp xếp theo id cho dễ nhìn
        List<VocabularyEntity> list = new ArrayList<>(vocabs);
        
        // Log first few words
        if (!list.isEmpty()) {
            System.out.println("[DEBUG] First 3 words in lesson:");
            for (int i = 0; i < Math.min(3, list.size()); i++) {
                VocabularyEntity v = list.get(i);
                System.out.println("  - id=" + v.getId() + ", word=" + v.getWord() + ", meaning=" + v.getMeaning());
            }
        } else {
            System.out.println("[DEBUG] WARNING: vocabularies collection is empty but vocabularyCount=" + lesson.getVocabularyCount());
        }
        
        list.sort(Comparator.comparing(VocabularyEntity::getId));
        return list;
    }

    @Transactional
    public void updateLessonReviewSchedule(Long userId,
                                           String languageCode,
                                           String level,
                                           Integer lessonIndex,
                                           int score,
                                           int total) {

        LessonEntity lesson = lessonRepository
                .findByUserIdAndLanguageCodeAndLevelAndLessonIndex(
                        userId, languageCode, level, lessonIndex);

        if (lesson == null) {
            throw new RuntimeException("Không tìm thấy lesson cho user="
                    + userId + ", lang=" + languageCode + ", level=" + level
                    + ", lessonIndex=" + lessonIndex);
        }

        // Nếu chưa có lịch thì thôi (có thể set lịch ban đầu ở createNewLesson)
        LocalDateTime due = lesson.getNextReviewAt();
        if (due == null) {
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDate today = now.toLocalDate();
        LocalDate dueDate = due.toLocalDate();

        // ❗ CHỈ xử lý tăng/giảm bậc nếu HÔM NAY là NGÀY ĐẾN HẠN
        if (!today.equals(dueDate)) {
            // Không đúng ngày ôn định kì → không thay đổi stage/next_review
            return;
        }

        // Từ đây trở đi là "đúng ngày ôn", mới xét điểm để điều chỉnh
        double rate = total > 0 ? (double) score / total : 0.0;

        Integer currentStage = lesson.getReviewStage();
        if (currentStage == null) currentStage = 0;

        if (rate < 0.5) {
            // Sai nhiều: hạ 1 bậc và cho ôn lại sớm (ví dụ: ngày mai)
            int newStage = Math.max(0, currentStage - 1);
            lesson.setReviewStage(newStage);
            lesson.setNextReviewAt(now.plusDays(1));
        } else {
            // Đúng ổn: tăng bậc theo lịch bạn định nghĩa
            int newStage = currentStage + 1;
            lesson.setReviewStage(newStage);

            LocalDateTime next;
            switch (newStage) {
                case 1 -> next = now.plusDays(3);   // sau lần ôn 1: +3 ngày
                case 2 -> next = now.plusDays(7);   // sau lần ôn 2: +7 ngày
                case 3 -> next = now.plusDays(14);  // sau lần ôn 3: +14 ngày
                default -> next = now.plusDays(14); // từ lần 4 trở đi: 2 tuần/lần
            }
            lesson.setNextReviewAt(next);
        }

        lessonRepository.save(lesson);
    }

    public List<LessonEntity> getDueLessons(Long userId) {
        LocalDateTime now = LocalDateTime.now();
        return lessonRepository.findDueLessons(userId, now);
    }

    /**
     * Tìm kiếm lesson theo số lượng từ
     */
    public List<LessonEntity> findLessonsByVocabCount(Long userId, Integer minCount, Integer maxCount) {
        if (minCount != null && maxCount != null) {
            return lessonRepository.findByUserIdAndVocabularyCountBetween(userId, minCount, maxCount);
        } else if (minCount != null) {
            return lessonRepository.findByUserIdAndVocabularyCountGreaterThanEqual(userId, minCount);
        } else if (maxCount != null) {
            return lessonRepository.findByUserIdAndVocabularyCountLessThanEqual(userId, maxCount);
        }
        return List.of();
    }



}

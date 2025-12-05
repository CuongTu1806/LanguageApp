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
        lesson.getVocabularies().addAll(candidates);

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

    public List<VocabularyEntity> getLessonWords(Long userId, String lang, String level, Integer lessonIndex) {
        LessonEntity lesson =
                lessonRepository.findByUserIdAndLanguageCodeAndLevelAndLessonIndex(userId, lang, level, lessonIndex);
        if (lesson == null) return List.of();

        // Sắp xếp theo id cho dễ nhìn
        List<VocabularyEntity> list = new ArrayList<>(lesson.getVocabularies());
        list.sort(Comparator.comparing(VocabularyEntity::getId));
        return list;
    }
}

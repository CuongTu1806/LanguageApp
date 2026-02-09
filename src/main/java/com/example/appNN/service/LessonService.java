package com.example.appNN.service;

import com.example.appNN.dto.AddVocabularyRequest;
import com.example.appNN.dto.VocabularyCheckResponse;
import com.example.appNN.dto.VocabularyDto;
import com.example.appNN.entity.*;
import com.example.appNN.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class LessonService {

    private final LessonRepository  lessonRepository;
    private final VocabularyRepository vocabularyRepository;
    private final AppUserRepository appUserRepository;
    private final LessonVocabularyRepository lessonVocabularyRepository;
    private final UserVocabularyRepository userVocabularyRepository;

    /**
     * Tạo bài mới cho user với lang + level + số từ mong muốn.
     * @return lessonIndex mới (Bài số mấy), hoặc null nếu không còn từ.
     */
    @Transactional
    public Integer createNewLesson(Long userId, String languageCode,int size) {

        // 1. Lấy user
        AppUserEntity user = appUserRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User không tồn tại: " + userId));

        // 2. Xác định lesson_index tiếp theo
        Integer maxIndex = lessonRepository.findMaxLessonIndex(userId, languageCode);
        int nextIndex = (maxIndex == null ? 1 : maxIndex + 1);

        // 3. Random các từ chưa từng có trong bài nào của user
        List<VocabularyEntity> candidates =
                vocabularyRepository.findRandomUnlearnedForUserByLanguage(userId, languageCode, size);


        if (candidates.isEmpty()) {
            System.out.println("No unlearned vocabulary found!");
            return null; // Không còn từ để tạo bài mới
        }

        // 4. Tạo lesson mới
        LessonEntity lesson = new LessonEntity();
        lesson.setUser(user);
        lesson.setLanguageCode(languageCode);
        lesson.setLessonIndex(nextIndex);
        lesson.setCreatedAt(LocalDateTime.now());
        lesson.setReviewStage(0);  // Bài mới bắt đầu từ stage 0
        
        // Set hạn ôn lần đầu: 23:59:59 của ngày mai
        LocalDateTime tomorrow = LocalDateTime.now().plusDays(1)
                .toLocalDate()
                .atTime(23, 59, 59);
        lesson.setNextReviewAt(tomorrow);
        
        // Thêm vocabularies vào bảng lesson_vocabulary
        for (VocabularyEntity vocab : candidates) {
            LessonVocabularyEntity mapping = new LessonVocabularyEntity();
            mapping.setLessonId(null); // sẽ được set sau khi save lesson
            mapping.setVocabId(vocab.getId());
            mapping.setUserVocabId(null); // system vocab
            lesson.getVocabularies().add(vocab); // temporary for count
        }
        
        lesson.setVocabularyCount(candidates.size()); // Set số lượng từ
        lesson.setLessonType("system");

        lesson = lessonRepository.save(lesson);
        
        // Sau khi save lesson, save vocabulary mappings
        for (VocabularyEntity vocab : candidates) {
            LessonVocabularyEntity mapping = new LessonVocabularyEntity();
            mapping.setLessonId(lesson.getId());
            mapping.setVocabId(vocab.getId());
            mapping.setUserVocabId(null);
            lessonVocabularyRepository.save(mapping);
        }

        return nextIndex;
    }

    // function: get all Lessons when the first time load page or search all
    public List<Integer> listLessons(Long userId, String lang, String level) {
        List<LessonEntity> lessons =
                lessonRepository.findByUserIdAndLanguageCodeAndLevelOrderByLessonIndex(userId, lang, level);
        List<Integer> result = new ArrayList<>();
        for (LessonEntity l : lessons) {
            result.add(l.getLessonIndex());
        }
        return result;
    }


    // function: get all words in a lesson (by lessonId)
    @Transactional(readOnly = true)
    public List<VocabularyDto> getLessonWords(Long lessonId) {
        // Lấy vocabularies từ lesson_vocabulary table
        List<LessonVocabularyEntity> mappings = lessonVocabularyRepository.findByLessonId(lessonId);
        List<VocabularyDto> result = new ArrayList<>();
        
        for (LessonVocabularyEntity mapping : mappings) {
            if (mapping.getVocabId() != null) {
                // get VocabularyId and put into VocabularyDto
                vocabularyRepository.findById(mapping.getVocabId()).ifPresent(v -> 
                    result.add(VocabularyDto.fromVocabularyEntity(v))
                );
            } else if (mapping.getUserVocabId() != null) {
                // get UserVocabularyId then put VocabularyDto
                userVocabularyRepository.findById(mapping.getUserVocabId()).ifPresent(uv -> 
                    result.add(VocabularyDto.fromUserVocabularyEntity(uv))
                );
            }
        }
        
        log.debug("getLessonWords: Found {} vocabularies for lesson id={}", result.size(), lessonId);
        // Sort theo ID để đảm bảo thứ tự nhất quán
        result.sort(Comparator.comparing(VocabularyDto::getId));
        return result;
    }
    
    // Deprecated: dùng getLessonWords(lessonId) thay thế
    @Deprecated
    @Transactional(readOnly = true)
    public List<VocabularyDto> getLessonWords(Long userId, String lang, Integer lessonIndex) {
        LessonEntity lesson =
                lessonRepository.findByUserIdAndLanguageCodeAndLessonIndex(userId, lang, lessonIndex);
        if (lesson == null) {
            log.debug("getLessonWords: Lesson not found for userId={}, lang={}, lessonIndex={}", 
                userId, lang, lessonIndex);
            return List.of();
        }
        return getLessonWords(lesson.getId());
    }

    
    /**
     * Cập nhật lịch ôn tập cho lesson theo ID (dùng cho personal lesson)
     */
    @Transactional
    public void updateLessonReviewScheduleById(Long lessonId, int score, int total) {
        LessonEntity lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new RuntimeException("Lesson not found: " + lessonId));
        updateReviewScheduleInternal(lesson, score, total);
    }
    
    /**
     * Logic chung cho việc cập nhật review schedule
     */
    private void updateReviewScheduleInternal(LessonEntity lesson, int score, int total) {
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

        if (rate < 0.7) {
            // Dưới 70%: hạ 1 bậc và cho ôn lại sớm (ngày mai 23:59:59)
            int newStage = Math.max(0, currentStage - 1);
            lesson.setReviewStage(newStage);
            LocalDateTime nextReview = now.plusDays(1)
                    .toLocalDate()
                    .atTime(23, 59, 59);
            lesson.setNextReviewAt(nextReview);
        } else {
            // Đúng ổn: tăng bậc theo lịch, set đến 23:59:59 của ngày đó
            int newStage = currentStage + 1;
            lesson.setReviewStage(newStage);

            int daysToAdd;
            switch (newStage) {
                case 1 -> daysToAdd = 3;   // sau lần ôn 1: +3 ngày
                case 2 -> daysToAdd = 7;   // sau lần ôn 2: +7 ngày
                case 3 -> daysToAdd = 14;  // sau lần ôn 3: +14 ngày
                default -> daysToAdd = 14; // từ lần 4 trở đi: 2 tuần/lần
            }
            
            LocalDateTime next = now.plusDays(daysToAdd)
                    .toLocalDate()
                    .atTime(23, 59, 59);
            lesson.setNextReviewAt(next);
        }

        lessonRepository.save(lesson);
    }

    public List<LessonEntity> getDueLessons(Long userId) {
        LocalDateTime now = LocalDateTime.now();
        return lessonRepository.findDueLessons(userId, now);
    }
    
    /**
     * Lấy danh sách bài tập tới hạn (bao gồm cả quá hạn và sắp tới hạn trong N ngày)
     * Hạn được tính đến 23:59:59 của ngày đó
     */
    public List<LessonEntity> getUpcomingDueLessons(Long userId, int daysAhead) {
        // Tính đến 23:59:59 của ngày thứ N
        LocalDateTime deadline = LocalDateTime.now().plusDays(daysAhead)
                .toLocalDate()
                .atTime(23, 59, 59);
        // Lấy tất cả bài có hạn <= deadline (bao gồm cả bài quá hạn)
        return lessonRepository.findAllDueLessons(userId, deadline);
    }
    
    /**
     * Đếm số bài tập chưa làm (đã tới hạn)
     */
    public long countOverdueLessons(Long userId) {
        LocalDateTime now = LocalDateTime.now();
        return lessonRepository.countOverdueLessons(userId, now);
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

    // ========== PERSONAL LESSON METHODS ==========
    
    /**
     * Tạo personal lesson mới
     */
    @Transactional
    public LessonEntity createPersonalLesson(Long userId, String title, String description, 
                                              String languageCode, String level) {
        AppUserEntity user = appUserRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User không tồn tại: " + userId));
        
        LessonEntity lesson = new LessonEntity();
        lesson.setUser(user);
        lesson.setTitle(title);
        lesson.setDescription(description);
        lesson.setLanguageCode(languageCode);
        lesson.setLevel(level);
        lesson.setVocabularyCount(0);
        lesson.setReviewStage(0);
        lesson.setLessonType("personal");
        lesson.setLessonIndex(null); // Personal lesson không cần lesson_index
        
        // Set hạn ôn lần đầu: 23:59:59 của ngày mai
        LocalDateTime tomorrow = LocalDateTime.now().plusDays(1)
                .toLocalDate()
                .atTime(23, 59, 59);
        lesson.setNextReviewAt(tomorrow);
        
        lesson = lessonRepository.save(lesson);
        log.info("Created personal lesson: {} (ID: {}) for user {}", title, lesson.getId(), userId);
        
        return lesson;
    }
    
    /**
     * Lấy danh sách personal lesson của user
     */
    public List<LessonEntity> getPersonalLessons(Long userId) {
        return lessonRepository.findByUserIdAndLessonType(userId, "personal");
    }
    
    /**
     * Thêm vocabulary vào personal lesson
     */
    @Transactional
    public Object addVocabularyToPersonalLesson(AddVocabularyRequest request) {
        LessonEntity lesson = lessonRepository.findById(request.getCustomLessonId())
                .orElseThrow(() -> new RuntimeException("Personal lesson not found"));
        
        if (!"personal".equals(lesson.getLessonType())) {
            throw new RuntimeException("This is not a personal lesson");
        }
        
        Long userId = lesson.getUser().getId();
        
        // Tạo hoặc cập nhật user vocabulary
        Optional<UserVocabularyEntity> existingUserVocab = userVocabularyRepository.findByUserIdAndWordAndLanguageCode(
            userId, request.getWord(), request.getLanguageCode()
        );
        
        UserVocabularyEntity userVocab;
        
        if (existingUserVocab.isPresent()) {
            userVocab = existingUserVocab.get();
            if (request.getPronunciation() != null) userVocab.setPronunciation(request.getPronunciation());
            if (request.getPos() != null) userVocab.setPos(request.getPos());
            if (request.getMeaning() != null) userVocab.setMeaning(request.getMeaning());
            if (request.getExampleSrc() != null) userVocab.setExampleSrc(request.getExampleSrc());
            if (request.getExampleVi() != null) userVocab.setExampleVi(request.getExampleVi());
            if (request.getLevel() != null) userVocab.setLevel(request.getLevel());
            userVocab = userVocabularyRepository.save(userVocab);
            log.info("Updated user vocabulary: {} (ID: {})", userVocab.getWord(), userVocab.getId());
        } else {
            userVocab = new UserVocabularyEntity();
            userVocab.setUserId(userId);
            userVocab.setWord(request.getWord());
            userVocab.setPronunciation(request.getPronunciation());
            userVocab.setPos(request.getPos());
            userVocab.setMeaning(request.getMeaning());
            userVocab.setExampleSrc(request.getExampleSrc());
            userVocab.setExampleVi(request.getExampleVi());
            userVocab.setLanguageCode(request.getLanguageCode());
            userVocab.setLevel(request.getLevel());
            userVocab = userVocabularyRepository.save(userVocab);
            log.info("Created user vocabulary: {} (ID: {})", userVocab.getWord(), userVocab.getId());
        }
        
        // Kiểm tra đã thêm vào lesson chưa
        Optional<LessonVocabularyEntity> existing = lessonVocabularyRepository
            .findByLessonIdAndUserVocabId(lesson.getId(), userVocab.getId());
        
        if (existing.isEmpty()) {
            LessonVocabularyEntity mapping = new LessonVocabularyEntity();
            mapping.setLessonId(lesson.getId());
            mapping.setVocabId(null);
            mapping.setUserVocabId(userVocab.getId());
            lessonVocabularyRepository.save(mapping);
            
            lesson.setVocabularyCount((int) lessonVocabularyRepository.countByLessonId(lesson.getId()));
            lessonRepository.save(lesson);
            
            log.info("Added user vocabulary {} to personal lesson {}", userVocab.getWord(), lesson.getTitle());
        }
        
        return userVocab;
    }
    
    /**
     * Xóa vocabulary khỏi personal lesson
     */
    @Transactional
    public void removeVocabularyFromPersonalLesson(Long lessonId, Long userId, Long vocabId, boolean isUserVocab) {
        LessonEntity lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new RuntimeException("Lesson not found"));
        
        if (!lesson.getUser().getId().equals(userId)) {
            throw new RuntimeException("Unauthorized");
        }
        
        LessonVocabularyEntity mapping;
        if (isUserVocab) {
            mapping = lessonVocabularyRepository
                .findByLessonIdAndUserVocabId(lessonId, vocabId)
                .orElseThrow(() -> new RuntimeException("Vocabulary not found in this lesson"));
        } else {
            mapping = lessonVocabularyRepository
                .findByLessonIdAndVocabId(lessonId, vocabId)
                .orElseThrow(() -> new RuntimeException("Vocabulary not found in this lesson"));
        }
        
        lessonVocabularyRepository.delete(mapping);
        
        lesson.setVocabularyCount((int) lessonVocabularyRepository.countByLessonId(lessonId));
        lessonRepository.save(lesson);
        
        log.info("Removed vocabulary {} from lesson {}", vocabId, lesson.getTitle());
    }
    
    /**
     * Lấy tất cả vocabulary cho lesson (dưới dạng DTO)
     */
    public List<VocabularyDto> getAllVocabulariesForLessonAsDto(Long lessonId) {
        List<LessonVocabularyEntity> mappings = lessonVocabularyRepository.findByLessonId(lessonId);
        
        return mappings.stream()
            .map(mapping -> {
                if (mapping.getVocabId() != null) {
                    return vocabularyRepository.findById(mapping.getVocabId())
                        .map(VocabularyDto::fromVocabularyEntity)
                        .orElse(null);
                } else if (mapping.getUserVocabId() != null) {
                    return userVocabularyRepository.findById(mapping.getUserVocabId())
                        .map(VocabularyDto::fromUserVocabularyEntity)
                        .orElse(null);
                }
                return null;
            })
            .filter(dto -> dto != null)
            .collect(Collectors.toList());
    }
    
    /**
     * Cập nhật thông tin lesson
     */
    @Transactional
    public LessonEntity updateLessonInfo(Long lessonId, String title, String description) {
        LessonEntity lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new RuntimeException("Lesson not found"));
        
        lesson.setTitle(title);
        lesson.setDescription(description);
        
        lessonRepository.save(lesson);
        log.info("Updated lesson info: {}", lesson.getTitle());
        
        return lesson;
    }
    
    /**
     * Cập nhật thông tin user vocabulary
     */
    @Transactional
    public void updateUserVocabulary(
            Long vocabId, Long userId, String word, String pronunciation,
            String pos, String meaning, String exampleSrc, String exampleVi, String level) {
        UserVocabularyEntity vocab = userVocabularyRepository.findById(vocabId)
                .orElseThrow(() -> new RuntimeException("User vocabulary not found"));
        
        if (!vocab.getUserId().equals(userId)) {
            throw new RuntimeException("Unauthorized");
        }
        
        vocab.setWord(word);
        vocab.setPronunciation(pronunciation);
        vocab.setPos(pos);
        vocab.setMeaning(meaning);
        vocab.setExampleSrc(exampleSrc);
        vocab.setExampleVi(exampleVi);
        vocab.setLevel(level);
        
        userVocabularyRepository.save(vocab);
        log.info("Updated user vocabulary: {}", word);
    }
    
    /**
     * Tìm lesson theo ID
     */
    public Optional<LessonEntity> findById(Long lessonId) {
        return lessonRepository.findById(lessonId);
    }
    
    /**
     * Xóa lesson (chỉ personal lesson)
     */
    @Transactional
    public void deletePersonalLesson(Long lessonId, Long userId) {
        LessonEntity lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new RuntimeException("Lesson not found"));
        
        if (!"personal".equals(lesson.getLessonType())) {
            throw new RuntimeException("Cannot delete system lesson");
        }
        
        if (!lesson.getUser().getId().equals(userId)) {
            throw new RuntimeException("Unauthorized");
        }
        
        // Xóa tất cả vocabulary mappings
        lessonVocabularyRepository.deleteByLessonId(lessonId);
        
        // Xóa lesson
        lessonRepository.delete(lesson);
        
        log.info("Deleted personal lesson: {}", lesson.getTitle());
    }

    /**
     * Kiểm tra từ vựng đã tồn tại trong hệ thống chưa
     */
    public VocabularyCheckResponse checkVocabularyExists(String word, String languageCode) {
        List<VocabularyEntity> existing = vocabularyRepository.findByWordAndLanguageCode(word, languageCode);
        
        if (!existing.isEmpty()) {
            VocabularyEntity vocab = existing.get(0);
            return new VocabularyCheckResponse(
                true, 
                VocabularyDto.fromVocabularyEntity(vocab),
                "Từ này đã có trong hệ thống"
            );
        }
        
        return new VocabularyCheckResponse(
            false, 
            null,
            "Từ chưa có trong hệ thống"
        );
    }

    /**
     * Xóa tất cả vocabulary khỏi lesson (personal lesson)
     */
    @Transactional
    public void clearAllVocabulariesFromLesson(Long lessonId, Long userId) {
        LessonEntity lesson = lessonRepository.findById(lessonId)
            .orElseThrow(() -> new RuntimeException("Lesson not found: " + lessonId));
        
        // Kiểm tra ownership
        if (!lesson.getUser().getId().equals(userId)) {
            throw new RuntimeException("Không có quyền xóa vocabulary của lesson này");
        }
        
        // Kiểm tra là personal lesson
        if (!"personal".equals(lesson.getLessonType())) {
            throw new RuntimeException("Chỉ có thể xóa vocabulary của personal lesson");
        }
        
        // Xóa tất cả vocabulary
        lessonVocabularyRepository.deleteByLessonId(lessonId);
        
        // Cập nhật vocabulary_count
        lesson.setVocabularyCount(0);
        lessonRepository.save(lesson);
        
        log.info("Cleared all vocabularies from lesson: {}", lessonId);
    }

}

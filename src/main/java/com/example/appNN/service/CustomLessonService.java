package com.example.appNN.service;

import com.example.appNN.dto.AddVocabularyRequest;
import com.example.appNN.dto.VocabularyCheckResponse;
import com.example.appNN.entity.CustomLessonEntity;
import com.example.appNN.entity.VocabularyEntity;
import com.example.appNN.repository.CustomLessonRepository;
import com.example.appNN.repository.VocabularyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service xử lý custom lesson và vocabulary
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CustomLessonService {
    
    private final CustomLessonRepository customLessonRepository;
    private final VocabularyRepository vocabularyRepository;
    
    /**
     * Kiểm tra từ đã tồn tại trong DB chưa
     */
    public VocabularyCheckResponse checkVocabularyExists(String word, String languageCode) {
        VocabularyEntity existing = vocabularyRepository.findByWordAndLanguageCode(word, languageCode);
        
        if (existing != null) {
            String message = String.format(
                "Từ '%s' đã có trong database với nghĩa: %s. Bạn có muốn sử dụng từ này không?",
                word, existing.getMeaning()
            );
            return new VocabularyCheckResponse(true, existing, message);
        } else {
            return new VocabularyCheckResponse(false, null, "Từ chưa có trong database, sẽ tạo mới");
        }
    }
    
    /**
     * Thêm vocabulary vào custom lesson
     * - Nếu từ đã tồn tại → cập nhật các trường thiếu (pronunciation, example, etc.)
     * - Nếu từ chưa tồn tại → tạo mới
     */
    @Transactional
    public VocabularyEntity addVocabularyToCustomLesson(AddVocabularyRequest request) {
        // Tìm custom lesson
        CustomLessonEntity lesson = customLessonRepository.findById(request.getCustomLessonId())
                .orElseThrow(() -> new RuntimeException("Custom lesson not found"));
        
        VocabularyEntity vocab;
        
        // Kiểm tra từ đã tồn tại chưa
        VocabularyEntity existing = vocabularyRepository.findByWordAndLanguageCode(
            request.getWord(), 
            request.getLanguageCode()
        );
        
        if (existing != null) {
            // Từ đã tồn tại → Cập nhật các trường thiếu
            vocab = existing;
            boolean updated = false;
            
            // Cập nhật pronunciation nếu thiếu
            if ((vocab.getPronunciation() == null || vocab.getPronunciation().isEmpty()) 
                && request.getPronunciation() != null && !request.getPronunciation().isEmpty()) {
                vocab.setPronunciation(request.getPronunciation());
                updated = true;
                log.info("Updated pronunciation for {}", vocab.getWord());
            }
            
            // Cập nhật pos nếu thiếu
            if ((vocab.getPos() == null || vocab.getPos().isEmpty()) 
                && request.getPos() != null && !request.getPos().isEmpty()) {
                vocab.setPos(request.getPos());
                updated = true;
                log.info("Updated pos for {}", vocab.getWord());
            }
            
            // Cập nhật exampleSrc nếu thiếu
            if ((vocab.getExampleSrc() == null || vocab.getExampleSrc().isEmpty()) 
                && request.getExampleSrc() != null && !request.getExampleSrc().isEmpty()) {
                vocab.setExampleSrc(request.getExampleSrc());
                updated = true;
                log.info("Updated exampleSrc for {}", vocab.getWord());
            }
            
            // Cập nhật exampleVi nếu thiếu
            if ((vocab.getExampleVi() == null || vocab.getExampleVi().isEmpty()) 
                && request.getExampleVi() != null && !request.getExampleVi().isEmpty()) {
                vocab.setExampleVi(request.getExampleVi());
                updated = true;
                log.info("Updated exampleVi for {}", vocab.getWord());
            }
            
            if (updated) {
                vocab = vocabularyRepository.save(vocab);
                log.info("Updated existing vocabulary: {} (ID: {})", vocab.getWord(), vocab.getId());
            } else {
                log.info("Using existing vocabulary without changes: {} (ID: {})", vocab.getWord(), vocab.getId());
            }
        } else {
            // Tạo vocabulary mới
            vocab = new VocabularyEntity();
            vocab.setWord(request.getWord());
            vocab.setPronunciation(request.getPronunciation());
            vocab.setPos(request.getPos());
            vocab.setMeaning(request.getMeaning());
            vocab.setExampleSrc(request.getExampleSrc());
            vocab.setExampleVi(request.getExampleVi());
            vocab.setLanguageCode(request.getLanguageCode());
            vocab.setLevel(request.getLevel());
            vocab.setLessonNo(null); // Custom vocab không có lessonNo
            
            vocab = vocabularyRepository.save(vocab);
            log.info("Created new vocabulary: {} (ID: {})", vocab.getWord(), vocab.getId());
        }
        
        // Thêm vào custom lesson (nếu chưa có)
        if (!lesson.getVocabularies().contains(vocab)) {
            lesson.getVocabularies().add(vocab);
            customLessonRepository.save(lesson);
            log.info("Added vocabulary {} to custom lesson {}", vocab.getWord(), lesson.getTitle());
        } else {
            log.info("Vocabulary {} already in custom lesson {}", vocab.getWord(), lesson.getTitle());
        }
        
        return vocab;
    }
    
    /**
     * Tạo custom lesson mới
     */
    @Transactional
    public CustomLessonEntity createCustomLesson(Long userId, String title, String description, 
                                                  String languageCode, String level) {
        CustomLessonEntity lesson = new CustomLessonEntity();
        lesson.setUserId(userId);
        lesson.setTitle(title);
        lesson.setDescription(description);
        lesson.setLanguageCode(languageCode);
        lesson.setLevel(level);
        
        lesson = customLessonRepository.save(lesson);
        log.info("Created custom lesson: {} (ID: {}) for user {}", title, lesson.getId(), userId);
        
        return lesson;
    }
    
    /**
     * Lấy danh sách custom lesson của user
     */
    public List<CustomLessonEntity> getMyCustomLessons(Long userId) {
        return customLessonRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }
    
    /**
     * Lấy chi tiết custom lesson
     */
    public CustomLessonEntity getCustomLessonById(Long lessonId) {
        return customLessonRepository.findById(lessonId).orElse(null);
    }
}

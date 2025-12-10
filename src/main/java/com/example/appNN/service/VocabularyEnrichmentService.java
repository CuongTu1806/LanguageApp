package com.example.appNN.service;

import com.example.appNN.entity.VocabularyEntity;
import com.example.appNN.repository.VocabularyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service để làm giàu dữ liệu từ vựng (enrichment):
 * - Tự động lấy hình ảnh từ Pixabay
 * - Trong tương lai có thể thêm: lấy ví dụ câu, phát âm, v.v.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class VocabularyEnrichmentService {

    private final VocabularyRepository vocabularyRepository;
    private final ImageService imageService;
    private final LessonService lessonService;

    /**
     * Xử lý tự động lấy hình ảnh cho TẤT CẢ từ vựng chưa có hình
     * Chạy trong background, có thể mất thời gian
     */
    @Transactional
    public void enrichAllVocabulary() {
        log.info("Starting vocabulary enrichment process...");
        
        List<VocabularyEntity> allVocabs = vocabularyRepository.findAll();
        int processed = 0;
        int success = 0;
        int skipped = 0;

        for (VocabularyEntity vocab : allVocabs) {
            processed++;
            
            // Skip nếu đã có hình
            if (vocab.getImagePath() != null && !vocab.getImagePath().isEmpty()) {
                skipped++;
                if (processed % 100 == 0) {
                    log.info("Progress: {}/{} - Skipped (already has image): {}", 
                            processed, allVocabs.size(), vocab.getWord());
                }
                continue;
            }

            // Lấy từ khóa tìm kiếm: ưu tiên meaning (tiếng Việt) 
            // Nhưng Pixabay search tốt nhất với tiếng Anh
            // => Cần dịch meaning sang tiếng Anh hoặc dùng word (Hanzi) + pinyin
            String searchKeyword = extractSearchKeyword(vocab);
            
            if (searchKeyword == null || searchKeyword.isEmpty()) {
                log.warn("Cannot extract search keyword for vocab: {}", vocab.getWord());
                continue;
            }

            try {
                // Tìm và tải hình ảnh
                String imagePath = imageService.fetchAndDownloadImage(searchKeyword, vocab.getId());
                
                if (imagePath != null) {
                    vocab.setImagePath(imagePath);
                    vocabularyRepository.save(vocab);
                    success++;
                    log.info("✓ [{}/{}] Successfully enriched: {} -> {}", 
                            processed, allVocabs.size(), vocab.getWord(), imagePath);
                } else {
                    log.warn("✗ [{}/{}] No image found for: {} (keyword: {})", 
                            processed, allVocabs.size(), vocab.getWord(), searchKeyword);
                }

                // Rate limiting: đợi 1 giây giữa các request để tránh spam API
                Thread.sleep(1000);

            } catch (InterruptedException e) {
                log.error("Enrichment interrupted", e);
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                log.error("Error enriching vocab: " + vocab.getWord(), e);
            }
        }

        log.info("Enrichment completed! Processed: {}, Success: {}, Skipped: {}, Failed: {}", 
                processed, success, skipped, (processed - success - skipped));
    }

    /**
     * Xử lý hình ảnh cho một từ vựng cụ thể
     */
    @Transactional
    public boolean enrichSingleVocabulary(Long vocabId) {
        VocabularyEntity vocab = vocabularyRepository.findById(vocabId).orElse(null);
        if (vocab == null) {
            log.warn("Vocabulary not found: {}", vocabId);
            return false;
        }

        // Skip nếu đã có hình
        if (vocab.getImagePath() != null && !vocab.getImagePath().isEmpty()) {
            log.info("Vocabulary already has image: {}", vocab.getWord());
            return true;
        }

        String searchKeyword = extractSearchKeyword(vocab);
        if (searchKeyword == null || searchKeyword.isEmpty()) {
            log.warn("Cannot extract search keyword for vocab: {}", vocab.getWord());
            return false;
        }

        String imagePath = imageService.fetchAndDownloadImage(searchKeyword, vocab.getId());
        if (imagePath != null) {
            vocab.setImagePath(imagePath);
            vocabularyRepository.save(vocab);
            log.info("Successfully enriched vocabulary: {} -> {}", vocab.getWord(), imagePath);
            return true;
        }

        log.warn("No image found for vocabulary: {}", vocab.getWord());
        return false;
    }

    /**
     * Xử lý hình ảnh cho một bài học cụ thể (theo hệ thống Lesson mới)
     */
    @Transactional
    public int enrichLesson(Long userId, String languageCode, String level, Integer lessonIndex) {
        log.info("Enriching lesson for user {}: {} - {} - Lesson {}", userId, languageCode, level, lessonIndex);
        
        // Lấy từ vựng từ lesson thông qua LessonService
        List<VocabularyEntity> vocabs = lessonService.getLessonWords(userId, languageCode, level, lessonIndex);
        
        if (vocabs.isEmpty()) {
            log.warn("No vocabularies found for lesson: {} - {} - {}", languageCode, level, lessonIndex);
            return 0;
        }
        
        int success = 0;
        for (VocabularyEntity vocab : vocabs) {
            if (enrichSingleVocabulary(vocab.getId())) {
                success++;
            }
            
            try {
                Thread.sleep(1000); // Rate limiting
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        
        log.info("Enriched {}/{} vocabularies in lesson {}", success, vocabs.size(), lessonIndex);
        return success;
    }

    /**
     * Trích xuất từ khóa tìm kiếm tốt nhất cho hình ảnh
     * Logic: 
     * 1. Ưu tiên meaning_en (tiếng Anh được dịch tự động)
     * 2. Fallback: dùng pinyin (pronunciation)
     * 3. Last resort: dùng word (Hanzi)
     */
    private String extractSearchKeyword(VocabularyEntity vocab) {
        // Ưu tiên dùng meaningEn nếu có
        String meaningEn = vocab.getMeaningEn();
        if (meaningEn != null && !meaningEn.isEmpty()) {
            return meaningEn.trim();
        }

        // Fallback: dùng pinyin
        String pronunciation = vocab.getPronunciation();
        if (pronunciation != null && !pronunciation.isEmpty()) {
            return pronunciation.trim();
        }

        // Last resort: dùng word (Hanzi) - không hiệu quả lắm với Pixabay
        return vocab.getWord();
    }
}

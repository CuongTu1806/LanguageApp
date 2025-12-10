package com.example.appNN.service;

import com.example.appNN.entity.VocabularyEntity;
import com.example.appNN.repository.VocabularyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Service tự động dịch nghĩa tiếng Việt sang tiếng Anh
 * Sử dụng Google Translate unofficial API (miễn phí, không cần key)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TranslationService {

    private final VocabularyRepository vocabularyRepository;
    private final RestTemplate restTemplate = new RestTemplate();
    
    // Google Translate unofficial API - miễn phí, không cần key
    private static final String TRANSLATION_API = "https://translate.googleapis.com/translate_a/single";

    /**
     * Dịch một từ tiếng Trung (Hanzi) sang tiếng Anh
     * Chiến lược: Ưu tiên dịch từ tiếng Việt vì dễ hơn
     */
    public String translateZhToEn(String chinese) {
        return translateZhToEn(chinese, null);
    }
    
    /**
     * Dịch một từ tiếng Trung (Hanzi) sang tiếng Anh
     * @param chinese Từ tiếng Trung
     * @param vietnameseMeaning Nghĩa tiếng Việt (ưu tiên dùng để dịch)
     */
    public String translateZhToEn(String chinese, String vietnameseMeaning) {
        // Ưu tiên dịch từ tiếng Việt vì Google Translate dịch tiếng Việt -> Anh tốt hơn
        if (vietnameseMeaning != null && !vietnameseMeaning.isEmpty()) {
            String viTranslation = tryTranslateVietnamese(vietnameseMeaning);
            if (viTranslation != null && !viTranslation.isEmpty()) {
                return viTranslation;
            }
        }
        
        // Fallback: thử dịch từ tiếng Trung
        try {
            // Build URL with parameters - thử cả zh-CN và auto
            String url = String.format(
                "%s?client=gtx&sl=auto&tl=en&dt=t&q=%s",
                TRANSLATION_API,
                java.net.URLEncoder.encode(chinese, StandardCharsets.UTF_8)
            );

            String response = restTemplate.getForObject(url, String.class);
            
            if (response == null || response.isEmpty()) {
                log.warn("Empty response for: {}", chinese);
                return null;
            }

            // Parse response: [[["tea","茶",null,null,10]],null,"zh-CN",null,null,null,null,[]]
            // Extract first translation between quotes after first [[["
            Pattern pattern = Pattern.compile("\\[\\[\\[\"([^\"]+)\"");
            Matcher matcher = pattern.matcher(response);
            
            if (matcher.find()) {
                String translation = matcher.group(1).trim();
                
                // Decode URL-encoded characters if present
                try {
                    // Check if contains encoded characters like %e8%8c%b6
                    if (translation.contains("%")) {
                        translation = java.net.URLDecoder.decode(translation, StandardCharsets.UTF_8);
                    }
                } catch (Exception e) {
                    log.warn("Failed to decode translation: {}", translation);
                }
                
                // Kiểm tra xem có dịch thành công không (không trùng với từ gốc)
                if (translation.equals(chinese)) {
                    log.warn("Translation same as input '{}'", chinese);
                    return null;
                }
                
                translation = translation.toLowerCase();
                log.info("Translated (Chinese) '{}' -> '{}'", chinese, translation);
                return translation;
            }
            
            log.warn("No translation found for: {}", chinese);
            return null;

        } catch (Exception e) {
            log.error("Error translating: " + chinese, e);
            return null;
        }
    }
    
    /**
     * Thử dịch từ tiếng Việt sang tiếng Anh (primary method)
     */
    private String tryTranslateVietnamese(String vietnamese) {
        if (vietnamese == null || vietnamese.isEmpty()) {
            return null;
        }
        
        try {
            // Lấy từ đầu tiên nếu có nhiều nghĩa (tách bằng dấu ; hoặc ,)
            String firstMeaning = vietnamese.split("[;,]")[0].trim();
            
            // Loại bỏ các ký tự đặc biệt nếu có
            firstMeaning = firstMeaning.replaceAll("[\\(\\)]", "").trim();
            
            if (firstMeaning.isEmpty()) {
                return null;
            }
            
            // Thử method 1: MyMemory API (đơn giản hơn, ổn định hơn)
            try {
                String myMemoryUrl = String.format(
                    "https://api.mymemory.translated.net/get?q=%s&langpair=vi|en",
                    java.net.URLEncoder.encode(firstMeaning, StandardCharsets.UTF_8)
                );
                
                String myMemoryResponse = restTemplate.getForObject(myMemoryUrl, String.class);
                
                // **LOG RESPONSE ĐỂ DEBUG**
                log.info("MyMemory API response: {}", myMemoryResponse);
                
                if (myMemoryResponse != null) {
                    // Parse: {"responseData":{"translatedText":"tea"},...}
                    Pattern pattern = Pattern.compile("\"translatedText\"\\s*:\\s*\"([^\"]+)\"");
                    Matcher matcher = pattern.matcher(myMemoryResponse);
                    
                    if (matcher.find()) {
                        String translation = matcher.group(1).trim();
                        
                        // **DECODE NGAY SAU KHI MATCH**
                        try {
                            translation = java.net.URLDecoder.decode(translation, StandardCharsets.UTF_8);
                        } catch (Exception e) {
                            log.warn("Failed to decode MyMemory translation: {}", translation);
                        }
                        
                        translation = translation.toLowerCase();
                        
                        // **THÊM LOG ĐỂ XEM KẾT QUẢ SAU KHI DECODE**
                        log.info("After decode: '{}' (original: '{}')", translation, firstMeaning);
                        
                        if (!translation.equals(firstMeaning.toLowerCase())) {
                            log.info("Translated Vietnamese (MyMemory) '{}' -> '{}'", firstMeaning, translation);
                            return translation;
                        } else {
                            log.warn("Translation equals original after decode: '{}'", translation);
                        }
                    }
                }
            } catch (Exception e) {
                log.warn("MyMemory API failed: {}", e.getMessage());
            }
            
            // Fallback: Google Translate
            String url = String.format(
                "%s?client=gtx&sl=vi&tl=en&dt=t&q=%s",
                TRANSLATION_API,
                java.net.URLEncoder.encode(firstMeaning, StandardCharsets.UTF_8)
            );

            String response = restTemplate.getForObject(url, String.class);
            
            if (response != null && !response.isEmpty()) {
                Pattern pattern = Pattern.compile("\\[\\[\\[\"([^\"]+)\"");
                Matcher matcher = pattern.matcher(response);
                
                if (matcher.find()) {
                    String translation = matcher.group(1).trim();
                    
                    // Decode if needed
                    if (translation.contains("%")) {
                        translation = java.net.URLDecoder.decode(translation, StandardCharsets.UTF_8);
                    }
                    
                    // Kiểm tra xem dịch có thành công không
                    if (!translation.equals(firstMeaning)) {
                        translation = translation.toLowerCase();
                        log.info("Translated Vietnamese (Google) '{}' -> '{}'", firstMeaning, translation);
                        return translation;
                    }
                }
            }
        } catch (Exception e) {
            log.error("Error translating Vietnamese: " + vietnamese, e);
        }
        
        return null;
    }

    /**
     * Dịch một từ tiếng Việt sang tiếng Anh (fallback) - không dùng nữa
     */
    @Deprecated
    public String translateViToEn(String vietnamese) {
        try {
            String url = String.format(
                "%s?client=gtx&sl=vi&tl=en&dt=t&q=%s",
                TRANSLATION_API,
                java.net.URLEncoder.encode(vietnamese, StandardCharsets.UTF_8)
            );

            String response = restTemplate.getForObject(url, String.class);
            
            if (response == null || response.isEmpty()) {
                log.warn("Empty response for: {}", vietnamese);
                return null;
            }

            Pattern pattern = Pattern.compile("\\[\\[\\[\"([^\"]+)\"");
            Matcher matcher = pattern.matcher(response);
            
            if (matcher.find()) {
                String translation = matcher.group(1).trim().toLowerCase();
                log.info("Translated '{}' -> '{}'", vietnamese, translation);
                return translation;
            }
            
            log.warn("No translation found for: {}", vietnamese);
            return null;

        } catch (Exception e) {
            log.error("Error translating: " + vietnamese, e);
            return null;
        }
    }

    /**
     * Thêm nghĩa tiếng Anh vào tất cả từ vựng
     * Dịch từ Hanzi (tiếng Trung) sang tiếng Anh
     */
    @Transactional
    public int addEnglishMeaningToAll() {
        log.info("Starting to add English meanings to all vocabularies...");
        
        List<VocabularyEntity> allVocabs = vocabularyRepository.findAll();
        int updated = 0;
        int skipped = 0;
        
        for (VocabularyEntity vocab : allVocabs) {
            // Skip nếu đã có meaningEn
            if (vocab.getMeaningEn() != null && !vocab.getMeaningEn().isEmpty()) {
                skipped++;
                continue;
            }
            
            // Dịch từ Hanzi (word) sang tiếng Anh, fallback sang Vietnamese
            String hanzi = vocab.getWord();
            String meaning = vocab.getMeaning();
            if (hanzi == null || hanzi.isEmpty()) {
                continue;
            }
            
            // Dịch sang tiếng Anh (có fallback sang Vietnamese nếu cần)
            String enWord = translateZhToEn(hanzi, meaning);
            if (enWord != null && !enWord.isEmpty()) {
                // Lưu vào cột meaning_en
                vocab.setMeaningEn(enWord);
                vocabularyRepository.save(vocab);
                updated++;
                
                log.info("✓ Updated [{}]: meaning_en = {}", hanzi, enWord);
            }
            
            // Rate limiting: 1 giây/request (tránh spam API)
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        
        log.info("Translation completed! Updated: {}, Skipped: {}", updated, skipped);
        return updated;
    }

    /**
     * Thêm nghĩa tiếng Anh cho một bài học cụ thể
     * Dịch từ Hanzi sang tiếng Anh
     */
    @Transactional
    public int addEnglishMeaningToLesson(String languageCode, String level, Integer lessonNo) {
        log.info("Adding English meanings to lesson: {} - {} - {}", languageCode, level, lessonNo);
        
        List<VocabularyEntity> vocabs = vocabularyRepository
                .findByLanguageCodeAndLevelAndLessonNoOrderById(languageCode, level, lessonNo);
        
        int updated = 0;
        
        for (VocabularyEntity vocab : vocabs) {
            // Skip nếu đã có meaningEn
            if (vocab.getMeaningEn() != null && !vocab.getMeaningEn().isEmpty()) {
                continue;
            }
            
            String hanzi = vocab.getWord();
            String meaning = vocab.getMeaning();
            String enWord = translateZhToEn(hanzi, meaning);
            
            if (enWord != null) {
                vocab.setMeaningEn(enWord);
                vocabularyRepository.save(vocab);
                updated++;
                log.info("✓ Updated: {} -> meaning_en = {}", hanzi, enWord);
            }
            
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        
        log.info("Updated {}/{} vocabularies in lesson", updated, vocabs.size());
        return updated;
    }

    /**
     * Trích xuất nghĩa đầu tiên từ chuỗi meaning
     * Ví dụ: "trà; trà xanh" -> "trà"
     */
    private String extractFirstMeaning(String meaning) {
        if (meaning == null || meaning.isEmpty()) {
            return null;
        }
        
        // Lấy phần trước dấu phẩy, chấm phẩy, hoặc ngoặc đơn
        String[] separators = {",", ";", "、", "，", "("};
        String result = meaning;
        
        for (String sep : separators) {
            if (result.contains(sep)) {
                result = result.substring(0, result.indexOf(sep));
                break;
            }
        }
        
        return result.trim();
    }
}

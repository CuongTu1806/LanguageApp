package com.example.appNN.dto;

import com.example.appNN.entity.UserVocabularyEntity;
import com.example.appNN.entity.VocabularyEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO thống nhất cho vocabulary (cả system và user)
 * Dùng để trả về API response
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class VocabularyDto {
    
    private Long id; // ID hiển thị (có thể trùng giữa system và user vocab)
    private Long systemVocabId; // ID thật từ bảng vocabulary (null nếu là user vocab)
    private Long userVocabId; // ID thật từ bảng user_vocabulary (null nếu là system vocab)
    private Long vocabId; // Để tương thích với frontend (alias của id)
    private String word;
    private String pronunciation;
    private String pos;
    private String meaning;
    private String meaningEn;
    private String exampleSrc;
    private String exampleVi;
    private String audioPath;
    private String imagePath;
    private String languageCode;
    private String level;
    private Boolean isUserVocab; // true nếu là user vocab, false nếu là system vocab
    
    /**
     * Tạo DTO từ VocabularyEntity (system vocab)
     */
    public static VocabularyDto fromVocabularyEntity(VocabularyEntity entity) {
        VocabularyDto dto = new VocabularyDto();
        dto.setId(entity.getId());
        dto.setSystemVocabId(entity.getId()); // ID thật từ bảng vocabulary
        dto.setUserVocabId(null); // Không phải user vocab
        dto.setVocabId(entity.getId()); // vocabId = id
        dto.setWord(entity.getWord());
        dto.setPronunciation(entity.getPronunciation());
        dto.setPos(entity.getPos());
        dto.setMeaning(entity.getMeaning());
        dto.setMeaningEn(entity.getMeaningEn());
        dto.setExampleSrc(entity.getExampleSrc());
        dto.setExampleVi(entity.getExampleVi());
        dto.setAudioPath(entity.getAudioPath());
        dto.setImagePath(entity.getImagePath());
        dto.setLanguageCode(entity.getLanguageCode());
        dto.setLevel(entity.getLevel());
        dto.setIsUserVocab(false);
        return dto;
    }
    
    /**
     * Tạo DTO từ UserVocabularyEntity (user vocab)
     */
    public static VocabularyDto fromUserVocabularyEntity(UserVocabularyEntity entity) {
        VocabularyDto dto = new VocabularyDto();
        dto.setId(entity.getId());
        dto.setSystemVocabId(null); // Không phải system vocab
        dto.setUserVocabId(entity.getId()); // ID thật từ bảng user_vocabulary
        dto.setVocabId(entity.getId()); // vocabId = id
        dto.setWord(entity.getWord());
        dto.setPronunciation(entity.getPronunciation());
        dto.setPos(entity.getPos());
        dto.setMeaning(entity.getMeaning());
        dto.setMeaningEn(null); // User vocab không có meaningEn
        dto.setExampleSrc(entity.getExampleSrc());
        dto.setExampleVi(entity.getExampleVi());
        dto.setAudioPath(null); // User vocab không có audioPath
        dto.setImagePath(null); // User vocab không có imagePath
        dto.setLanguageCode(entity.getLanguageCode());
        dto.setLevel(entity.getLevel());
        dto.setIsUserVocab(true);
        return dto;
    }
    
    /**
     * Helper method để lấy ID phù hợp khi query stats
     * Tránh nhầm lẫn giữa system vocab và user vocab
     */
    public Long getRealVocabId() {
        return systemVocabId != null ? systemVocabId : userVocabId;
    }
}

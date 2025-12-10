// com.example.appNN.service.UserVocabStatsService
package com.example.appNN.service;

import com.example.appNN.entity.UserVocabStatsEntity;
import com.example.appNN.entity.UserVocabStatsId;
import com.example.appNN.repository.UserVocabStatsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class UserVocabStatsService {

    private final UserVocabStatsRepository statsRepository;

    /**
     * Cập nhật thống kê cho 1 (user, vocab) sau mỗi câu hỏi.
     * @param userId  id user
     * @param vocabId id từ vựng
     * @param isCorrect true nếu trả lời đúng, false nếu sai
     */
    @Transactional
    public void updateStats(Long userId, Long vocabId, boolean isCorrect) {
        UserVocabStatsId id = new UserVocabStatsId(userId, vocabId);

        UserVocabStatsEntity stats = statsRepository.findById(id)
                .orElseGet(() -> {
                    UserVocabStatsEntity s = new UserVocabStatsEntity();
                    s.setId(id);
                    s.setTotalAttempts(0);
                    s.setWrongCount(0);
                    return s;
                });

        LocalDateTime now = LocalDateTime.now();

        int total = stats.getTotalAttempts() == null ? 0 : stats.getTotalAttempts();
        int wrong = stats.getWrongCount() == null ? 0 : stats.getWrongCount();

        stats.setTotalAttempts(total + 1);
        stats.setLastAttemptAt(now);

        if (isCorrect) {
            stats.setLastResult("CORRECT");
            stats.setLastCorrectAt(now);
        } else {
            stats.setWrongCount(wrong + 1);
            stats.setLastResult("WRONG");
            stats.setLastWrongAt(now);
        }

        statsRepository.save(stats);
    }
}

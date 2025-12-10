// com.example.appNN.repository.UserVocabStatsRepository
package com.example.appNN.repository;

import com.example.appNN.entity.UserVocabStatsEntity;
import com.example.appNN.entity.UserVocabStatsId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserVocabStatsRepository
        extends JpaRepository<UserVocabStatsEntity, UserVocabStatsId> {

    List<UserVocabStatsEntity> findByIdUserId(Long userId);

    List<UserVocabStatsEntity> findByIdUserIdAndIdVocabIdIn(Long userId, List<Long> vocabIds);
}

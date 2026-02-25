package org.example.backend.repository;

import org.example.backend.entity.ChecklistResult;
import org.example.backend.entity.TopicReviewer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChecklistResultRepository extends JpaRepository<ChecklistResult, Long> {

    List<ChecklistResult> findByTopicReviewer(TopicReviewer topicReviewer);

    @Query("SELECT COALESCE(SUM(cr.score), 0) FROM ChecklistResult cr WHERE cr.topicReviewer = :topicReviewer")
    Integer sumScoreByTopicReviewer(@Param("topicReviewer") TopicReviewer topicReviewer);

    void deleteByTopicReviewer(TopicReviewer topicReviewer);
}

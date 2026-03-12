package org.example.backend.repository;

import org.example.backend.entity.Topic;
import org.example.backend.entity.TopicReviewer;
import org.example.backend.entity.User;
import org.example.backend.enums.ReviewStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TopicReviewerRepository extends JpaRepository<TopicReviewer, Long> {

    List<TopicReviewer> findByTopic(Topic topic);

    List<TopicReviewer> findByReviewer(User reviewer);

    List<TopicReviewer> findByReviewerAndReviewStatus(User reviewer, ReviewStatus status);

    Optional<TopicReviewer> findByTopicAndReviewer(Topic topic, User reviewer);

    boolean existsByTopicAndReviewer(Topic topic, User reviewer);

    @Query("SELECT COUNT(tr) FROM TopicReviewer tr WHERE tr.topic = :topic")
    Long countByTopic(@Param("topic") Topic topic);

    @Query("SELECT COUNT(tr) FROM TopicReviewer tr WHERE tr.topic = :topic AND tr.reviewStatus = 'COMPLETED'")
    Long countCompletedByTopic(@Param("topic") Topic topic);

    @Query("SELECT tr FROM TopicReviewer tr WHERE tr.reviewer = :reviewer AND tr.reviewStatus <> 'COMPLETED'")
    List<TopicReviewer> findPendingByReviewer(@Param("reviewer") User reviewer);
    @Query("SELECT COUNT(tr) FROM TopicReviewer tr WHERE tr.reviewer = :reviewer AND tr.topic.semester = :semester")
    Long countByReviewerAndTopicSemester(@Param("reviewer") User reviewer, @Param("semester") org.example.backend.entity.Semester semester);
}

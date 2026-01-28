package org.example.backend.repository;

import org.example.backend.entity.Review;
import org.example.backend.entity.Topic;
import org.example.backend.entity.User;
import org.example.backend.enums.ReviewDecision;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    List<Review> findByTopic(Topic topic);

    List<Review> findByReviewer(User reviewer);

    List<Review> findByTopicAndTopicVersion(Topic topic, Integer topicVersion);

    Optional<Review> findByTopicAndReviewer(Topic topic, User reviewer);

    @Query("SELECT r FROM Review r WHERE r.reviewer = :reviewer AND r.decision IS NULL")
    List<Review> findPendingByReviewer(@Param("reviewer") User reviewer);

    @Query("SELECT COUNT(r) FROM Review r WHERE r.topic = :topic AND r.topicVersion = :version AND r.decision IS NOT NULL")
    Long countCompletedReviews(@Param("topic") Topic topic, @Param("version") Integer version);

    @Query("SELECT r FROM Review r WHERE r.topic = :topic AND r.topicVersion = :version AND r.decision = :decision")
    List<Review> findByTopicVersionAndDecision(@Param("topic") Topic topic,
            @Param("version") Integer version,
            @Param("decision") ReviewDecision decision);

    boolean existsByTopicAndReviewer(Topic topic, User reviewer);
}

package org.example.backend.repository;

import org.example.backend.entity.Topic;
import org.example.backend.entity.TopicInheritance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TopicInheritanceRepository extends JpaRepository<TopicInheritance, Long> {

    Optional<TopicInheritance> findByChildTopic(Topic childTopic);

    List<TopicInheritance> findByParentTopic(Topic parentTopic);

    boolean existsByParentTopicAndChildTopic(Topic parentTopic, Topic childTopic);
}

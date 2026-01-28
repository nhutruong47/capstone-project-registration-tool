package org.example.backend.repository;

import org.example.backend.entity.Registration;
import org.example.backend.entity.Team;
import org.example.backend.entity.Topic;
import org.example.backend.enums.RegistrationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RegistrationRepository extends JpaRepository<Registration, Long> {

    List<Registration> findByTeam(Team team);

    List<Registration> findByTopic(Topic topic);

    List<Registration> findByStatus(RegistrationStatus status);

    Optional<Registration> findByTeamAndTopic(Team team, Topic topic);

    List<Registration> findByTopicAndStatus(Topic topic, RegistrationStatus status);

    @Query("SELECT r FROM Registration r WHERE r.topic.supervisor = :supervisor")
    List<Registration> findBySupervisor(@Param("supervisor") org.example.backend.entity.User supervisor);

    @Query("SELECT r FROM Registration r WHERE r.topic.supervisor = :supervisor AND r.status = :status")
    List<Registration> findBySupervisorAndStatus(@Param("supervisor") org.example.backend.entity.User supervisor,
            @Param("status") RegistrationStatus status);

    @Query("SELECT COUNT(r) FROM Registration r WHERE r.topic = :topic AND r.status IN ('PENDING', 'APPROVED', 'FINALIZED')")
    Long countActiveRegistrations(@Param("topic") Topic topic);

    boolean existsByTeamAndTopic(Team team, Topic topic);
}

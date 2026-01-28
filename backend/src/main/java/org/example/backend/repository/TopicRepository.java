package org.example.backend.repository;

import org.example.backend.entity.Topic;
import org.example.backend.entity.User;
import org.example.backend.entity.Semester;
import org.example.backend.enums.TopicStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TopicRepository extends JpaRepository<Topic, Long> {

    Optional<Topic> findByCode(String code);

    boolean existsByCode(String code);

    List<Topic> findBySupervisor(User supervisor);

    List<Topic> findBySemester(Semester semester);

    List<Topic> findByStatus(TopicStatus status);

    List<Topic> findBySemesterAndStatus(Semester semester, TopicStatus status);

    List<Topic> findBySupervisorAndSemester(User supervisor, Semester semester);

    @Query("SELECT t FROM Topic t WHERE t.status IN :statuses AND t.semester = :semester")
    List<Topic> findByStatusesAndSemester(@Param("statuses") List<TopicStatus> statuses,
            @Param("semester") Semester semester);

    @Query("SELECT COUNT(t) FROM Topic t WHERE t.semester = :semester AND t.code LIKE :prefix%")
    Long countByCodePrefix(@Param("semester") Semester semester, @Param("prefix") String prefix);

    @Query("SELECT t FROM Topic t WHERE t.status IN ('APPROVED', 'PUBLISHED') AND t.semester = :semester")
    List<Topic> findAvailableForRegistration(@Param("semester") Semester semester);
}

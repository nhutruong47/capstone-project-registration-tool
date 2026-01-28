package org.example.backend.repository;

import org.example.backend.entity.Team;
import org.example.backend.entity.Semester;
import org.example.backend.entity.User;
import org.example.backend.enums.TeamStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TeamRepository extends JpaRepository<Team, Long> {

    Optional<Team> findByInviteCode(String inviteCode);

    List<Team> findByLeader(User leader);

    List<Team> findBySemester(Semester semester);

    List<Team> findByStatus(TeamStatus status);

    List<Team> findBySemesterAndStatus(Semester semester, TeamStatus status);

    @Query("SELECT t FROM Team t JOIN t.members m WHERE m.user = :user AND t.semester = :semester")
    Optional<Team> findByMemberAndSemester(@Param("user") User user, @Param("semester") Semester semester);

    @Query("SELECT CASE WHEN COUNT(tm) > 0 THEN true ELSE false END FROM TeamMember tm WHERE tm.user = :user AND tm.team.semester = :semester")
    boolean isUserInTeamForSemester(@Param("user") User user, @Param("semester") Semester semester);

    boolean existsByInviteCode(String inviteCode);
}

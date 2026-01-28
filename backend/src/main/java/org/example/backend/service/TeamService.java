package org.example.backend.service;

import lombok.RequiredArgsConstructor;
import org.example.backend.entity.*;
import org.example.backend.enums.TeamMemberRole;
import org.example.backend.enums.TeamStatus;
import org.example.backend.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class TeamService {

    private static final int MIN_TEAM_SIZE = 4;
    private static final int MAX_TEAM_SIZE = 5;

    private final TeamRepository teamRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final SemesterRepository semesterRepository;
    private final NotificationService notificationService;

    /**
     * Generate unique invite code
     */
    private String generateInviteCode() {
        String code;
        do {
            code = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        } while (teamRepository.existsByInviteCode(code));
        return code;
    }

    public Team create(User leader, Long semesterId, String name) {
        Semester semester = semesterRepository.findById(semesterId)
                .orElseThrow(() -> new RuntimeException("Semester not found"));

        // Check if user is already in a team for this semester
        if (teamRepository.isUserInTeamForSemester(leader, semester)) {
            throw new RuntimeException("User is already in a team for this semester");
        }

        Team team = Team.builder()
                .name(name)
                .inviteCode(generateInviteCode())
                .leader(leader)
                .semester(semester)
                .status(TeamStatus.FORMING)
                .build();

        Team savedTeam = teamRepository.save(team);

        // Add leader as first member
        TeamMember leaderMember = TeamMember.builder()
                .team(savedTeam)
                .user(leader)
                .role(TeamMemberRole.LEADER)
                .build();
        teamMemberRepository.save(leaderMember);

        return savedTeam;
    }

    public Optional<Team> findById(Long id) {
        return teamRepository.findById(id);
    }

    public Optional<Team> findByInviteCode(String inviteCode) {
        return teamRepository.findByInviteCode(inviteCode);
    }

    public Optional<Team> findByUserAndSemester(User user, Semester semester) {
        return teamRepository.findByMemberAndSemester(user, semester);
    }

    public List<Team> findBySemester(Semester semester) {
        return teamRepository.findBySemester(semester);
    }

    public Team joinByInviteCode(User user, String inviteCode) {
        Team team = teamRepository.findByInviteCode(inviteCode)
                .orElseThrow(() -> new RuntimeException("Invalid invite code"));

        // Check if user is already in a team for this semester
        if (teamRepository.isUserInTeamForSemester(user, team.getSemester())) {
            throw new RuntimeException("User is already in a team for this semester");
        }

        // Check team size
        Long currentSize = teamMemberRepository.countByTeam(team);
        if (currentSize >= MAX_TEAM_SIZE) {
            throw new RuntimeException("Team is full");
        }

        // Add member
        TeamMember member = TeamMember.builder()
                .team(team)
                .user(user)
                .role(TeamMemberRole.MEMBER)
                .build();
        teamMemberRepository.save(member);

        // Update team status
        updateTeamStatus(team);

        // Notify leader
        notificationService.create(team.getLeader(),
                "New Team Member",
                user.getFullName() + " has joined your team " + team.getName(),
                "/teams/" + team.getId());

        return team;
    }

    public void kickMember(Long teamId, Long userId, User requestingUser) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new RuntimeException("Team not found"));

        // Only leader can kick members
        if (!team.getLeader().getId().equals(requestingUser.getId())) {
            throw new RuntimeException("Only team leader can kick members");
        }

        // Cannot kick yourself
        if (userId.equals(requestingUser.getId())) {
            throw new RuntimeException("Leader cannot kick themselves");
        }

        TeamMember member = teamMemberRepository.findByTeamAndUser(team,
                User.builder().id(userId).build())
                .orElseThrow(() -> new RuntimeException("Member not found in team"));

        teamMemberRepository.delete(member);
        updateTeamStatus(team);
    }

    public void leaveTeam(Long teamId, User user) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new RuntimeException("Team not found"));

        // Leader cannot leave, must transfer leadership or disband
        if (team.getLeader().getId().equals(user.getId())) {
            throw new RuntimeException("Leader cannot leave the team. Transfer leadership first or disband the team.");
        }

        TeamMember member = teamMemberRepository.findByTeamAndUser(team, user)
                .orElseThrow(() -> new RuntimeException("User is not a member of this team"));

        teamMemberRepository.delete(member);
        updateTeamStatus(team);

        // Notify leader
        notificationService.create(team.getLeader(),
                "Team Member Left",
                user.getFullName() + " has left your team " + team.getName(),
                "/teams/" + team.getId());
    }

    public Team transferLeadership(Long teamId, Long newLeaderId, User currentLeader) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new RuntimeException("Team not found"));

        if (!team.getLeader().getId().equals(currentLeader.getId())) {
            throw new RuntimeException("Only current leader can transfer leadership");
        }

        // Find new leader member
        TeamMember newLeaderMember = team.getMembers().stream()
                .filter(m -> m.getUser().getId().equals(newLeaderId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("New leader must be a team member"));

        // Find current leader member
        TeamMember currentLeaderMember = team.getMembers().stream()
                .filter(m -> m.getUser().getId().equals(currentLeader.getId()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Current leader member not found"));

        // Update roles
        newLeaderMember.setRole(TeamMemberRole.LEADER);
        currentLeaderMember.setRole(TeamMemberRole.MEMBER);
        team.setLeader(newLeaderMember.getUser());

        teamMemberRepository.save(newLeaderMember);
        teamMemberRepository.save(currentLeaderMember);
        return teamRepository.save(team);
    }

    public String regenerateInviteCode(Long teamId, User leader) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new RuntimeException("Team not found"));

        if (!team.getLeader().getId().equals(leader.getId())) {
            throw new RuntimeException("Only leader can regenerate invite code");
        }

        team.setInviteCode(generateInviteCode());
        teamRepository.save(team);
        return team.getInviteCode();
    }

    private void updateTeamStatus(Team team) {
        Long memberCount = teamMemberRepository.countByTeam(team);

        if (memberCount >= MIN_TEAM_SIZE && memberCount <= MAX_TEAM_SIZE) {
            if (team.getStatus() == TeamStatus.FORMING) {
                team.setStatus(TeamStatus.READY);
            }
        } else if (memberCount < MIN_TEAM_SIZE) {
            if (team.getStatus() == TeamStatus.READY) {
                team.setStatus(TeamStatus.FORMING);
            }
        }

        teamRepository.save(team);
    }

    public boolean isTeamReady(Team team) {
        Long memberCount = teamMemberRepository.countByTeam(team);
        return memberCount >= MIN_TEAM_SIZE && memberCount <= MAX_TEAM_SIZE;
    }

    public void delete(Long id) {
        teamRepository.deleteById(id);
    }
}

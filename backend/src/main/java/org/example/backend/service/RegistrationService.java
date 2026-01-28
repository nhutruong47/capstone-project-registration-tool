package org.example.backend.service;

import lombok.RequiredArgsConstructor;
import org.example.backend.entity.*;
import org.example.backend.enums.RegistrationStatus;
import org.example.backend.enums.TeamStatus;
import org.example.backend.enums.TopicStatus;
import org.example.backend.repository.RegistrationRepository;
import org.example.backend.repository.TopicRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class RegistrationService {

    private final RegistrationRepository registrationRepository;
    private final TopicRepository topicRepository;
    private final NotificationService notificationService;

    /**
     * Register a team for a topic (FCFS - First Come First Served)
     */
    public Registration register(Team team, Long topicId) {
        Topic topic = topicRepository.findById(topicId)
                .orElseThrow(() -> new RuntimeException("Topic not found"));

        // Validate team status
        if (team.getStatus() != TeamStatus.READY) {
            throw new RuntimeException("Team must have 4-5 members to register");
        }

        // Validate topic status
        if (topic.getStatus() != TopicStatus.PUBLISHED && topic.getStatus() != TopicStatus.APPROVED) {
            throw new RuntimeException("Topic is not available for registration");
        }

        // Check if team already registered for this topic
        if (registrationRepository.existsByTeamAndTopic(team, topic)) {
            throw new RuntimeException("Team has already registered for this topic");
        }

        // Check if topic has available slots
        Long activeRegistrations = registrationRepository.countActiveRegistrations(topic);
        if (activeRegistrations >= topic.getMaxTeams()) {
            throw new RuntimeException("Topic has no available slots");
        }

        Registration registration = Registration.builder()
                .team(team)
                .topic(topic)
                .status(RegistrationStatus.PENDING)
                .build();

        Registration savedRegistration = registrationRepository.save(registration);

        // Update topic status if max teams reached
        if (activeRegistrations + 1 >= topic.getMaxTeams()) {
            topic.setStatus(TopicStatus.REGISTERED);
            topicRepository.save(topic);
        }

        // Notify supervisor
        notificationService.create(topic.getSupervisor(),
                "New Topic Registration",
                "Team " + team.getName() + " has registered for topic: " + topic.getCode(),
                "/registrations/" + savedRegistration.getId());

        return savedRegistration;
    }

    public Optional<Registration> findById(Long id) {
        return registrationRepository.findById(id);
    }

    public List<Registration> findByTopic(Topic topic) {
        return registrationRepository.findByTopic(topic);
    }

    public List<Registration> findByTeam(Team team) {
        return registrationRepository.findByTeam(team);
    }

    public List<Registration> findBySupervisor(User supervisor) {
        return registrationRepository.findBySupervisor(supervisor);
    }

    public List<Registration> findPendingBySupervisor(User supervisor) {
        return registrationRepository.findBySupervisorAndStatus(supervisor, RegistrationStatus.PENDING);
    }

    public Registration approve(Long registrationId, User supervisor) {
        Registration registration = registrationRepository.findById(registrationId)
                .orElseThrow(() -> new RuntimeException("Registration not found"));

        // Verify supervisor owns this topic
        if (!registration.getTopic().getSupervisor().getId().equals(supervisor.getId())) {
            throw new RuntimeException("Not authorized to approve this registration");
        }

        if (registration.getStatus() != RegistrationStatus.PENDING) {
            throw new RuntimeException("Registration is not pending");
        }

        registration.setStatus(RegistrationStatus.APPROVED);
        registration.setApprovedAt(LocalDateTime.now());
        Registration savedRegistration = registrationRepository.save(registration);

        // Update team status
        Team team = registration.getTeam();
        team.setStatus(TeamStatus.REGISTERED);

        // Notify team leader
        notificationService.create(team.getLeader(),
                "Registration Approved",
                "Your registration for topic " + registration.getTopic().getCode() + " has been approved!",
                "/registrations/" + registration.getId());

        return savedRegistration;
    }

    public Registration reject(Long registrationId, User supervisor, String reason) {
        Registration registration = registrationRepository.findById(registrationId)
                .orElseThrow(() -> new RuntimeException("Registration not found"));

        // Verify supervisor owns this topic
        if (!registration.getTopic().getSupervisor().getId().equals(supervisor.getId())) {
            throw new RuntimeException("Not authorized to reject this registration");
        }

        if (registration.getStatus() != RegistrationStatus.PENDING) {
            throw new RuntimeException("Registration is not pending");
        }

        registration.setStatus(RegistrationStatus.REJECTED);
        registration.setRejectReason(reason);
        registration.setRejectedAt(LocalDateTime.now());
        Registration savedRegistration = registrationRepository.save(registration);

        // Revert topic status if needed
        Topic topic = registration.getTopic();
        Long activeRegistrations = registrationRepository.countActiveRegistrations(topic);
        if (activeRegistrations < topic.getMaxTeams() && topic.getStatus() == TopicStatus.REGISTERED) {
            topic.setStatus(TopicStatus.PUBLISHED);
            topicRepository.save(topic);
        }

        // Notify team leader
        notificationService.create(registration.getTeam().getLeader(),
                "Registration Rejected",
                "Your registration for topic " + topic.getCode() + " has been rejected. Reason: " + reason,
                "/topics");

        return savedRegistration;
    }

    public Registration finalize(Long registrationId) {
        Registration registration = registrationRepository.findById(registrationId)
                .orElseThrow(() -> new RuntimeException("Registration not found"));

        if (registration.getStatus() != RegistrationStatus.APPROVED) {
            throw new RuntimeException("Registration must be approved before finalizing");
        }

        registration.setStatus(RegistrationStatus.FINALIZED);
        Registration savedRegistration = registrationRepository.save(registration);

        // Update topic and team status
        registration.getTopic().setStatus(TopicStatus.FINALIZED);
        topicRepository.save(registration.getTopic());

        Team team = registration.getTeam();
        team.setStatus(TeamStatus.FINALIZED);

        // Notify team leader
        notificationService.create(team.getLeader(),
                "Registration Finalized",
                "Your team has been officially assigned to topic: " + registration.getTopic().getCode(),
                "/registrations/" + registration.getId());

        return savedRegistration;
    }

    public void delete(Long id) {
        registrationRepository.deleteById(id);
    }
}

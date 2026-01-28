package org.example.backend.service;

import lombok.RequiredArgsConstructor;
import org.example.backend.entity.*;
import org.example.backend.enums.TopicStatus;
import org.example.backend.repository.TopicRepository;
import org.example.backend.repository.SemesterRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class TopicService {

    private final TopicRepository topicRepository;
    private final SemesterRepository semesterRepository;

    /**
     * Generate unique topic code: [Semester]-[Major][Sequence]
     * Example: SP26-SE005
     */
    private String generateTopicCode(Semester semester, String majorPrefix) {
        Long count = topicRepository.countByCodePrefix(semester, semester.getCode() + "-" + majorPrefix);
        return String.format("%s-%s%03d", semester.getCode(), majorPrefix, count + 1);
    }

    public Topic create(User supervisor, Long semesterId, String titleEn, String titleVi,
            String description, String requirements, Integer maxTeams, String majorPrefix) {
        Semester semester = semesterRepository.findById(semesterId)
                .orElseThrow(() -> new RuntimeException("Semester not found"));

        String code = generateTopicCode(semester, majorPrefix);

        Topic topic = Topic.builder()
                .code(code)
                .titleEn(titleEn)
                .titleVi(titleVi)
                .description(description)
                .requirements(requirements)
                .maxTeams(maxTeams != null ? maxTeams : 1)
                .supervisor(supervisor)
                .semester(semester)
                .status(TopicStatus.DRAFT)
                .version(1)
                .build();

        return topicRepository.save(topic);
    }

    public Optional<Topic> findById(Long id) {
        return topicRepository.findById(id);
    }

    public Optional<Topic> findByCode(String code) {
        return topicRepository.findByCode(code);
    }

    public List<Topic> findBySupervisor(User supervisor) {
        return topicRepository.findBySupervisor(supervisor);
    }

    public List<Topic> findBySemester(Semester semester) {
        return topicRepository.findBySemester(semester);
    }

    public List<Topic> findByStatus(TopicStatus status) {
        return topicRepository.findByStatus(status);
    }

    public List<Topic> findAvailableForRegistration(Semester semester) {
        return topicRepository.findAvailableForRegistration(semester);
    }

    public Topic update(Long topicId, String titleEn, String titleVi,
            String description, String requirements, Integer maxTeams) {
        Topic topic = topicRepository.findById(topicId)
                .orElseThrow(() -> new RuntimeException("Topic not found"));

        topic.setTitleEn(titleEn);
        topic.setTitleVi(titleVi);
        topic.setDescription(description);
        topic.setRequirements(requirements);
        if (maxTeams != null) {
            topic.setMaxTeams(maxTeams);
        }

        return topicRepository.save(topic);
    }

    public Topic submit(Long topicId) {
        Topic topic = topicRepository.findById(topicId)
                .orElseThrow(() -> new RuntimeException("Topic not found"));

        topic.setStatus(TopicStatus.PROCESSING);
        topic.setSubmittedAt(LocalDateTime.now());
        return topicRepository.save(topic);
    }

    public Topic updateStatus(Long topicId, TopicStatus status) {
        Topic topic = topicRepository.findById(topicId)
                .orElseThrow(() -> new RuntimeException("Topic not found"));

        topic.setStatus(status);

        if (status == TopicStatus.APPROVED) {
            topic.setApprovedAt(LocalDateTime.now());
        } else if (status == TopicStatus.PUBLISHED) {
            topic.setPublishedAt(LocalDateTime.now());
        }

        return topicRepository.save(topic);
    }

    public Topic updateAIResults(Long topicId, Boolean compliancePass, String complianceFeedback,
            Double similarityScore, String similarityDetails) {
        Topic topic = topicRepository.findById(topicId)
                .orElseThrow(() -> new RuntimeException("Topic not found"));

        topic.setAiCompliancePass(compliancePass);
        topic.setAiComplianceFeedback(complianceFeedback);
        topic.setAiSimilarityScore(similarityScore);
        topic.setAiSimilarityDetails(similarityDetails);

        // Determine status based on AI results
        if (compliancePass && (similarityScore == null || similarityScore < 80)) {
            topic.setStatus(TopicStatus.AI_PASSED);
        } else {
            topic.setStatus(TopicStatus.AI_FAILED);
        }

        return topicRepository.save(topic);
    }

    public Topic incrementVersion(Long topicId) {
        Topic topic = topicRepository.findById(topicId)
                .orElseThrow(() -> new RuntimeException("Topic not found"));

        topic.setVersion(topic.getVersion() + 1);
        topic.setStatus(TopicStatus.PROCESSING);
        return topicRepository.save(topic);
    }

    public void delete(Long id) {
        topicRepository.deleteById(id);
    }
}

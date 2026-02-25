package org.example.backend.service;

import lombok.RequiredArgsConstructor;
import org.example.backend.entity.*;
import org.example.backend.enums.TopicStatus;
import org.example.backend.repository.RegistrationPhaseRepository;
import org.example.backend.repository.SemesterRepository;
import org.example.backend.repository.TopicInheritanceRepository;
import org.example.backend.repository.TopicRepository;
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
    private final RegistrationPhaseRepository registrationPhaseRepository;
    private final TopicInheritanceRepository topicInheritanceRepository;

    /**
     * Generate unique topic code: [Semester]-[Major][Sequence]
     * Example: SP26-SE005
     */
    private String generateTopicCode(Semester semester, String majorPrefix) {
        Long count = topicRepository.countByCodePrefix(semester, semester.getCode() + "-" + majorPrefix);
        return String.format("%s-%s%03d", semester.getCode(), majorPrefix, count + 1);
    }

    /**
     * Giảng viên tạo đề tài mới trong một đợt đăng ký
     */
    public Topic create(User supervisor, Long semesterId, Long registrationPhaseId,
            String title, String description, String majorPrefix) {
        Semester semester = semesterRepository.findById(semesterId)
                .orElseThrow(() -> new RuntimeException("Semester not found"));

        RegistrationPhase phase = registrationPhaseRepository.findById(registrationPhaseId)
                .orElseThrow(() -> new RuntimeException("Registration phase not found"));

        String code = generateTopicCode(semester, majorPrefix);

        Topic topic = Topic.builder()
                .code(code)
                .title(title)
                .description(description)
                .supervisor(supervisor)
                .semester(semester)
                .registrationPhase(phase)
                .status(TopicStatus.PENDING)
                .submittedAt(LocalDateTime.now())
                .build();

        return topicRepository.save(topic);
    }

    /**
     * Nộp lại đề tài FAIL ở đợt 2 (tạo đề tài con kế thừa)
     */
    public Topic resubmit(Long parentTopicId, Long newPhaseId, String title, String description, String majorPrefix) {
        Topic parentTopic = topicRepository.findById(parentTopicId)
                .orElseThrow(() -> new RuntimeException("Parent topic not found"));

        if (parentTopic.getStatus() != TopicStatus.FAIL) {
            throw new RuntimeException("Only FAILED topics can be resubmitted");
        }

        RegistrationPhase newPhase = registrationPhaseRepository.findById(newPhaseId)
                .orElseThrow(() -> new RuntimeException("Registration phase not found"));

        // Tạo đề tài mới
        Topic childTopic = create(
                parentTopic.getSupervisor(),
                parentTopic.getSemester().getId(),
                newPhaseId,
                title != null ? title : parentTopic.getTitle(),
                description != null ? description : parentTopic.getDescription(),
                majorPrefix);

        // Tạo quan hệ kế thừa
        TopicInheritance inheritance = TopicInheritance.builder()
                .parentTopic(parentTopic)
                .childTopic(childTopic)
                .build();
        topicInheritanceRepository.save(inheritance);

        return childTopic;
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

    public List<Topic> findByRegistrationPhase(RegistrationPhase phase) {
        return topicRepository.findByRegistrationPhase(phase);
    }

    /**
     * Lấy danh sách đề tài PASS để công bố cho sinh viên
     */
    public List<Topic> findPassedTopicsBySemester(Semester semester) {
        return topicRepository.findPassedTopicsBySemester(semester);
    }

    public Topic update(Long topicId, String title, String description) {
        Topic topic = topicRepository.findById(topicId)
                .orElseThrow(() -> new RuntimeException("Topic not found"));

        if (title != null)
            topic.setTitle(title);
        if (description != null)
            topic.setDescription(description);

        return topicRepository.save(topic);
    }

    public Topic updateStatus(Long topicId, TopicStatus status) {
        Topic topic = topicRepository.findById(topicId)
                .orElseThrow(() -> new RuntimeException("Topic not found"));
        topic.setStatus(status);
        return topicRepository.save(topic);
    }

    /**
     * Cập nhật kết quả AI similarity
     */
    public Topic updateAISimilarity(Long topicId, Double similarityScore, String details) {
        Topic topic = topicRepository.findById(topicId)
                .orElseThrow(() -> new RuntimeException("Topic not found"));
        topic.setAiSimilarityScore(similarityScore);
        topic.setAiSimilarityDetails(details);
        return topicRepository.save(topic);
    }

    /**
     * Lấy lịch sử kế thừa của đề tài
     */
    public List<TopicInheritance> getInheritanceHistory(Long topicId) {
        Topic topic = topicRepository.findById(topicId)
                .orElseThrow(() -> new RuntimeException("Topic not found"));
        return topicInheritanceRepository.findByParentTopic(topic);
    }

    public void delete(Long id) {
        topicRepository.deleteById(id);
    }
}

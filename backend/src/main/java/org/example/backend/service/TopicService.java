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
     * Generate unique topic code: [Semester][Department][Sequence]
     * Example: FA25SE001, SP26SE005
     */
    private String generateTopicCode(Semester semester, String department) {
        String prefix = semester.getCode() + (department != null ? department : "SE");
        Long count = topicRepository.countByCodePrefix(semester, prefix);
        return String.format("%s%03d", prefix, count + 1);
    }

    /**
     * Giảng viên tạo đề tài mới trong một đợt đăng ký
     */
    public Topic create(User supervisor, Long semesterId, Long registrationPhaseId,
            String titleEn, String titleVi, String description, String department,
            String studentGroupInfo, Integer studentCount,
            Long supervisor2Id) {
        Semester semester = semesterRepository.findById(semesterId)
                .orElseThrow(() -> new RuntimeException("Semester not found"));

        RegistrationPhase phase = registrationPhaseRepository.findById(registrationPhaseId)
                .orElseThrow(() -> new RuntimeException("Registration phase not found"));

        String code = generateTopicCode(semester, department);

        Topic.TopicBuilder builder = Topic.builder()
                .code(code)
                .titleEn(titleEn)
                .titleVi(titleVi)
                .description(description)
                .department(department != null ? department : "SE")
                .studentGroupInfo(studentGroupInfo)
                .studentCount(studentCount)
                .supervisor(supervisor)
                .semester(semester)
                .registrationPhase(phase)
                .status(TopicStatus.PENDING)
                .submittedAt(LocalDateTime.now());

        return topicRepository.save(builder.build());
    }

    /**
     * Nộp lại đề tài FAIL ở đợt 2 (tạo đề tài con kế thừa)
     */
    public Topic resubmit(Long parentTopicId, Long newPhaseId, String titleEn, String titleVi,
            String description, String department, String studentGroupInfo) {
        Topic parentTopic = topicRepository.findById(parentTopicId)
                .orElseThrow(() -> new RuntimeException("Parent topic not found"));

        if (parentTopic.getStatus() != TopicStatus.FAIL) {
            throw new RuntimeException("Only FAILED topics can be resubmitted");
        }

        RegistrationPhase newPhase = registrationPhaseRepository.findById(newPhaseId)
                .orElseThrow(() -> new RuntimeException("Registration phase not found"));

        Semester semester = parentTopic.getSemester();
        String dept = department != null ? department : parentTopic.getDepartment();
        String code = generateTopicCode(semester, dept);

        Topic childTopic = Topic.builder()
                .code(code)
                .titleEn(titleEn != null ? titleEn : parentTopic.getTitleEn())
                .titleVi(titleVi != null ? titleVi : parentTopic.getTitleVi())
                .description(description != null ? description : parentTopic.getDescription())
                .department(dept)
                .studentGroupInfo(studentGroupInfo != null ? studentGroupInfo : parentTopic.getStudentGroupInfo())
                .studentCount(parentTopic.getStudentCount())
                .supervisor(parentTopic.getSupervisor())
                .supervisor2(parentTopic.getSupervisor2())
                .semester(semester)
                .registrationPhase(newPhase)
                .parentTopic(parentTopic)
                .status(TopicStatus.PENDING)
                .submittedAt(LocalDateTime.now())
                .build();

        childTopic = topicRepository.save(childTopic);

        TopicInheritance inheritance = TopicInheritance.builder()
                .parentTopic(parentTopic)
                .childTopic(childTopic)
                .build();
        topicInheritanceRepository.save(inheritance);

        return childTopic;
    }

    /**
     * Moderator khóa kết quả đề tài
     */
    public Topic lockTopic(Long topicId) {
        Topic topic = topicRepository.findById(topicId)
                .orElseThrow(() -> new RuntimeException("Topic not found"));

        TopicStatus status = topic.getStatus();
        if (status != TopicStatus.PASS && status != TopicStatus.FAIL && status != TopicStatus.CONSIDER) {
            throw new RuntimeException(
                    "Can only lock topics with final status (PASS/FAIL/CONSIDER). Current: " + status);
        }

        topic.setIsLocked(true);
        topic.setStatus(TopicStatus.LOCKED);
        return topicRepository.save(topic);
    }

    public List<Topic> findAll() {
        return topicRepository.findAll();
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

    public List<Topic> findPassedTopicsBySemester(Semester semester) {
        return topicRepository.findPassedTopicsBySemester(semester);
    }

    public Topic update(Long topicId, String titleEn, String titleVi, String description,
            String studentGroupInfo) {
        Topic topic = topicRepository.findById(topicId)
                .orElseThrow(() -> new RuntimeException("Topic not found"));

        if (topic.getIsLocked()) {
            throw new RuntimeException("Topic is locked. Cannot modify.");
        }

        if (titleEn != null)
            topic.setTitleEn(titleEn);
        if (titleVi != null)
            topic.setTitleVi(titleVi);
        if (description != null)
            topic.setDescription(description);
        if (studentGroupInfo != null)
            topic.setStudentGroupInfo(studentGroupInfo);

        return topicRepository.save(topic);
    }

    public Topic updateStatus(Long topicId, TopicStatus status) {
        Topic topic = topicRepository.findById(topicId)
                .orElseThrow(() -> new RuntimeException("Topic not found"));
        topic.setStatus(status);
        return topicRepository.save(topic);
    }

    public Topic updateAISimilarity(Long topicId, Double similarityScore, String details) {
        Topic topic = topicRepository.findById(topicId)
                .orElseThrow(() -> new RuntimeException("Topic not found"));
        topic.setAiSimilarityScore(similarityScore);
        topic.setAiSimilarityDetails(details);
        return topicRepository.save(topic);
    }

    public List<TopicInheritance> getInheritanceHistory(Long topicId) {
        Topic topic = topicRepository.findById(topicId)
                .orElseThrow(() -> new RuntimeException("Topic not found"));
        return topicInheritanceRepository.findByParentTopic(topic);
    }

    public void delete(Long id) {
        topicRepository.deleteById(id);
    }
}

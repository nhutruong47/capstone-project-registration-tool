package org.example.backend.service;

import lombok.RequiredArgsConstructor;
import org.example.backend.entity.*;
import org.example.backend.enums.TopicStatus;
import org.example.backend.repository.RegistrationPhaseRepository;
import org.example.backend.repository.SemesterRepository;
import org.example.backend.repository.TopicInheritanceRepository;
import org.example.backend.repository.TopicRepository;
import org.example.backend.repository.UserRepository;
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
    private final UserRepository userRepository;

    /**
     * Tự động sinh mã đề tài theo format: [Mã Ngành] + [STT] + [Kỳ] + [Năm]
     * Ví dụ: A1Spring26 (Ngành AI, STT 1, Kỳ Spring 2026)
     */
    private String generateAutoId(Semester semester, String department) {
        String deptPrefix = (department != null && !department.isEmpty()) ? department : "SE";
        
        // Count existing topics for this semester and department
        // We'll use a rough count here or a prefix count.
        Long count = topicRepository.countBySemester(semester); // Simplified, ideally should count by dept. Let's assume total count is enough or we use countBySemester
        
        // Format String: [Dept] + [Count] + [SemesterName] 
        // Example: semester.getName() = "Spring 2026" -> "Spring26"
        String semName = semester.getName().replaceAll("\\s+", "").replace("20", "");
        if (semName.length() > 8) {
            // fallback if format is unusual
            semName = semester.getCode();
        }
        
        return String.format("%s%d%s", deptPrefix, count + 1, semName);
    }


    /**
     * Sinh viên tự đề xuất đề tài mới
     */
    public Topic createByStudent(Long semesterId, Long registrationPhaseId,
            String titleEn, String titleVi, String description, String department,
            String studentGroupInfo, Integer studentCount, Long submitterId) {

        Semester semester = semesterRepository.findById(semesterId)
                .orElseThrow(() -> new RuntimeException("Semester not found"));

        RegistrationPhase phase = registrationPhaseRepository.findById(registrationPhaseId)
                .orElseThrow(() -> new RuntimeException("Registration phase not found"));

        User submitter = null;
        if (submitterId != null) {
            submitter = userRepository.findById(submitterId)
                    .orElseThrow(() -> new RuntimeException("Submitter not found"));
        }

        // Tạm thời để code rỗng hoặc code nháp, mã chính thức sẽ được cấp sau khi qua AI
        Topic.TopicBuilder builder = Topic.builder()
                .code("TEMP-" + System.currentTimeMillis()) 
                .titleEn(titleEn != null && !titleEn.isEmpty() ? titleEn : titleVi)
                .titleVi(titleVi)
                .description(description)
                .department(department != null ? department : "SE")
                .studentGroupInfo(studentGroupInfo)
                .studentCount(studentCount)
                .supervisor(null) // Chưa có giảng viên hướng dẫn chính thức
                .submitter(submitter) // Lưu người nộp đề tài
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

        if (parentTopic.getStatus() != TopicStatus.REJECTED) {
            throw new RuntimeException("Only REJECTED topics can be resubmitted");
        }

        RegistrationPhase newPhase = registrationPhaseRepository.findById(newPhaseId)
                .orElseThrow(() -> new RuntimeException("Registration phase not found"));

        Semester semester = parentTopic.getSemester();
        String dept = department != null ? department : parentTopic.getDepartment();
        String code = generateAutoId(semester, dept);

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
     * Hoàn tất đề tài (Finalized)
     */
    public Topic finalizeTopic(Long topicId, Long supervisorId) {
        Topic topic = topicRepository.findById(topicId)
                .orElseThrow(() -> new RuntimeException("Topic not found"));

        TopicStatus status = topic.getStatus();
        if (status != TopicStatus.APPROVED && status != TopicStatus.REJECTED) {
            throw new RuntimeException(
                    "Can only finalize topics with APPROVED or REJECTED statuses. Current: " + status);
        }

        if (supervisorId != null) {
            User supervisor = userRepository.findById(supervisorId)
                .orElseThrow(() -> new RuntimeException("Supervisor not found"));
            topic.setSupervisor(supervisor);
        }

        topic.setIsLocked(true);
        topic.setStatus(TopicStatus.FINALIZED);
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

    public List<Topic> findBySubmitter(Long submitterId) {
        User submitter = userRepository.findById(submitterId)
                .orElseThrow(() -> new RuntimeException("Submitter not found"));
        return topicRepository.findBySubmitter(submitter);
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

    public Topic updateAIResults(Long topicId, boolean isCompliant, Double similarityScore, String details) {
        Topic topic = topicRepository.findById(topicId)
                .orElseThrow(() -> new RuntimeException("Topic not found"));
        
        topic.setAiSimilarityScore(similarityScore);
        topic.setAiSimilarityDetails(details);
        
        StringBuilder note = new StringBuilder();
        if (topic.getFinalNote() != null) {
            note.append(topic.getFinalNote()).append("\n");
        }

        // Nếu AI test pass: Cả chất lượng và trùng lắp đều OK
        // Similarity < 80% và isCompliant = true
        if (isCompliant && (similarityScore == null || similarityScore < 80.0)) {
            String newCode = generateAutoId(topic.getSemester(), topic.getDepartment());
            topic.setCode(newCode);
            topic.setStatus(TopicStatus.WAITING_MODERATOR);
            note.append("AI Check Result: PASSED (Compliant & Unique)");
        } else {
            topic.setStatus(TopicStatus.REJECTED);
            if (!isCompliant) {
                note.append("AI Check Result: FAILED (Low quality description)\n");
            }
            if (similarityScore != null && similarityScore >= 80.0) {
                note.append("AI Check Result: FAILED (High similarity detected)\n");
            }
        }
        
        topic.setFinalNote(note.toString().trim());
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

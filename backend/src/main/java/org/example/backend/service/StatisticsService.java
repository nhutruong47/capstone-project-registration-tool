package org.example.backend.service;

import lombok.RequiredArgsConstructor;
import org.example.backend.entity.Semester;
import org.example.backend.entity.Topic;
import org.example.backend.enums.TopicStatus;
import org.example.backend.repository.SemesterRepository;
import org.example.backend.repository.TopicRepository;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class StatisticsService {

    private final TopicRepository topicRepository;
    private final SemesterRepository semesterRepository;

    /**
     * Thống kê tổng quan theo semester
     */
    public Map<String, Object> getStatsBySemester(Long semesterId) {
        Semester semester = semesterRepository.findById(semesterId)
                .orElseThrow(() -> new RuntimeException("Semester not found"));

        List<Topic> topics = topicRepository.findBySemester(semester);

        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("semesterCode", semester.getCode());
        stats.put("semesterName", semester.getName());
        stats.put("totalTopics", topics.size());

        // Đếm theo trạng thái
        Map<String, Long> statusCounts = new LinkedHashMap<>();
        for (TopicStatus status : TopicStatus.values()) {
            long count = topics.stream().filter(t -> t.getStatus() == status).count();
            statusCounts.put(status.name(), count);
        }
        stats.put("statusCounts", statusCounts);

        // Đếm theo đợt đăng ký
        Map<String, Long> phaseCounts = new LinkedHashMap<>();
        topics.stream()
                .collect(java.util.stream.Collectors.groupingBy(
                        t -> t.getRegistrationPhase().getName(),
                        java.util.stream.Collectors.counting()))
                .forEach(phaseCounts::put);
        stats.put("phaseCounts", phaseCounts);

        // Đếm theo giảng viên hướng dẫn
        Map<String, Long> supervisorCounts = new LinkedHashMap<>();
        topics.stream()
                .collect(java.util.stream.Collectors.groupingBy(
                        t -> t.getSupervisor().getFullName(),
                        java.util.stream.Collectors.counting()))
                .forEach(supervisorCounts::put);
        stats.put("supervisorCounts", supervisorCounts);

        // Tỷ lệ pass
        long passCount = statusCounts.getOrDefault("PASS", 0L) + statusCounts.getOrDefault("LOCKED", 0L);
        double passRate = topics.isEmpty() ? 0 : (double) passCount / topics.size() * 100;
        stats.put("passRate", Math.round(passRate * 100.0) / 100.0);

        return stats;
    }
}

package org.example.backend.service;

import lombok.RequiredArgsConstructor;
import org.example.backend.entity.Semester;
import org.example.backend.repository.SemesterRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class SemesterService {

    private final SemesterRepository semesterRepository;

    public Semester create(String code, String name, LocalDate startDate, LocalDate endDate) {
        if (semesterRepository.existsByCode(code)) {
            throw new RuntimeException("Semester code already exists");
        }

        Semester semester = Semester.builder()
                .code(code)
                .name(name)
                .startDate(startDate)
                .endDate(endDate)
                .isActive(false)
                .build();

        return semesterRepository.save(semester);
    }

    public Optional<Semester> findById(Long id) {
        return semesterRepository.findById(id);
    }

    public Optional<Semester> findByCode(String code) {
        return semesterRepository.findByCode(code);
    }

    public Optional<Semester> getActiveSemester() {
        return semesterRepository.findByIsActiveTrue();
    }

    public List<Semester> findAll() {
        return semesterRepository.findAll();
    }

    public Semester setActive(Long semesterId) {
        // Deactivate all semesters
        semesterRepository.findByIsActiveTrue()
                .ifPresent(s -> {
                    s.setIsActive(false);
                    semesterRepository.save(s);
                });

        // Activate the selected semester
        Semester semester = semesterRepository.findById(semesterId)
                .orElseThrow(() -> new RuntimeException("Semester not found"));
        semester.setIsActive(true);
        return semesterRepository.save(semester);
    }

    public Semester updateRegistrationPeriod(Long semesterId, LocalDateTime open, LocalDateTime close) {
        Semester semester = semesterRepository.findById(semesterId)
                .orElseThrow(() -> new RuntimeException("Semester not found"));
        semester.setRegistrationOpen(open);
        semester.setRegistrationClose(close);
        return semesterRepository.save(semester);
    }

    public Semester updateTopicSubmissionPeriod(Long semesterId, LocalDateTime open, LocalDateTime close) {
        Semester semester = semesterRepository.findById(semesterId)
                .orElseThrow(() -> new RuntimeException("Semester not found"));
        semester.setTopicSubmissionOpen(open);
        semester.setTopicSubmissionClose(close);
        return semesterRepository.save(semester);
    }

    public void delete(Long id) {
        semesterRepository.deleteById(id);
    }
}

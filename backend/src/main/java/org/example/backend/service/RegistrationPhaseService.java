package org.example.backend.service;

import lombok.RequiredArgsConstructor;
import org.example.backend.entity.RegistrationPhase;
import org.example.backend.entity.Semester;
import org.example.backend.enums.PhaseStatus;
import org.example.backend.repository.RegistrationPhaseRepository;
import org.example.backend.repository.SemesterRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class RegistrationPhaseService {

    private final RegistrationPhaseRepository registrationPhaseRepository;
    private final SemesterRepository semesterRepository;

    public RegistrationPhase create(Long semesterId, String name,
            java.time.LocalDateTime startDate,
            java.time.LocalDateTime endDate) {
        Semester semester = semesterRepository.findById(semesterId)
                .orElseThrow(() -> new RuntimeException("Semester not found"));

        RegistrationPhase phase = RegistrationPhase.builder()
                .name(name)
                .startDate(startDate)
                .endDate(endDate)
                .status(PhaseStatus.OPEN)
                .semester(semester)
                .build();

        return registrationPhaseRepository.save(phase);
    }

    public Optional<RegistrationPhase> findById(Long id) {
        return registrationPhaseRepository.findById(id);
    }

    public List<RegistrationPhase> findBySemester(Semester semester) {
        return registrationPhaseRepository.findBySemester(semester);
    }

    public List<RegistrationPhase> findOpenPhasesBySemester(Semester semester) {
        return registrationPhaseRepository.findBySemesterAndStatus(semester, PhaseStatus.OPEN);
    }

    public RegistrationPhase closePhase(Long phaseId) {
        RegistrationPhase phase = registrationPhaseRepository.findById(phaseId)
                .orElseThrow(() -> new RuntimeException("Registration phase not found"));
        phase.setStatus(PhaseStatus.CLOSED);
        return registrationPhaseRepository.save(phase);
    }

    public RegistrationPhase openPhase(Long phaseId) {
        RegistrationPhase phase = registrationPhaseRepository.findById(phaseId)
                .orElseThrow(() -> new RuntimeException("Registration phase not found"));
        phase.setStatus(PhaseStatus.OPEN);
        return registrationPhaseRepository.save(phase);
    }

    public void delete(Long id) {
        registrationPhaseRepository.deleteById(id);
    }
}

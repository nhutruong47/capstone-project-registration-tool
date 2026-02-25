package org.example.backend.repository;

import org.example.backend.entity.RegistrationPhase;
import org.example.backend.entity.Semester;
import org.example.backend.enums.PhaseStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RegistrationPhaseRepository extends JpaRepository<RegistrationPhase, Long> {

    List<RegistrationPhase> findBySemester(Semester semester);

    List<RegistrationPhase> findBySemesterAndStatus(Semester semester, PhaseStatus status);

    List<RegistrationPhase> findByStatus(PhaseStatus status);
}

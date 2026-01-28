package org.example.backend.repository;

import org.example.backend.entity.Semester;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SemesterRepository extends JpaRepository<Semester, Long> {

    Optional<Semester> findByCode(String code);

    Optional<Semester> findByIsActiveTrue();

    boolean existsByCode(String code);
}

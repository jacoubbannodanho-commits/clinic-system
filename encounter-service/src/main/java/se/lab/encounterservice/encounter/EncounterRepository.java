package se.lab.encounterservice.encounter;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface EncounterRepository extends JpaRepository<Encounter, Long> {

    List<Encounter> findByPatientId(Long patientId);

    List<Encounter> findByPractitionerIdAndDateBetween(
            Long practitionerId,
            LocalDateTime start,
            LocalDateTime end
    );
}

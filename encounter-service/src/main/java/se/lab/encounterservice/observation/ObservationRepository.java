package se.lab.encounterservice.observation;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ObservationRepository extends JpaRepository<Observation, Long> {

    List<Observation> findByEncounterId(Long encounterId);
}

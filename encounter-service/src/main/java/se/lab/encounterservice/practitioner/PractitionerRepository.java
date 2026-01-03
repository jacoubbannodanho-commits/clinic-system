package se.lab.encounterservice.practitioner;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PractitionerRepository extends JpaRepository<Practitioner, Long> {

    Optional<Practitioner> findByKeycloakUserId(String keycloakUserId);
}

package se.lab.patientservice.patient;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface PatientRepository extends JpaRepository<Patient, Long> {

    Optional<Patient> findBySsn(String ssn);

    // ny metod för name-sök (för Quarkus)
    List<Patient> findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(
            String firstNamePart,
            String lastNamePart
    );
}

package se.lab.patientservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import se.lab.patientservice.model.Patient;

public interface PatientRepository extends JpaRepository<Patient, Long> {
}

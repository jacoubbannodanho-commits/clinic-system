package se.lab.patientservice.patient;

import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class PatientService {

    private final PatientRepository repo;

    public PatientService(PatientRepository repo) {
        this.repo = repo;
    }

    public List<Patient> findAll() {
        return repo.findAll();
    }

    public Patient create(Patient p) {
        return repo.save(p);
    }

    public Patient update(Long id, Patient in) {
        Patient existing = repo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Patient saknas: " + id));

        existing.setSsn(in.getSsn());
        existing.setFirstName(in.getFirstName());
        existing.setLastName(in.getLastName());
        existing.setBirthDate(in.getBirthDate());

        return repo.save(existing);
    }

    public void delete(Long id) {
        repo.deleteById(id);
    }

    public Patient findById(Long id) {
        return repo.findById(id).orElse(null);
    }

    public Patient findBySsn(String ssn) {
        return repo.findBySsn(ssn).orElse(null);
    }
    public List<Patient> searchByName(String name) {
        if (name == null || name.isBlank()) {
            return repo.findAll();
        }
        return repo.findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(name, name);
    }


}

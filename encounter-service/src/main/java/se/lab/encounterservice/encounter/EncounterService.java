package se.lab.encounterservice.encounter;

import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class EncounterService {

    private final EncounterRepository repo;

    public EncounterService(EncounterRepository repo) {
        this.repo = repo;
    }

    public List<Encounter> all() {
        return repo.findAll();
    }

    public Encounter create(Encounter e) {
        return repo.save(e);
    }

    public List<Encounter> forPatient(Long patientId) {
        return repo.findByPatientId(patientId);
    }

    public List<Encounter> forPractitionerOnDate(Long practitionerId, LocalDate date) {
        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = date.plusDays(1).atStartOfDay();
        return repo.findByPractitionerIdAndDateBetween(practitionerId, start, end);
    }
}

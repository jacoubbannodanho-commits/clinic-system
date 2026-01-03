package se.lab.encounterservice.condition;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ConditionService {

    private final ConditionRepository repo;

    public ConditionService(ConditionRepository repo) {
        this.repo = repo;
    }

    public Condition create(Condition c) {
        return repo.save(c);
    }

    public List<Condition> forPatient(Long patientId) {
        return repo.findByPatientId(patientId);
    }

    public List<Condition> forEncounter(Long encounterId) {
        return repo.findByEncounterId(encounterId);
    }

    public List<Condition> all() {
        return repo.findAll();
    }

}

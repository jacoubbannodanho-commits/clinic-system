package se.lab.encounterservice.observation;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ObservationService {

    private final ObservationRepository repo;

    public ObservationService(ObservationRepository repo) {
        this.repo = repo;
    }

    public List<Observation> forEncounter(Long encounterId) {
        return repo.findByEncounterId(encounterId);
    }

    public Observation addToEncounter(Long encounterId, Observation in) {
        in.setEncounterId(encounterId);
        return repo.save(in);
    }
}

package se.lab.encounterservice.practitioner;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PractitionerService {

    private final PractitionerRepository repo;

    public PractitionerService(PractitionerRepository repo) {
        this.repo = repo;
    }

    public List<Practitioner> all() {
        return repo.findAll();
    }

    public Practitioner create(Practitioner p) {
        return repo.save(p);
    }

    public Practitioner findByKeycloakUserId(String keycloakUserId) {
        return repo.findByKeycloakUserId(keycloakUserId).orElse(null);
    }
}

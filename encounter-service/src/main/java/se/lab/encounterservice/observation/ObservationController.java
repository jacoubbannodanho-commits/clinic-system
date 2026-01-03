package se.lab.encounterservice.observation;

import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/encounters/{encounterId}/observations")
public class ObservationController {

    private final ObservationService service;

    public ObservationController(ObservationService service) {
        this.service = service;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('doctor','staff')")
    public List<Observation> allForEncounter(@PathVariable Long encounterId) {
        return service.forEncounter(encounterId);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('doctor','staff')")
    public Observation add(@PathVariable Long encounterId,
                           @Valid @RequestBody Observation in) {
        return service.addToEncounter(encounterId, in);
    }
}

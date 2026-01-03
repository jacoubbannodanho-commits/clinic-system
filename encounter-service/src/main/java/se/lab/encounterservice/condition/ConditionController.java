package se.lab.encounterservice.condition;

import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/conditions")
public class ConditionController {

    private final ConditionService service;

    public ConditionController(ConditionService service) {
        this.service = service;
    }

    // GET /api/conditions?patientId=...
    @GetMapping(params = "patientId")
    @PreAuthorize("hasAnyRole('doctor','staff','patient')")
    public List<Condition> byPatient(@RequestParam Long patientId) {
        return service.forPatient(patientId);
    }

    // GET /api/conditions?encounterId=...
    @GetMapping(params = "encounterId")
    @PreAuthorize("hasAnyRole('doctor','staff','patient')")
    public List<Condition> byEncounter(@RequestParam Long encounterId) {
        return service.forEncounter(encounterId);
    }

    // GET /api/conditions?code=...
    @GetMapping(params = "code")
    @PreAuthorize("hasAnyRole('doctor','staff')")
    public List<Condition> byCode(@RequestParam String code) {
        // enkel variant: filtrera i minnet
        return service.all().stream()
                .filter(c -> c.getCode() != null &&
                        c.getCode().equalsIgnoreCase(code))
                .toList();
    }

    // POST /api/conditions – skapa diagnos kopplad till patient + encounter
    @PostMapping
    @PreAuthorize("hasAnyRole('doctor','staff')")
    public Condition create(@Valid @RequestBody Condition in) {
        return service.create(in);
    }
}

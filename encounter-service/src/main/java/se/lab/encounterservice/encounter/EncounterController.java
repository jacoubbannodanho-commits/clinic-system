package se.lab.encounterservice.encounter;

import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/encounters")
public class EncounterController {

    private final EncounterService service;

    public EncounterController(EncounterService service) {
        this.service = service;
    }

    // GET /api/encounters eller /api/encounters?patientId=1
    @GetMapping
    @PreAuthorize("hasAnyRole('doctor','staff')")
    public List<Encounter> all(@RequestParam(required = false) Long patientId) {
        if (patientId != null) {
            return service.forPatient(patientId);
        }
        return service.all();
    }
    // GET /api/encounters?patientId=...
    @GetMapping(params = "patientId")
    @PreAuthorize("hasAnyRole('doctor','staff','patient')")
    public List<Encounter> forPatient(@RequestParam Long patientId) {
        return service.forPatient(patientId);
    }

    // GET /api/encounters/by-practitioner?practitionerId=1&date=2025-11-27
    @GetMapping("/by-practitioner")
    @PreAuthorize("hasAnyRole('doctor','staff')")
    public List<Encounter> forPractitionerOnDate(
            @RequestParam Long practitionerId,
            @RequestParam String date   // format: yyyy-MM-dd
    ) {
        LocalDate d = LocalDate.parse(date);
        return service.forPractitionerOnDate(practitionerId, d);
    }

    @PostMapping
    @PreAuthorize("hasRole('doctor')")
    public Encounter create(@Valid @RequestBody Encounter e) {
        return service.create(e);
    }
}

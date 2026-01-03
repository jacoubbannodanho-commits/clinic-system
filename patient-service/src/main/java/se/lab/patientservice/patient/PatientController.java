package se.lab.patientservice.patient;

import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/patients")
public class PatientController {

    private final PatientService service;

    public PatientController(PatientService service) {
        this.service = service;
    }

    // GET /api/patients?name=...&ssn=...
    @GetMapping
    @PreAuthorize("hasAnyRole('doctor','staff','patient')")
    public List<Patient> allOrSearch(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String ssn
    ) {
        if (ssn != null && !ssn.isBlank()) {
            Patient p = service.findBySsn(ssn);
            return p == null ? List.of() : List.of(p);
        }
        if (name != null && !name.isBlank()) {
            return service.searchByName(name);
        }
        return service.findAll();
    }

    // GET /api/patients/{id}
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('doctor','staff','patient')")
    public Patient byId(@PathVariable Long id) {
        return service.findById(id);
    }

    // POST /api/patients
    @PostMapping
    @PreAuthorize("hasAnyRole('doctor','staff')")
    public Patient create(@Valid @RequestBody Patient in) {
        return service.create(in);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('doctor','staff')")
    public Patient update(@PathVariable Long id, @Valid @RequestBody Patient in) {
        return service.update(id, in);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('doctor','staff')")
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}

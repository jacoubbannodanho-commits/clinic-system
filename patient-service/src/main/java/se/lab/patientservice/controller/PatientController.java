package se.lab.patientservice.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import se.lab.patientservice.model.Patient;
import se.lab.patientservice.repository.PatientRepository;

import java.util.List;

@RestController
@RequestMapping("/patients")
public class PatientController {

    private final PatientRepository repo;

    public PatientController(PatientRepository repo) {
        this.repo = repo;
    }

    @GetMapping
    public List<Patient> list() {
        return repo.findAll();
    }

    @PostMapping
    public Patient create(@RequestBody Patient patient) {
        return repo.save(patient);
    }

    @GetMapping("{id}")
    public ResponseEntity<Patient> get(@PathVariable Long id) {
        return repo.findById(id).map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }
}

package se.lab.encounterservice.practitioner;

import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/practitioners")
public class PractitionerController {

    private final PractitionerService service;

    public PractitionerController(PractitionerService service) {
        this.service = service;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('doctor','staff')")
    public List<Practitioner> all() {
        return service.all();
    }

    // enkel skapande-endpoint, kan användas av t.ex. admin i början
    @PostMapping
    @PreAuthorize("hasAnyRole('doctor','staff')")
    public Practitioner create(@Valid @RequestBody Practitioner p) {
        return service.create(p);
    }
}

package se.lab.encounterservice.practitioner;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;

@Entity
public class Practitioner {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // koppling till Keycloak-användaren (sub eller username)
    @NotBlank
    @Column(unique = true, nullable = false)
    private String keycloakUserId;

    @NotBlank
    private String firstName;

    @NotBlank
    private String lastName;

    // t.ex. "DOCTOR" eller "STAFF"
    @NotBlank
    private String role;

    private String specialty;

    public Long getId() { return id; }

    public String getKeycloakUserId() { return keycloakUserId; }
    public void setKeycloakUserId(String keycloakUserId) { this.keycloakUserId = keycloakUserId; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getSpecialty() { return specialty; }
    public void setSpecialty(String specialty) { this.specialty = specialty; }
}

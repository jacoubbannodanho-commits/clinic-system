package se.lab.encounterservice.condition;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

@Entity
@Table(name = "conditions")   // ← FIX (ändrat från "condition")
public class Condition {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    private Long patientId;

    @NotNull
    private Long encounterId;

    @NotBlank
    private String code;

    private String description;

    private LocalDate onsetDate;

    private String status;

    public Long getId() { return id; }

    public Long getPatientId() { return patientId; }
    public void setPatientId(Long patientId) { this.patientId = patientId; }

    public Long getEncounterId() { return encounterId; }
    public void setEncounterId(Long encounterId) { this.encounterId = encounterId; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public LocalDate getOnsetDate() { return onsetDate; }
    public void setOnsetDate(LocalDate onsetDate) { this.onsetDate = onsetDate; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}

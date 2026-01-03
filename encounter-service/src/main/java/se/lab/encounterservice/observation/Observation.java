package se.lab.encounterservice.observation;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

@Entity
public class Observation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    private Long encounterId;

    @NotBlank
    private String code;      // t.ex. "blood-pressure", "temperature"

    @NotBlank
    private String value;     // t.ex. "120/80", "37.5"

    private String unit;      // mmHg, °C, etc.

    private LocalDateTime recordedAt = LocalDateTime.now();

    public Long getId() { return id; }

    public Long getEncounterId() { return encounterId; }
    public void setEncounterId(Long encounterId) { this.encounterId = encounterId; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getValue() { return value; }
    public void setValue(String value) { this.value = value; }

    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }

    public LocalDateTime getRecordedAt() { return recordedAt; }
    public void setRecordedAt(LocalDateTime recordedAt) { this.recordedAt = recordedAt; }
}

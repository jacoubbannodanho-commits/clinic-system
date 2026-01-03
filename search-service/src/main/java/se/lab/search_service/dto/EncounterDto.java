package se.lab.search_service.dto;

import java.time.LocalDateTime;

public class EncounterDto {
    public Long id;
    public Long patientId;
    public Long practitionerId;
    public LocalDateTime date;
    public String notes;
}

package se.lab.search_service.dto;

import java.time.LocalDate;

public class ConditionDto {
    public Long id;
    public Long patientId;
    public Long encounterId;
    public String code;
    public String description;
    public LocalDate onsetDate;
    public String status;
}

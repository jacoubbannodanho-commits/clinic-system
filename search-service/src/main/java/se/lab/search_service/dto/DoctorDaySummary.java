package se.lab.search_service.dto;

import java.time.LocalDate;
import java.util.List;

public class DoctorDaySummary {
    public PractitionerDto practitioner;
    public LocalDate date;
    public List<DoctorDayEncounterResult> encounters;
}

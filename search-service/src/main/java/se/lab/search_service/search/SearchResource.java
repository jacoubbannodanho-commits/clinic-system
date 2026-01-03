package se.lab.search_service.search;

import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.resteasy.reactive.ClientWebApplicationException;
import se.lab.search_service.client.EncounterClient;
import se.lab.search_service.client.PatientClient;
import se.lab.search_service.client.PractitionerClient;
import se.lab.search_service.dto.*;

import java.time.LocalDate;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Path("/api/search")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SearchResource {

    @Inject @RestClient PatientClient patientClient;
    @Inject @RestClient EncounterClient encounterClient;
    @Inject @RestClient PractitionerClient practitionerClient;

    private static final Pattern DIAG_PATTERN =
            Pattern.compile("(?i)Diagnosis:\\s*([^\\r\\n]+)");

    // ============================
    // 1) Sök patienter (name/ssn/conditionCode)
    // ============================
    @GET
    @Path("/patients")
    public Uni<List<PatientSearchResult>> searchPatients(
            @HeaderParam("Authorization") String auth,
            @QueryParam("name") String name,
            @QueryParam("ssn") String ssn,
            @QueryParam("conditionCode") String conditionCode
    ) {
        requireAuth(auth);

        if (ssn != null && !ssn.isBlank()) {
            return mapAuthErrors(patientClient.getAll(auth))
                    .onItem().transform(list ->
                            list.stream()
                                    .filter(p -> ssn.equals(p.ssn))
                                    .collect(Collectors.toList())
                    )
                    .onItem().transformToUni(patients -> attachEncounterConditions(auth, patients, null));
        }

        if (name != null && !name.isBlank()) {
            return mapAuthErrors(patientClient.searchByName(auth, name))
                    .onItem().transformToUni(patients -> attachEncounterConditions(auth, patients, null));
        }

        if (conditionCode != null && !conditionCode.isBlank()) {
            return mapAuthErrors(patientClient.getAll(auth))
                    .onItem().transformToUni(patients -> attachEncounterConditions(auth, patients, conditionCode.trim()));
        }

        return mapAuthErrors(patientClient.getAll(auth))
                .onItem().transformToUni(patients -> attachEncounterConditions(auth, patients, null));
    }

    /**
     * För varje patient: hämta encounters och bygg conditions[] av diagnoskoder i notes.
     * Om filterCode != null: returnera endast patienter som har den diagnoskoden.
     */
    private Uni<List<PatientSearchResult>> attachEncounterConditions(String auth, List<PatientDto> patients, String filterCode) {
        if (patients == null || patients.isEmpty()) return Uni.createFrom().item(List.of());

        List<Uni<PatientSearchResult>> unis = new ArrayList<>();
        for (PatientDto p : patients) {
            Uni<PatientSearchResult> u =
                    mapAuthErrors(encounterClient.forPatient(auth, p.id))
                            .onItem().transform(encounters -> {
                                List<ConditionDto> conds = deriveConditionsFromEncounters(p.id, encounters);

                                if (filterCode != null && !filterCode.isBlank()) {
                                    boolean ok = conds.stream().anyMatch(c -> filterCode.equalsIgnoreCase(c.code));
                                    if (!ok) return null;
                                }

                                PatientSearchResult r = new PatientSearchResult();
                                r.patient = p;
                                r.conditions = conds;
                                return r;
                            });

            unis.add(u);
        }

        return Uni.combine().all().unis(unis)
                .with(list -> {
                    @SuppressWarnings("unchecked")
                    List<PatientSearchResult> raw = (List<PatientSearchResult>) list;
                    return raw.stream().filter(Objects::nonNull).collect(Collectors.toList());
                });
    }

    private List<ConditionDto> deriveConditionsFromEncounters(Long patientId, List<EncounterDto> encounters) {
        if (encounters == null || encounters.isEmpty()) return List.of();

        Map<String, ConditionDto> byCode = new LinkedHashMap<>();

        for (EncounterDto e : encounters) {
            String notes = e.notes; // era data: "Diagnosis: 1111\n\nträna"
            if (notes == null) continue;

            Matcher m = DIAG_PATTERN.matcher(notes);
            if (!m.find()) continue;

            String code = m.group(1).trim();
            String description;

            int idx = notes.indexOf("\n\n");
            if (idx >= 0 && idx + 2 < notes.length()) {
                description = notes.substring(idx + 2).trim();
            } else {
                description = "";
            }

            byCode.computeIfAbsent(code, k -> {
                ConditionDto c = new ConditionDto();
                c.id = (long) Math.abs(Objects.hash(patientId, k)); // syntetiskt men stabilt för UI
                c.patientId = patientId;
                c.code = k;
                c.description = description;
                return c;
            });
        }

        return new ArrayList<>(byCode.values());
    }

    // ============================
    // 2) Sök via läkare + dag (ID)
    // ============================
    @GET
    @Path("/practitioners/{practitionerId}/day")
    public Uni<DoctorDaySummary> searchDoctorDayById(
            @HeaderParam("Authorization") String auth,
            @PathParam("practitionerId") Long practitionerId,
            @QueryParam("date") String date
    ) {
        requireAuth(auth);
        requireDate(date);

        Uni<PractitionerDto> practitionerUni =
                mapAuthErrors(practitionerClient.all(auth))
                        .onItem().transform(list -> list.stream()
                                .filter(p -> Objects.equals(p.id, practitionerId))
                                .findFirst()
                                .orElse(null)
                        );

        return buildDoctorDaySummary(auth, practitionerUni, practitionerId, date);
    }

    // ============================
    // 3) ✅ NYTT krav: Sök via läkare NAMN + dag
    //    GET /api/search/practitioners/day?name=...&date=yyyy-MM-dd
    // ============================
    @GET
    @Path("/practitioners/day")
    public Uni<List<DoctorDaySummary>> searchDoctorDayByName(
            @HeaderParam("Authorization") String auth,
            @QueryParam("name") String name,
            @QueryParam("date") String date
    ) {
        requireAuth(auth);
        requireDate(date);

        if (name == null || name.isBlank()) {
            throw new WebApplicationException("Missing name query param", 400);
        }

        String needle = name.trim().toLowerCase(Locale.ROOT);

        return mapAuthErrors(practitionerClient.all(auth))
                .onItem().transform(list -> list.stream()
                        .filter(p -> matchesName(p, needle))
                        .collect(Collectors.toList()))
                .onItem().transformToUni(matches -> {
                    if (matches.isEmpty()) return Uni.createFrom().item(List.of());

                    List<Uni<DoctorDaySummary>> unis = new ArrayList<>();
                    for (PractitionerDto p : matches) {
                        Uni<PractitionerDto> prUni = Uni.createFrom().item(p);
                        unis.add(buildDoctorDaySummary(auth, prUni, p.id, date));
                    }

                    return Uni.combine().all().unis(unis)
                            .with(x -> (List<DoctorDaySummary>) x);
                });
    }

    private boolean matchesName(PractitionerDto p, String needle) {
        String fn = (p.firstName == null ? "" : p.firstName).trim().toLowerCase(Locale.ROOT);
        String ln = (p.lastName == null ? "" : p.lastName).trim().toLowerCase(Locale.ROOT);
        String full = (fn + " " + ln).trim();
        return fn.contains(needle) || ln.contains(needle) || full.contains(needle);
    }

    private Uni<DoctorDaySummary> buildDoctorDaySummary(String auth, Uni<PractitionerDto> practitionerUni, Long practitionerId, String date) {
        Uni<List<EncounterDto>> encountersUni =
                mapAuthErrors(encounterClient.forPractitionerOnDate(auth, practitionerId, date));

        Uni<List<DoctorDayEncounterResult>> detailsUni = encountersUni
                .onItem().transformToUni(encounters -> {
                    if (encounters == null || encounters.isEmpty()) {
                        return Uni.createFrom().item(List.of());
                    }

                    List<Uni<DoctorDayEncounterResult>> unis = new ArrayList<>();
                    for (EncounterDto e : encounters) {
                        Uni<DoctorDayEncounterResult> u =
                                mapAuthErrors(patientClient.getById(auth, e.patientId))
                                        .onItem().transform(p -> {
                                            DoctorDayEncounterResult r = new DoctorDayEncounterResult();
                                            r.patient = p;
                                            r.encounter = e;
                                            return r;
                                        });
                        unis.add(u);
                    }

                    return Uni.combine().all().unis(unis)
                            .with(list -> (List<DoctorDayEncounterResult>) list);
                });

        return Uni.combine().all().unis(practitionerUni, detailsUni).asTuple()
                .onItem().transform(tuple -> {
                    DoctorDaySummary summary = new DoctorDaySummary();
                    summary.practitioner = tuple.getItem1();
                    summary.date = LocalDate.parse(date);
                    summary.encounters = tuple.getItem2();
                    return summary;
                });
    }

    // ============================
    // Helpers
    // ============================
    private void requireAuth(String auth) {
        if (auth == null || auth.isBlank()) {
            throw new WebApplicationException("Missing Authorization header", 401);
        }
    }

    private void requireDate(String date) {
        if (date == null || date.isBlank()) {
            throw new WebApplicationException("Missing date query param (yyyy-MM-dd)", 400);
        }
    }

    private <T> Uni<T> mapAuthErrors(Uni<T> uni) {
        return uni.onFailure(ClientWebApplicationException.class)
                .transform(t -> {
                    ClientWebApplicationException ex = (ClientWebApplicationException) t;
                    int status = ex.getResponse().getStatus();
                    if (status == 401 || status == 403) {
                        return new WebApplicationException("Downstream denied (" + status + ")", status);
                    }
                    return ex;
                });
    }
}

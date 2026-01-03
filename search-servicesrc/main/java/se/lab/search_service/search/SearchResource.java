package se.lab.search_service.search;

import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Context;
import io.smallrye.mutiny.Uni;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import se.lab.search_service.client.PatientClient;
import se.lab.search_service.client.PractitionerClient;
import se.lab.search_service.client.EncounterClient;
import se.lab.search_service.client.ConditionClient;
import se.lab.search_service.dto.PatientDto;
import se.lab.search_service.dto.EncounterDto;
import se.lab.search_service.dto.ConditionDto;
import java.util.List;
import java.util.stream.Collectors;
import java.util.Collections;

@Path("/api/search")
@Produces(MediaType.APPLICATION_JSON)
public class SearchResource {

    @Inject
    @RestClient
    PatientClient patientClient;

    @Inject
    @RestClient
    PractitionerClient practitionerClient;

    @Inject
    @RestClient
    EncounterClient encounterClient;

    @Inject
    @RestClient
    ConditionClient conditionClient;

    @Context
    HttpHeaders httpHeaders;

    private String authHeader() {
        List<String> authHeaders = httpHeaders.getRequestHeader("Authorization");
        return (authHeaders != null && !authHeaders.isEmpty()) ? authHeaders.get(0) : null;
    }

    /**
     * Search for patients by name and optionally by condition code.
     * Only doctors and staff can perform this search.
     */
    @GET
    @Path("/patients")
    @RolesAllowed({"Doctor","Staff"})
    public Uni<List<PatientDto>> searchPatients(@QueryParam("name") String name,
                                                @QueryParam("condition") String condition) {
        String auth = authHeader();
        Uni<List<PatientDto>> patientsUni;
        if (name != null && !name.isBlank()) {
            patientsUni = patientClient.searchByName(auth, name);
        } else {
            patientsUni = patientClient.getAll(auth);
        }

        if (condition != null && !condition.isBlank()) {
            return conditionClient.byCode(auth, condition)
                    .onItem().transformToUni(conditionList -> patientsUni
                            .onItem().transform(patients -> {
                                return patients.stream()
                                        .filter(p -> conditionList.stream().anyMatch(c -> c.getPatientId().equals(p.getId())))
                                        .collect(Collectors.toList());
                            })
                    );
        } else {
            return patientsUni;
        }
    }

    /**
     * Retrieve patients and their encounters for a practitioner on a specific date.
     * Only doctors and staff can perform this search.
     */
    @GET
    @Path("/practitioners/{practitionerId}/patients")
    @RolesAllowed({"Doctor","Staff"})
    public Uni<List<PatientDto>> searchPatientsByPractitioner(@PathParam("practitionerId") Long practitionerId,
                                                              @QueryParam("date") String date) {
        String auth = authHeader();
        return encounterClient.forPractitionerOnDate(auth, practitionerId, date)
                .onItem().transformToUni(encounters -> {
                    if (encounters == null || encounters.isEmpty()) {
                        return Uni.createFrom().item(Collections.emptyList());
                    }
                    List<Long> patientIds = encounters.stream()
                            .map(e -> e.getPatientId())
                            .distinct()
                            .collect(Collectors.toList());
                    List<Uni<PatientDto>> uniList = patientIds.stream()
                            .map(id -> patientClient.getById(auth, id))
                            .collect(Collectors.toList());
                    return Uni.combine().all().unis(uniList).combinedWith(list -> (List<PatientDto>) list);
                });
    }
}
comit

package se.lab.search_service.client;

import io.smallrye.mutiny.Uni;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import se.lab.search_service.dto.EncounterDto;

import java.util.List;

@Path("/api/encounters")
@RegisterRestClient(configKey = "encounter-api")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public interface EncounterClient {

    @GET
    Uni<List<EncounterDto>> forPatient(
            @HeaderParam("Authorization") String auth,
            @QueryParam("patientId") Long patientId
    );

    @GET
    Uni<List<EncounterDto>> forPractitionerOnDate(
            @HeaderParam("Authorization") String auth,
            @QueryParam("practitionerId") Long practitionerId,
            @QueryParam("date") String date // yyyy-MM-dd
    );
}

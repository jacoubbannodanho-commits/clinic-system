package se.lab.search_service.client;

import io.smallrye.mutiny.Uni;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import se.lab.search_service.dto.ConditionDto;

import java.util.List;

@Path("/api/conditions")
@RegisterRestClient(configKey = "condition-api")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public interface ConditionClient {

    @GET
    Uni<List<ConditionDto>> forPatient(
            @HeaderParam("Authorization") String auth,
            @QueryParam("patientId") Long patientId
    );

    @GET
    Uni<List<ConditionDto>> forEncounter(
            @HeaderParam("Authorization") String auth,
            @QueryParam("encounterId") Long encounterId
    );

    @GET
    Uni<List<ConditionDto>> byCode(
            @HeaderParam("Authorization") String auth,
            @QueryParam("code") String code
    );
}

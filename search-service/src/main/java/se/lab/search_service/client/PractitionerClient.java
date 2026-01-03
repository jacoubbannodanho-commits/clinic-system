package se.lab.search_service.client;

import io.smallrye.mutiny.Uni;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import se.lab.search_service.dto.PractitionerDto;

import java.util.List;

@Path("/api/practitioners")
@RegisterRestClient(configKey = "practitioner-api")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public interface PractitionerClient {

    @GET
    Uni<List<PractitionerDto>> all(@HeaderParam("Authorization") String auth);
}

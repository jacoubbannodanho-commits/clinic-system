package se.lab.search_service.client;

import io.smallrye.mutiny.Uni;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import se.lab.search_service.dto.PatientDto;

import java.util.List;

@Path("/api/patients")
@RegisterRestClient(configKey = "patient-api")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public interface PatientClient {

    @GET
    Uni<List<PatientDto>> getAll(@HeaderParam("Authorization") String auth);

    @GET
    @Path("/{id}")
    Uni<PatientDto> getById(@HeaderParam("Authorization") String auth, @PathParam("id") Long id);

    @GET
    Uni<List<PatientDto>> searchByName(@HeaderParam("Authorization") String auth, @QueryParam("name") String name);
}

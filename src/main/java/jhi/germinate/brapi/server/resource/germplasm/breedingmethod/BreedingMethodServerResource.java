package jhi.germinate.brapi.server.resource.germplasm.breedingmethod;

import uk.ac.hutton.ics.brapi.resource.base.*;
import uk.ac.hutton.ics.brapi.resource.germplasm.germplasm.BreedingMethod;
import uk.ac.hutton.ics.brapi.server.base.BaseServerResource;
import uk.ac.hutton.ics.brapi.server.germplasm.breedingmethod.BrapiBreedingMethodServerResource;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.sql.SQLException;

@Path("brapi/v2/breedingmethods")
public class BreedingMethodServerResource extends BaseServerResource implements BrapiBreedingMethodServerResource
{
	@GET
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public BaseResult<ArrayResult<BreedingMethod>> getBreedingMethods()
		throws IOException, SQLException {
		return new BaseResult<>(new ArrayResult<>(), page, pageSize, 0);
	}

	@GET
	@Path("/{breedingMethodDbId}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public BaseResult<BreedingMethod> getBreedingMethodById(@PathParam("breedingMethodDbId") String breedingMethodDbId)
		throws IOException, SQLException {
		return new BaseResult<>(null, page, pageSize, 0);
	}
}

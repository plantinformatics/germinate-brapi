package jhi.germinate.brapi.server.resource.genotyping.call;

import jhi.germinate.server.util.Secured;
import uk.ac.hutton.ics.brapi.resource.base.*;
import uk.ac.hutton.ics.brapi.resource.genotyping.call.*;
import uk.ac.hutton.ics.brapi.server.base.BaseServerResource;
import uk.ac.hutton.ics.brapi.server.genotyping.call.BrapiSearchCallSetServerResource;

import jakarta.annotation.security.PermitAll;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import java.io.IOException;
import java.sql.SQLException;

@Path("brapi/v2/search/callsets")
@Secured
@PermitAll
public class SearchCallSetServerResource extends BaseServerResource implements BrapiSearchCallSetServerResource
{
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response postCallSetSearch(CallSetSearch search)
		throws SQLException, IOException
	{
		resp.sendError(Response.Status.NOT_IMPLEMENTED.getStatusCode());
		return null;
	}

	@GET
	@Path("/{searchResultsDbId}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public BaseResult<ArrayResult<CallSet>> getCallSetSearchAsync(@PathParam("searchResultsDbId") String searchResultsDbId)
		throws SQLException, IOException
	{
		resp.sendError(Response.Status.NOT_IMPLEMENTED.getStatusCode());
		return null;
	}
}

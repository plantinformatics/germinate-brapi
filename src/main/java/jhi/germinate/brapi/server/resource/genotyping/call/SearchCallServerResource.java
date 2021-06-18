package jhi.germinate.brapi.server.resource.genotyping.call;

import jhi.germinate.server.util.Secured;
import uk.ac.hutton.ics.brapi.resource.base.*;
import uk.ac.hutton.ics.brapi.resource.genotyping.call.*;
import uk.ac.hutton.ics.brapi.server.base.BaseServerResource;
import uk.ac.hutton.ics.brapi.server.genotyping.call.BrapiSearchCallServerResource;

import javax.annotation.security.PermitAll;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.io.IOException;
import java.sql.SQLException;

@Path("brapi/v2/search/calls")
@Secured
@PermitAll
public class SearchCallServerResource extends BaseServerResource implements BrapiSearchCallServerResource
{
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response postCallSearch(CallSearch search)
		throws SQLException, IOException
	{
		resp.sendError(Response.Status.NOT_IMPLEMENTED.getStatusCode());
		return null;
	}

	@GET
	@Path("/{searchResultsDbId}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public BaseResult<ArrayResult<Call>> getCallSearchAsync(@PathParam("searchResultsDbId") String searchResultsDbId)
		throws SQLException, IOException
	{
		resp.sendError(Response.Status.NOT_IMPLEMENTED.getStatusCode());
		return null;
	}
}

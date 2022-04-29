package jhi.germinate.brapi.server.resource.genotyping.variant;

import jhi.germinate.server.util.Secured;
import uk.ac.hutton.ics.brapi.resource.base.*;
import uk.ac.hutton.ics.brapi.resource.genotyping.variant.*;
import uk.ac.hutton.ics.brapi.server.base.BaseServerResource;
import uk.ac.hutton.ics.brapi.server.genotyping.variant.BrapiSearchVariantSetServerResource;

import jakarta.annotation.security.PermitAll;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import java.io.IOException;
import java.sql.SQLException;

@Path("brapi/v2/search/variantsets")
@Secured
@PermitAll
public class SearchVariantSetServerResource extends BaseServerResource implements BrapiSearchVariantSetServerResource
{
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response postVariantSetSearch(VariantSetSearch search)
		throws SQLException, IOException
	{
		resp.sendError(Response.Status.NOT_IMPLEMENTED.getStatusCode());
		return null;
	}

	@GET
	@Path("/{searchResultsDbId}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public BaseResult<ArrayResult<VariantSet>> getVariantSetSearchAsync(@PathParam("searchResultsDbId") String searchResultsDbId)
		throws SQLException, IOException
	{
		resp.sendError(Response.Status.NOT_IMPLEMENTED.getStatusCode());
		return null;
	}
}

package jhi.germinate.brapi.server.resource.genotyping.call;

import jhi.germinate.server.util.Secured;
import uk.ac.hutton.ics.brapi.resource.base.BaseResult;
import uk.ac.hutton.ics.brapi.resource.genotyping.call.*;
import uk.ac.hutton.ics.brapi.server.base.BaseServerResource;
import uk.ac.hutton.ics.brapi.server.genotyping.call.BrapiCallServerResource;

import jakarta.annotation.security.PermitAll;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;

import java.io.IOException;
import java.sql.SQLException;

@Path("brapi/v2/calls")
@Secured
@PermitAll
public class CallServerResource extends BaseServerResource implements BrapiCallServerResource
{
	@Override
	@GET
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public BaseResult<CallResult<Call>> getCalls(@QueryParam("callSetDbId") String callSetDbId, @QueryParam("variantDbId") String variantDbId, @QueryParam("variantSetDbId") String variantSetDbId, @QueryParam("expandHomozygotes") Boolean expandHomozygotes, @QueryParam("unknownString") String unknownString, @QueryParam("sepPhased") String sepPhased, @QueryParam("sepUnphased") String sepUnphased)
		throws IOException, SQLException
	{
		resp.sendError(Response.Status.SERVICE_UNAVAILABLE.getStatusCode());
		return null;
	}

	@Override
	@PUT
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public BaseResult<CallResult<Call>> putCalls(Call[] calls)
		throws IOException, SQLException
	{
		resp.sendError(Response.Status.SERVICE_UNAVAILABLE.getStatusCode());
		return null;
	}
}

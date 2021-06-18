package jhi.germinate.brapi.server.resource.genotyping.call;

import jhi.germinate.server.util.Secured;
import uk.ac.hutton.ics.brapi.resource.base.TokenBaseResult;
import uk.ac.hutton.ics.brapi.resource.genotyping.call.*;
import uk.ac.hutton.ics.brapi.server.base.BaseServerResource;
import uk.ac.hutton.ics.brapi.server.genotyping.call.BrapiCallServerResource;

import javax.annotation.security.PermitAll;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.io.IOException;
import java.sql.SQLException;

@Path("brapi/v2/calls")
@Secured
@PermitAll
public class CallServerResource extends BaseServerResource implements BrapiCallServerResource
{
	@GET
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public TokenBaseResult<CallResult<Call>> getCalls(@QueryParam("callSetDbId") String callSetDbId,
													  @QueryParam("variantDbId") String variantDbId,
													  @QueryParam("variantSetDbId") String variantSetDbId,
													  @QueryParam("expandHomozygotes") String expandHomozygotes,
													  @QueryParam("unknownString") String unknownString,
													  @QueryParam("sepPhased") String sepPhased,
													  @QueryParam("sepUnphased") String sepUnphased)
		throws IOException, SQLException
	{
		resp.sendError(Response.Status.SERVICE_UNAVAILABLE.getStatusCode());
		return null;
	}
}

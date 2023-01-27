package jhi.germinate.brapi.server.resource.core.program;

import jakarta.annotation.security.PermitAll;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import jhi.germinate.resource.enums.UserType;
import jhi.germinate.server.util.Secured;
import uk.ac.hutton.ics.brapi.resource.base.*;
import uk.ac.hutton.ics.brapi.resource.core.program.Program;
import uk.ac.hutton.ics.brapi.server.base.BaseServerResource;
import uk.ac.hutton.ics.brapi.server.core.program.BrapiProgramServerResource;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Collections;

@Path("brapi/v2/programs")
public class ProgramServerResource extends BaseServerResource implements BrapiProgramServerResource
{
	private static final Program THE_PROGRAM = new Program()
		.setProgramDbId("1")
		.setProgramName("Germinate")
		.setProgramType("STANDARD");

	@Override
	@GET
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Secured
	@PermitAll
	public BaseResult<ArrayResult<Program>> getPrograms(@QueryParam("abbreviation") String abbreviation,
												 @QueryParam("commonCropName") String commonCropName,
												 @QueryParam("programDbId") String programDbId,
												 @QueryParam("programName") String programName,
												 @QueryParam("programType") String programType,
												 @QueryParam("externalReferenceId") String externalReferenceId,
												 @QueryParam("externalReferenceSource") String externalReferenceSource)
		throws SQLException, IOException {
		return new BaseResult<>(new ArrayResult<Program>().setData(Collections.singletonList(THE_PROGRAM)), page, pageSize, 1);
	}

	@Override
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Secured(UserType.DATA_CURATOR)
	public BaseResult<ArrayResult<Program>> postPrograms(Program[] newPrograms)
		throws SQLException, IOException {
		resp.sendError(Response.Status.NOT_IMPLEMENTED.getStatusCode());
		return null;
	}

	@Override
	@GET
	@Path("/{programDbId}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Secured
	@PermitAll
	public BaseResult<Program> getProgramById(@PathParam("programDbId") String programDbId)
		throws SQLException, IOException {
		return new BaseResult<>(THE_PROGRAM, page, pageSize, 1);
	}

	@Override
	@PUT
	@Path("/{programDbId}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Secured(UserType.DATA_CURATOR)
	public BaseResult<Program> putProgramById(@PathParam("programDbId") String programDbId, Program program)
		throws SQLException, IOException {
		resp.sendError(Response.Status.NOT_IMPLEMENTED.getStatusCode());
		return null;
	}
}

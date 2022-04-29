package jhi.germinate.brapi.server.resource.core.study;

import jhi.germinate.server.util.Secured;
import org.jooq.DSLContext;

import java.io.IOException;
import java.sql.*;
import java.util.List;

import jhi.germinate.server.Database;
import uk.ac.hutton.ics.brapi.resource.base.*;
import uk.ac.hutton.ics.brapi.server.base.BaseServerResource;
import uk.ac.hutton.ics.brapi.server.core.study.BrapiStudyTypesServerResource;

import jakarta.annotation.security.PermitAll;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;

import static jhi.germinate.server.database.codegen.tables.Datasettypes.*;

/**
 * @author Sebastian Raubach
 */
@Path("brapi/v2/studytypes")
@Secured
@PermitAll
public class StudyTypesServerResource extends BaseServerResource implements BrapiStudyTypesServerResource
{
	@GET
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public BaseResult<ArrayResult<String>> getStudyTypes()
		throws IOException, SQLException
	{
		try (Connection conn = Database.getConnection())
		{
			DSLContext context = Database.getContext(conn);
			List<String> result = context.select(DATASETTYPES.DESCRIPTION)
										 .hint("SQL_CALC_FOUND_ROWS")
										 .from(DATASETTYPES)
										 .limit(pageSize)
										 .offset(pageSize * page)
										 .fetchInto(String.class);

			long totalCount = context.fetchOne("SELECT FOUND_ROWS()").into(Long.class);
			return new BaseResult<>(new ArrayResult<String>()
				.setData(result), page, pageSize, totalCount);
		}
	}
}

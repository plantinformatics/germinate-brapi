package jhi.germinate.brapi.server.resource.core.study;

import org.jooq.DSLContext;
import org.restlet.data.Status;
import org.restlet.resource.*;

import java.sql.*;
import java.util.List;

import jhi.germinate.server.Database;
import uk.ac.hutton.ics.brapi.resource.base.*;
import uk.ac.hutton.ics.brapi.server.base.BaseServerResource;
import uk.ac.hutton.ics.brapi.server.core.study.BrapiStudyTypesServerResource;

import static jhi.germinate.server.database.codegen.tables.Datasettypes.*;

/**
 * @author Sebastian Raubach
 */
public class StudyTypesServerResource extends BaseServerResource implements BrapiStudyTypesServerResource
{
	@Get
	public BaseResult<ArrayResult<String>> getStudyTypes()
	{
		try (Connection conn = Database.getConnection();
			 DSLContext context = Database.getContext(conn))
		{
			List<String> result = context.select(DATASETTYPES.DESCRIPTION)
										 .hint("SQL_CALC_FOUND_ROWS")
										 .from(DATASETTYPES)
										 .limit(pageSize)
										 .offset(pageSize * currentPage)
										 .fetchInto(String.class);

			long totalCount = context.fetchOne("SELECT FOUND_ROWS()").into(Long.class);
			return new BaseResult<>(new ArrayResult<String>()
				.setData(result), currentPage, pageSize, totalCount);
		}
		catch (SQLException e)
		{
			e.printStackTrace();
			throw new ResourceException(Status.SERVER_ERROR_INTERNAL);
		}
	}
}

package jhi.germinate.brapi.server.resource.core.study;

import org.jooq.DSLContext;
import org.restlet.data.Status;
import org.restlet.resource.ResourceException;

import java.sql.*;
import java.util.List;

import jhi.germinate.brapi.resource.ArrayResult;
import jhi.germinate.brapi.resource.base.BaseResult;
import jhi.germinate.brapi.server.resource.BaseServerResource;
import jhi.germinate.server.Database;

import static jhi.germinate.server.database.tables.Datasettypes.*;

/**
 * @author Sebastian Raubach
 */
public class StudyTypesServerResource extends BaseServerResource<ArrayResult<String>>
{
	@Override
	public BaseResult<ArrayResult<String>> getJson()
	{
		try (Connection conn = Database.getConnection();
			 DSLContext context = Database.getContext(conn))
		{
			List<String> result = context.select(DATASETTYPES.DESCRIPTION)
										 .hint("SQL_CALC_FOUND_ROWS")
										 .from(DATASETTYPES)
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

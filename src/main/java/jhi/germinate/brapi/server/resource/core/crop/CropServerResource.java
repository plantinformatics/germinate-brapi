package jhi.germinate.brapi.server.resource.core.crop;

import org.jooq.DSLContext;
import org.restlet.data.Status;
import org.restlet.resource.ResourceException;

import java.sql.*;
import java.util.List;

import jhi.germinate.brapi.resource.ArrayResult;
import jhi.germinate.brapi.resource.base.BaseResult;
import jhi.germinate.brapi.server.resource.BaseServerResource;
import jhi.germinate.server.Database;

import static jhi.germinate.server.database.tables.Taxonomies.*;

/**
 * @author Sebastian Raubach
 */
public class CropServerResource extends BaseServerResource<ArrayResult<String>>
{
	@Override
	public BaseResult<ArrayResult<String>> getJson()
	{
		try (Connection conn = Database.getConnection();
			 DSLContext context = Database.getContext(conn))
		{
			List<String> crops = context.selectDistinct(TAXONOMIES.CROPNAME)
										.hint("SQL_CALC_FOUND_ROWS")
										.from(TAXONOMIES)
										.limit(pageSize)
										.offset(pageSize * currentPage)
										.fetch(TAXONOMIES.CROPNAME);

			long totalCount = context.fetchOne("SELECT FOUND_ROWS()").into(Long.class);
			return new BaseResult<>(new ArrayResult<String>()
				.setData(crops), currentPage, pageSize, totalCount);
		}
		catch (SQLException e)
		{
			e.printStackTrace();
			throw new ResourceException(Status.SERVER_ERROR_INTERNAL);
		}
	}
}

package jhi.germinate.brapi.server.resource.core.season;

import org.jooq.DSLContext;
import org.restlet.data.Status;
import org.restlet.resource.*;

import java.sql.*;
import java.util.List;

import jhi.germinate.brapi.resource.ArrayResult;
import jhi.germinate.brapi.resource.base.BaseResult;
import jhi.germinate.brapi.resource.season.Season;
import jhi.germinate.server.Database;
import jhi.germinate.server.auth.*;

/**
 * @author Sebastian Raubach
 */
public class SeasonServerResource extends SeasonBaseServerResource<ArrayResult<Season>>
{
	@Post
	@MinUserType(UserType.DATA_CURATOR)
	public BaseResult<ArrayResult<Season>> postJson(Season newSeason)
	{
		throw new ResourceException(Status.SERVER_ERROR_NOT_IMPLEMENTED);
	}

	@Override
	public BaseResult<ArrayResult<Season>> getJson()
	{
		try (Connection conn = Database.getConnection();
			 DSLContext context = Database.getContext(conn))
		{
			List<Season> seasons = getSeasons(context, null);

			long totalCount = context.fetchOne("SELECT FOUND_ROWS()").into(Long.class);
			return new BaseResult<>(new ArrayResult<Season>()
				.setData(seasons), currentPage, pageSize, totalCount);
		}
		catch (SQLException e)
		{
			e.printStackTrace();
			throw new ResourceException(Status.SERVER_ERROR_INTERNAL);
		}
	}
}

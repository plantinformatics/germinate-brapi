package jhi.germinate.brapi.server.resource.core.season;

import org.jooq.DSLContext;
import org.restlet.data.Status;
import org.restlet.resource.*;

import java.sql.*;
import java.util.List;

import jhi.germinate.brapi.resource.*;
import jhi.germinate.brapi.resource.base.BaseResult;
import jhi.germinate.brapi.resource.season.SeasonResult;
import jhi.germinate.server.Database;
import jhi.germinate.server.auth.*;

/**
 * @author Sebastian Raubach
 */
public class SeasonServerResource extends SeasonBaseServerResource<ArrayResult<SeasonResult>>
{
	@Post
	@MinUserType(UserType.DATA_CURATOR)
	public BaseResult<ArrayResult<SeasonResult>> postJson(SeasonResult newSeason)
	{
		throw new ResourceException(Status.SERVER_ERROR_NOT_IMPLEMENTED);
	}

	@Override
	public BaseResult<ArrayResult<SeasonResult>> getJson()
	{
		try (Connection conn = Database.getConnection();
			 DSLContext context = Database.getContext(conn))
		{
			List<SeasonResult> seasons = getSeasons(context, null);

			long totalCount = context.fetchOne("SELECT FOUND_ROWS()").into(Long.class);
			return new BaseResult<>(new ArrayResult<SeasonResult>()
				.setData(seasons), currentPage, pageSize, totalCount);
		}
		catch (SQLException e)
		{
			e.printStackTrace();
			throw new ResourceException(Status.SERVER_ERROR_INTERNAL);
		}
	}
}

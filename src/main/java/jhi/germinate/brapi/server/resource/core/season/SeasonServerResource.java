package jhi.germinate.brapi.server.resource.core.season;

import org.jooq.DSLContext;
import org.restlet.data.Status;
import org.restlet.resource.*;

import java.sql.*;
import java.util.List;

import jhi.germinate.server.Database;
import jhi.germinate.server.auth.*;
import uk.ac.hutton.ics.brapi.resource.base.*;
import uk.ac.hutton.ics.brapi.resource.core.season.Season;
import uk.ac.hutton.ics.brapi.server.core.season.BrapiSeasonServerResource;

/**
 * @author Sebastian Raubach
 */
public class SeasonServerResource extends SeasonBaseServerResource implements BrapiSeasonServerResource
{
	@Post
	@MinUserType(UserType.DATA_CURATOR)
	public BaseResult<ArrayResult<Season>> postSeasons(Season[] newSeason)
	{
		throw new ResourceException(Status.SERVER_ERROR_NOT_IMPLEMENTED);
	}

	@Get
	public BaseResult<ArrayResult<Season>> getSeasons()
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

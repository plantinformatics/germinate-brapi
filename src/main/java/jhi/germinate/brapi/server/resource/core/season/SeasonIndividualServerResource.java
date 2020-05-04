package jhi.germinate.brapi.server.resource.core.season;

import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.restlet.data.Status;
import org.restlet.resource.*;

import java.sql.*;
import java.util.*;

import jhi.germinate.brapi.resource.base.BaseResult;
import jhi.germinate.brapi.resource.season.Season;
import jhi.germinate.server.Database;
import jhi.germinate.server.auth.*;
import jhi.germinate.server.util.*;

import static jhi.germinate.server.database.tables.Datasets.*;

/**
 * @author Sebastian Raubach
 */
public class SeasonIndividualServerResource extends SeasonBaseServerResource<Season>
{
	private String seasonDbId;

	@Override
	public void doInit()
	{
		super.doInit();

		try
		{
			this.seasonDbId = getRequestAttributes().get("seasonDbId").toString();
		}
		catch (Exception e)
		{
		}
	}

	@Put
	@MinUserType(UserType.DATA_CURATOR)
	public BaseResult<Season> putJson(Season newSeason)
	{
		throw new ResourceException(Status.SERVER_ERROR_NOT_IMPLEMENTED);
	}

	@Override
	public BaseResult<Season> getJson()
	{
		try (Connection conn = Database.getConnection();
			 DSLContext context = Database.getContext(conn))
		{
			List<Season> seasons = getSeasons(context, Collections.singletonList(DSL.year(DATASETS.DATE_START).cast(String.class).eq(seasonDbId)));

			return new BaseResult<>(CollectionUtils.isEmpty(seasons) ? null : seasons.get(0), currentPage, pageSize, 1);
		}
		catch (SQLException e)
		{
			e.printStackTrace();
			throw new ResourceException(Status.SERVER_ERROR_INTERNAL);
		}
	}
}

package jhi.germinate.brapi.server.resource.core.location;

import org.jooq.*;
import org.jooq.tools.StringUtils;
import org.restlet.data.Status;
import org.restlet.resource.*;

import java.sql.*;
import java.util.*;

import jhi.germinate.brapi.resource.ArrayResult;
import jhi.germinate.brapi.resource.base.BaseResult;
import jhi.germinate.brapi.resource.location.Location;
import jhi.germinate.server.Database;
import jhi.germinate.server.auth.*;

import static jhi.germinate.server.database.tables.ViewTableLocations.*;

/**
 * @author Sebastian Raubach
 */
public class LocationServerResource extends LocationBaseResource<ArrayResult<Location>>
{
	public static final String PARAM_LOCATION_TYPE = "locationType";

	private String locationType;

	@Override
	public void doInit()
	{
		super.doInit();

		this.locationType = getQueryValue(PARAM_LOCATION_TYPE);
	}

	@Post
	@MinUserType(UserType.DATA_CURATOR)
	public BaseResult<ArrayResult<Location>> postJson(Location[] newLocations)
	{
		throw new ResourceException(Status.SERVER_ERROR_NOT_IMPLEMENTED);
	}

	@Override
	public BaseResult<ArrayResult<Location>> getJson()
	{
		try (Connection conn = Database.getConnection();
			 DSLContext context = Database.getContext(conn))
		{
			List<Condition> conditions = new ArrayList<>();

			if (!StringUtils.isEmpty(locationType))
				conditions.add(VIEW_TABLE_LOCATIONS.LOCATION_TYPE.eq(locationType));

			List<Location> result = getLocations(context, conditions);

			long totalCount = context.fetchOne("SELECT FOUND_ROWS()").into(Long.class);
			return new BaseResult<>(new ArrayResult<Location>()
				.setData(result), currentPage, pageSize, totalCount);
		}
		catch (SQLException e)
		{
			e.printStackTrace();
			throw new ResourceException(Status.SERVER_ERROR_INTERNAL);
		}
	}
}

package jhi.germinate.brapi.server.resource.core.location;

import org.jooq.DSLContext;
import org.restlet.data.Status;
import org.restlet.resource.*;

import java.sql.*;
import java.util.*;

import jhi.germinate.brapi.resource.base.BaseResult;
import jhi.germinate.brapi.resource.location.Location;
import jhi.germinate.server.Database;
import jhi.germinate.server.auth.*;
import jhi.germinate.server.util.*;

import static jhi.germinate.server.database.tables.ViewTableLocations.*;

/**
 * @author Sebastian Raubach
 */
public class LocationIndividualServerResource extends LocationBaseResource<Location>
{
	private String locationDbId;

	@Override
	public void doInit()
	{
		super.doInit();

		try
		{
			this.locationDbId = getRequestAttributes().get("locationDbId").toString();
		}
		catch (Exception e)
		{
		}
	}

	@Put
	@MinUserType(UserType.DATA_CURATOR)
	public BaseResult<Location> postJson(Location newLocation)
	{
		throw new ResourceException(Status.SERVER_ERROR_NOT_IMPLEMENTED);
	}

	@Override
	public BaseResult<Location> getJson()
	{
		if (StringUtils.isEmpty(locationDbId))
			throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST);

		try (Connection conn = Database.getConnection();
			 DSLContext context = Database.getContext(conn))
		{
			List<Location> result = getLocations(context, Collections.singletonList(VIEW_TABLE_LOCATIONS.LOCATION_ID.cast(String.class).eq(locationDbId)));

			return new BaseResult<>(CollectionUtils.isEmpty(result) ? null : result.get(0), currentPage, pageSize, 1);
		}
		catch (SQLException e)
		{
			e.printStackTrace();
			throw new ResourceException(Status.SERVER_ERROR_INTERNAL);
		}
	}
}

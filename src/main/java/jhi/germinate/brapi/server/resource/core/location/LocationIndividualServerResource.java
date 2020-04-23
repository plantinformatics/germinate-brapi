package jhi.germinate.brapi.server.resource.core.location;

import org.jooq.DSLContext;
import org.restlet.data.Status;
import org.restlet.resource.*;

import java.sql.*;
import java.util.*;

import jhi.germinate.brapi.resource.location.LocationResult;
import jhi.germinate.brapi.resource.base.BaseResult;
import jhi.germinate.server.Database;
import jhi.germinate.server.auth.*;
import jhi.germinate.server.util.*;

import static jhi.germinate.server.database.tables.ViewTableLocations.*;

/**
 * @author Sebastian Raubach
 */
public class LocationIndividualServerResource extends LocationBaseResource<LocationResult>
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
	public BaseResult<LocationResult> postJson(LocationResult newLocation)
	{
		throw new ResourceException(Status.SERVER_ERROR_NOT_IMPLEMENTED);
	}

	@Override
	public BaseResult<LocationResult> getJson()
	{
		if (StringUtils.isEmpty(locationDbId))
			throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST);

		try (Connection conn = Database.getConnection();
			 DSLContext context = Database.getContext(conn))
		{
			List<LocationResult> result = getLocations(context, Collections.singletonList(VIEW_TABLE_LOCATIONS.LOCATION_ID.cast(String.class).eq(locationDbId)));

			long totalCount = context.fetchOne("SELECT FOUND_ROWS()").into(Long.class);
			return new BaseResult<>(CollectionUtils.isEmpty(result) ? null : result.get(0), currentPage, pageSize, totalCount);
		}
		catch (SQLException e)
		{
			e.printStackTrace();
			throw new ResourceException(Status.SERVER_ERROR_INTERNAL);
		}
	}
}

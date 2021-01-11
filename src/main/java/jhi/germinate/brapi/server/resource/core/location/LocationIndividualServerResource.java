package jhi.germinate.brapi.server.resource.core.location;

import org.jooq.DSLContext;
import org.restlet.data.Status;
import org.restlet.resource.*;

import java.sql.*;
import java.util.*;

import jhi.germinate.server.Database;
import jhi.germinate.server.auth.*;
import jhi.germinate.server.util.CollectionUtils;
import uk.ac.hutton.ics.brapi.resource.base.BaseResult;
import uk.ac.hutton.ics.brapi.resource.core.location.Location;
import uk.ac.hutton.ics.brapi.server.core.location.BrapiLocationIndividualServerResource;

import static jhi.germinate.server.database.codegen.tables.ViewTableLocations.*;

/**
 * @author Sebastian Raubach
 */
public class LocationIndividualServerResource extends LocationBaseResource implements BrapiLocationIndividualServerResource
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
	public BaseResult<Location> putLocationById(Location newLocation)
	{
		throw new ResourceException(Status.SERVER_ERROR_NOT_IMPLEMENTED);
	}

	@Get
	public BaseResult<Location> getLocationById()
	{
		try (DSLContext context = Database.getContext())
		{
			List<Location> result = getLocations(context, Collections.singletonList(VIEW_TABLE_LOCATIONS.LOCATION_ID.cast(String.class).eq(locationDbId)));

			return new BaseResult<>(CollectionUtils.isEmpty(result) ? null : result.get(0), currentPage, pageSize, 1);
		}
	}
}

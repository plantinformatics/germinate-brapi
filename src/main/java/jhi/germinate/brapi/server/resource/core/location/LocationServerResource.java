package jhi.germinate.brapi.server.resource.core.location;

import org.jooq.*;
import org.jooq.tools.StringUtils;
import org.restlet.data.Status;
import org.restlet.resource.*;

import java.sql.*;
import java.util.*;

import jhi.germinate.server.Database;
import jhi.germinate.server.auth.*;
import uk.ac.hutton.ics.brapi.resource.base.*;
import uk.ac.hutton.ics.brapi.resource.core.location.Location;
import uk.ac.hutton.ics.brapi.server.core.location.BrapiLocationServerResource;

import static jhi.germinate.server.database.codegen.tables.ViewTableLocations.*;

/**
 * @author Sebastian Raubach
 */
public class LocationServerResource extends LocationBaseResource implements BrapiLocationServerResource
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
	public BaseResult<ArrayResult<Location>> postLocations(Location[] newLocations)
	{
		throw new ResourceException(Status.SERVER_ERROR_NOT_IMPLEMENTED);
	}

	@Get
	public BaseResult<ArrayResult<Location>> getLocations()
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

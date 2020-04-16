package jhi.germinate.brapi.server.resource.core.location;

import org.jooq.*;
import org.jooq.tools.StringUtils;
import org.restlet.data.Status;
import org.restlet.resource.ResourceException;

import java.math.BigDecimal;
import java.sql.*;
import java.util.List;
import java.util.stream.Collectors;

import jhi.germinate.brapi.resource.*;
import jhi.germinate.brapi.resource.base.BaseResult;
import jhi.germinate.brapi.server.resource.BaseServerResource;
import jhi.germinate.server.Database;
import jhi.germinate.server.database.tables.pojos.ViewTableLocations;

import static jhi.germinate.server.database.tables.ViewTableLocations.*;

/**
 * @author Sebastian Raubach
 */
public class LocationServerResource extends BaseServerResource<ArrayResult<LocationResult>>
{
	public static final String PARAM_LOCATION_TYPE = "locationType";

	private String locationType;

	@Override
	public void doInit()
	{
		super.doInit();

		this.locationType = getQueryValue(PARAM_LOCATION_TYPE);
	}

	@Override
	public BaseResult<ArrayResult<LocationResult>> getJson()
	{
		try (Connection conn = Database.getConnection();
			 DSLContext context = Database.getContext(conn))
		{
			SelectJoinStep<Record> step = context.select()
												 .hint("SQL_CALC_FOUND_ROWS")
												 .from(VIEW_TABLE_LOCATIONS);

			if (!StringUtils.isEmpty(locationType))
				step.where(VIEW_TABLE_LOCATIONS.LOCATION_TYPE.eq(locationType));

			List<ViewTableLocations> locations = step.limit(pageSize)
													 .offset(pageSize * currentPage)
													 .fetchInto(ViewTableLocations.class);

			List<LocationResult> result = locations.stream()
												   .map(r -> {
													   // Set all the easy fields
													   LocationResult location = new LocationResult()
														   .setAbbreviation(r.getLocationNameShort())
														   .setCoordinateUncertainty(toString(r.getLocationCoordinateUncertainty()))
														   .setCountryCode(r.getCountryCode3())
														   .setCountryName(r.getCountryName())
														   .setLocationDbId(toString(r.getLocationId()))
														   .setLocationName(r.getLocationName())
														   .setLocationType(r.getLocationType());

													   // Then take care of the lat, lng and elv
													   BigDecimal lat = r.getLocationLatitude();
													   BigDecimal lng = r.getLocationLongitude();
													   BigDecimal elv = r.getLocationElevation();

													   if (lat != null && lng != null)
													   {
														   double[] coordinates;

														   if (elv != null)
															   coordinates = new double[]{lng.doubleValue(), lat.doubleValue(), elv.doubleValue()};
														   else
															   coordinates = new double[]{lng.doubleValue(), lat.doubleValue()};

														   location.setCoordinates(new LocationResult.Coordinates()
															   .setType("Feature")
															   .setGeometry(new LocationResult.Geometry()
																   .setCoordinates(coordinates)
																   .setType("Point")));
													   }

													   return location;
												   })
												   .collect(Collectors.toList());

			long totalCount = context.fetchOne("SELECT FOUND_ROWS()").into(Long.class);
			return new BaseResult<>(new ArrayResult<LocationResult>()
				.setData(result), currentPage, pageSize, totalCount);
		}
		catch (SQLException e)
		{
			e.printStackTrace();
			throw new ResourceException(Status.SERVER_ERROR_INTERNAL);
		}
	}
}

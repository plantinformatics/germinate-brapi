package jhi.germinate.brapi.server.resource.core.location;

import org.jooq.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

import jhi.germinate.server.database.codegen.tables.pojos.ViewTableLocations;
import jhi.germinate.server.util.StringUtils;
import uk.ac.hutton.ics.brapi.resource.core.location.*;
import uk.ac.hutton.ics.brapi.server.base.BaseServerResource;

import static jhi.germinate.server.database.codegen.tables.ViewTableLocations.*;

/**
 * @author Sebastian Raubach
 */
public abstract class LocationBaseResource extends BaseServerResource
{
	protected List<Location> getLocations(DSLContext context, List<Condition> conditions)
	{
		SelectJoinStep<Record> step = context.select()
											 .hint("SQL_CALC_FOUND_ROWS")
											 .from(VIEW_TABLE_LOCATIONS);

		if (conditions != null)
		{
			for (Condition condition : conditions)
				step.where(condition);
		}

		List<ViewTableLocations> locations = step.limit(pageSize)
												 .offset(pageSize * page)
												 .fetchInto(ViewTableLocations.class);

		return locations.stream()
						.map(r -> {
							// Set all the easy fields
							Location location = new Location()
								.setAbbreviation(r.getLocationNameShort())
								.setCoordinateUncertainty(StringUtils.toString(r.getLocationCoordinateUncertainty()))
								.setCountryCode(r.getCountryCode3())
								.setCountryName(r.getCountryName())
								.setLocationDbId(StringUtils.toString(r.getLocationId()))
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

								location.setCoordinatesPoint(new CoordinatesPoint()
									.setType("Feature")
									.setGeometry(new GeometryPoint()
										.setCoordinates(coordinates)
										.setType("Point")));
							}

							return location;
						})
						.collect(Collectors.toList());
	}
}

package jhi.germinate.brapi.server.resource.core.location;

import org.jooq.*;
import org.jooq.impl.DSL;
import org.restlet.data.Status;
import org.restlet.resource.*;

import java.sql.*;
import java.util.*;
import java.util.stream.Collectors;

import jhi.germinate.server.Database;
import jhi.germinate.server.util.CollectionUtils;
import uk.ac.hutton.ics.brapi.resource.base.*;
import uk.ac.hutton.ics.brapi.resource.core.location.*;
import uk.ac.hutton.ics.brapi.server.core.location.BrapiSearchLocationServerResource;

import static jhi.germinate.server.database.codegen.tables.ViewTableLocations.*;

/**
 * @author Sebastian Raubach
 */
public class SearchLocationServerResource extends LocationBaseResource implements BrapiSearchLocationServerResource
{
	public static String buildSqlPolygon(Double[][][] points)
	{
		StringBuilder builder = new StringBuilder();

		builder.append("MULTIPOLYGON(");

		if (!CollectionUtils.isEmpty(points))
		{
			builder.append("((");

			List<List<Double[]>> bounds = Arrays.stream(points)
												.map(p -> new ArrayList<>(Arrays.asList(p)))
												.collect(Collectors.toList());

			// Add the start as end point
			bounds.forEach(l -> l.add(l.get(0)));

			builder.append(bounds.stream()
								 .map(p -> p.stream().map(l -> l[0] + " " + l[1]).collect(Collectors.joining(", ")))
								 .collect(Collectors.joining(")), ((")));

			builder.append("))");
		}

		builder.append(")");

		return builder.toString();
	}

	@Post
	public BaseResult<ArrayResult<Location>> postLocationSearch(LocationSearch search)
	{
		if (search == null)
			throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST);

		try (DSLContext context = Database.getContext())
		{
			List<Condition> conditions = new ArrayList<>();

			if (!CollectionUtils.isEmpty(search.getAbbreviations()))
				conditions.add(VIEW_TABLE_LOCATIONS.LOCATION_NAME_SHORT.in(search.getAbbreviations()));
			if (search.getAltitudeMin() != null)
				conditions.add(VIEW_TABLE_LOCATIONS.LOCATION_ELEVATION.cast(Double.class).ge(search.getAltitudeMin()));
			if (search.getAltitudeMax() != null)
				conditions.add(VIEW_TABLE_LOCATIONS.LOCATION_ELEVATION.cast(Double.class).le(search.getAltitudeMax()));
			if (search.getCoordinates() != null && search.getCoordinates().getGeometry() != null && Objects.equals(search.getCoordinates().getGeometry().getType(), "Polygon"))
				conditions.add(DSL.condition("ST_CONTAINS(ST_GeomFromText({0}), ST_GeomFromText (CONCAT( 'POINT(', `view_table_locations`.`location_longitude`, ' ', `view_table_locations`.`location_latitude`, ')')))", buildSqlPolygon(search.getCoordinates().getGeometry().getCoordinates())));
			if (!CollectionUtils.isEmpty(search.getCountryCodes()))
				conditions.add(VIEW_TABLE_LOCATIONS.COUNTRY_CODE3.in(search.getCountryCodes()));
			if (!CollectionUtils.isEmpty(search.getCountryNames()))
				conditions.add(VIEW_TABLE_LOCATIONS.COUNTRY_NAME.in(search.getCountryNames()));
			if (!CollectionUtils.isEmpty(search.getLocationDbIds()))
				conditions.add(VIEW_TABLE_LOCATIONS.LOCATION_ID.cast(String.class).in(search.getLocationDbIds()));
			if (!CollectionUtils.isEmpty(search.getLocationDbIds()))
				conditions.add(VIEW_TABLE_LOCATIONS.LOCATION_NAME.in(search.getLocationNames()));
			if (!CollectionUtils.isEmpty(search.getLocationDbIds()))
				conditions.add(VIEW_TABLE_LOCATIONS.LOCATION_TYPE.in(search.getLocationTypes()));

			List<Location> result = getLocations(context, conditions);

			long totalCount = context.fetchOne("SELECT FOUND_ROWS()").into(Long.class);
			return new BaseResult<>(new ArrayResult<Location>()
				.setData(result), currentPage, pageSize, totalCount);
		}
	}

	@Post
	public BaseResult<SearchResult> postLocationSearchAsync(LocationSearch locationSearch)
	{
		throw new ResourceException(Status.SERVER_ERROR_NOT_IMPLEMENTED);
	}

	@Get
	public BaseResult<ArrayResult<Location>> getLocationSearchAsync()
	{
		throw new ResourceException(Status.SERVER_ERROR_NOT_IMPLEMENTED);
	}
}

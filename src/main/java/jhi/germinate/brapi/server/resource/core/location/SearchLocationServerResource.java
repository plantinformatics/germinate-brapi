package jhi.germinate.brapi.server.resource.core.location;

import org.jooq.*;
import org.jooq.impl.DSL;
import org.restlet.data.Status;
import org.restlet.resource.*;

import java.sql.*;
import java.util.*;
import java.util.stream.Collectors;

import jhi.germinate.brapi.resource.ArrayResult;
import jhi.germinate.brapi.resource.base.BaseResult;
import jhi.germinate.brapi.resource.location.*;
import jhi.germinate.server.Database;
import jhi.germinate.server.util.CollectionUtils;

import static jhi.germinate.server.database.tables.ViewTableLocations.*;

/**
 * @author Sebastian Raubach
 */
public class SearchLocationServerResource extends LocationBaseResource<ArrayResult<LocationResult>>
{
	@Override
	public BaseResult<ArrayResult<LocationResult>> getJson()
	{
		throw new ResourceException(Status.SERVER_ERROR_NOT_IMPLEMENTED);
	}

	@Post
	public BaseResult<ArrayResult<LocationResult>> postJson(LocationSearch search)
	{
		try (Connection conn = Database.getConnection();
			 DSLContext context = Database.getContext(conn))
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

			List<LocationResult> result = getLocations(context, conditions);

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
}

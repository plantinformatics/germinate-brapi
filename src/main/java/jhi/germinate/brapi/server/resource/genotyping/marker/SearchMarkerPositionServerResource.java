package jhi.germinate.brapi.server.resource.genotyping.marker;

import org.jooq.*;
import org.restlet.data.Status;
import org.restlet.resource.*;

import java.sql.*;
import java.util.List;

import jhi.germinate.brapi.resource.*;
import jhi.germinate.brapi.resource.base.BaseResult;
import jhi.germinate.brapi.server.resource.BaseServerResource;
import jhi.germinate.server.Database;
import jhi.germinate.server.util.CollectionUtils;

import static jhi.germinate.server.database.tables.Mapdefinitions.*;
import static jhi.germinate.server.database.tables.Maps.*;
import static jhi.germinate.server.database.tables.Markers.*;

/**
 * @author Sebastian Raubach
 */
public class SearchMarkerPositionServerResource extends BaseServerResource<ArrayResult<MarkerPositionResult>>
{
	@Override
	public BaseResult<ArrayResult<MarkerPositionResult>> getJson()
	{
		throw new ResourceException(Status.SERVER_ERROR_NOT_IMPLEMENTED);
	}

	@Post
	public BaseResult<ArrayResult<MarkerPositionResult>> postJson(MarkerPositionSearch search)
	{
		try (Connection conn = Database.getConnection();
			 DSLContext context = Database.getContext(conn))
		{
			SelectJoinStep<?> step = context.select(
				MAPDEFINITIONS.CHROMOSOME.as("linkageGroupName"),
				MAPS.ID.as("mapDbId"),
				MAPS.NAME.as("mapName"),
				MAPDEFINITIONS.DEFINITION_START.as("position"),
				MARKERS.ID.as("variantDbId"),
				MARKERS.MARKER_NAME.as("variantName")
			)
											.hint("SQL_CALC_FOUND_ROWS")
											.from(MAPS)
											.leftJoin(MAPDEFINITIONS).on(MAPDEFINITIONS.MAP_ID.eq(MAPS.ID))
											.leftJoin(MARKERS).on(MARKERS.ID.eq(MAPDEFINITIONS.MARKER_ID));

			step.where(MAPS.VISIBILITY.eq(true));

			if (!CollectionUtils.isEmpty(search.getMapDbIds()))
				step.where(MAPS.ID.cast(String.class).in(search.getMapDbIds()));
			if (!CollectionUtils.isEmpty(search.getLinkageGroupNames()))
				step.where(MAPDEFINITIONS.CHROMOSOME.in(search.getLinkageGroupNames()));
			if (!CollectionUtils.isEmpty(search.getVariantDbIds()))
				step.where(MARKERS.ID.cast(String.class).in(search.getVariantDbIds()));
			if (search.getMaxPosition() != null)
				step.where(MAPDEFINITIONS.DEFINITION_START.le(search.getMaxPosition()));
			if (search.getMinPosition() != null)
				step.where(MAPDEFINITIONS.DEFINITION_START.ge(search.getMinPosition()));

			List<MarkerPositionResult> result = step.fetchInto(MarkerPositionResult.class);

			long totalCount = context.fetchOne("SELECT FOUND_ROWS()").into(Long.class);
			return new BaseResult<>(new ArrayResult<MarkerPositionResult>()
				.setData(result), currentPage, pageSize, totalCount);
		}
		catch (SQLException e)
		{
			e.printStackTrace();
			throw new ResourceException(Status.SERVER_ERROR_INTERNAL);
		}
	}
}

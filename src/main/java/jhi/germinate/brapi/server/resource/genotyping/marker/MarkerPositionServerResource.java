package jhi.germinate.brapi.server.resource.genotyping.marker;

import org.jooq.*;
import org.restlet.data.Status;
import org.restlet.resource.ResourceException;

import java.sql.*;
import java.util.List;

import jhi.germinate.brapi.resource.*;
import jhi.germinate.brapi.resource.base.BaseResult;
import jhi.germinate.brapi.server.resource.BaseServerResource;
import jhi.germinate.server.Database;
import jhi.germinate.server.util.StringUtils;

import static jhi.germinate.server.database.tables.Mapdefinitions.*;
import static jhi.germinate.server.database.tables.Maps.*;
import static jhi.germinate.server.database.tables.Markers.*;

/**
 * @author Sebastian Raubach
 */
public class MarkerPositionServerResource extends BaseServerResource<ArrayResult<MarkerPositionResult>>
{
	private static final String PARAM_MAP_DB_ID          = "mapDbId";
	private static final String PARAM_LINKAGE_GROUP_NAME = "linkageGroupName";
	private static final String PARAM_VARIANT_DB_ID      = "variantDbId";
	private static final String PARAM_MAX_POSITION       = "maxPosition";
	private static final String PARAM_MIN_POSITION       = "minPosition";

	private String mapDbId;
	private String linkageGroupName;
	private String variantDbId;
	private Double maxPosition;
	private Double minPosition;

	@Override
	public void doInit()
	{
		super.doInit();

		this.mapDbId = getQueryValue(PARAM_MAP_DB_ID);
		this.linkageGroupName = getQueryValue(PARAM_LINKAGE_GROUP_NAME);
		this.variantDbId = getQueryValue(PARAM_VARIANT_DB_ID);

		try
		{
			this.maxPosition = Double.parseDouble(getQueryValue(PARAM_MAX_POSITION));
		}
		catch (Exception e)
		{
		}

		try
		{
			this.minPosition = Double.parseDouble(getQueryValue(PARAM_MIN_POSITION));
		}
		catch (Exception e)
		{
		}
	}

	@Override
	public BaseResult<ArrayResult<MarkerPositionResult>> getJson()
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

			if (!StringUtils.isEmpty(mapDbId))
				step.where(MAPS.ID.cast(String.class).eq(mapDbId));
			if (!StringUtils.isEmpty(linkageGroupName))
				step.where(MAPDEFINITIONS.CHROMOSOME.eq(linkageGroupName));
			if (!StringUtils.isEmpty(variantDbId))
				step.where(MARKERS.ID.cast(String.class).eq(variantDbId));
			if (maxPosition != null)
				step.where(MAPDEFINITIONS.DEFINITION_START.le(maxPosition));
			if (minPosition != null)
				step.where(MAPDEFINITIONS.DEFINITION_START.ge(minPosition));

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

package jhi.germinate.brapi.server.resource.genotyping.marker;

import org.jooq.*;
import org.restlet.data.Status;
import org.restlet.resource.ResourceException;

import java.sql.*;
import java.util.*;

import jhi.germinate.brapi.resource.base.ArrayResult;
import jhi.germinate.brapi.resource.base.BaseResult;
import jhi.germinate.brapi.resource.map.MarkerPosition;
import jhi.germinate.server.Database;
import jhi.germinate.server.util.StringUtils;

import static jhi.germinate.server.database.tables.Mapdefinitions.*;
import static jhi.germinate.server.database.tables.Maps.*;
import static jhi.germinate.server.database.tables.Markers.*;

/**
 * @author Sebastian Raubach
 */
public class MarkerPositionServerResource extends MarkerBaseServerResource<ArrayResult<MarkerPosition>>
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
	public BaseResult<ArrayResult<MarkerPosition>> getJson()
	{
		try (Connection conn = Database.getConnection();
			 DSLContext context = Database.getContext(conn))
		{
			List<Condition> conditions = new ArrayList<>();

			if (!StringUtils.isEmpty(mapDbId))
				conditions.add(MAPS.ID.cast(String.class).eq(mapDbId));
			if (!StringUtils.isEmpty(linkageGroupName))
				conditions.add(MAPDEFINITIONS.CHROMOSOME.eq(linkageGroupName));
			if (!StringUtils.isEmpty(variantDbId))
				conditions.add(MARKERS.ID.cast(String.class).eq(variantDbId));
			if (maxPosition != null)
				conditions.add(MAPDEFINITIONS.DEFINITION_START.le(maxPosition));
			if (minPosition != null)
				conditions.add(MAPDEFINITIONS.DEFINITION_START.ge(minPosition));

			List<MarkerPosition> result = getMarkerPositions(context, conditions);

			long totalCount = context.fetchOne("SELECT FOUND_ROWS()").into(Long.class);
			return new BaseResult<>(new ArrayResult<MarkerPosition>()
				.setData(result), currentPage, pageSize, totalCount);
		}
		catch (SQLException e)
		{
			e.printStackTrace();
			throw new ResourceException(Status.SERVER_ERROR_INTERNAL);
		}
	}
}

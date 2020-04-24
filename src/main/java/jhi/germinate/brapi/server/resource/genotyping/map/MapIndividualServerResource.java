package jhi.germinate.brapi.server.resource.genotyping.map;

import org.jooq.*;
import org.jooq.impl.DSL;
import org.restlet.data.Status;
import org.restlet.resource.ResourceException;

import java.sql.*;
import java.util.List;

import jhi.germinate.brapi.resource.base.BaseResult;
import jhi.germinate.brapi.resource.map.Map;
import jhi.germinate.brapi.server.resource.BaseServerResource;
import jhi.germinate.server.Database;
import jhi.germinate.server.util.CollectionUtils;

import static jhi.germinate.server.database.tables.Mapdefinitions.*;
import static jhi.germinate.server.database.tables.Maps.*;

/**
 * @author Sebastian Raubach
 */
public class MapIndividualServerResource extends BaseServerResource<Map>
{
	private String mapDbId;

	@Override
	public void doInit()
	{
		super.doInit();

		this.mapDbId = getRequestAttributes().get("mapDbId").toString();
	}

	@Override
	public BaseResult<Map> getJson()
	{
		try (Connection conn = Database.getConnection();
			 DSLContext context = Database.getContext(conn))
		{
			SelectJoinStep<?> step = context.select(
				MAPS.ID.as("mapDbId"),
				MAPS.NAME.as("mapName"),
				DSL.countDistinct(MAPDEFINITIONS.CHROMOSOME).as("linkageGroupCount"),
				DSL.count(MAPDEFINITIONS.MARKER_ID).as("markerCount"),
				DSL.val("Genetic").as("type"),
				MAPS.CREATED_ON.as("publishedDate")
			)
											.hint("SQL_CALC_FOUND_ROWS")
											.from(MAPS)
											.leftJoin(MAPDEFINITIONS).on(MAPDEFINITIONS.MAP_ID.eq(MAPS.ID));

			step.where(MAPS.VISIBILITY.eq(true))
				.and(MAPS.ID.cast(String.class).eq(mapDbId));

			List<Map> maps = step.groupBy(MAPS.ID)
								 .limit(pageSize)
								 .offset(pageSize * currentPage)
								 .fetchInto(Map.class);

			Map map = CollectionUtils.isEmpty(maps) ? null : maps.get(0);
			return new BaseResult<>(map, currentPage, pageSize, 1);
		}
		catch (SQLException e)
		{
			e.printStackTrace();
			throw new ResourceException(Status.SERVER_ERROR_INTERNAL);
		}
	}
}

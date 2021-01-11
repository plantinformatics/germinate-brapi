package jhi.germinate.brapi.server.resource.genotyping.map;

import org.jooq.*;
import org.jooq.impl.DSL;
import org.restlet.data.Status;
import org.restlet.resource.*;

import java.sql.*;
import java.util.List;

import jhi.germinate.server.Database;
import uk.ac.hutton.ics.brapi.resource.base.*;
import uk.ac.hutton.ics.brapi.server.base.BaseServerResource;
import uk.ac.hutton.ics.brapi.server.genotyping.map.BrapiMapLinkageGroupServerResource;

import static jhi.germinate.server.database.codegen.tables.Mapdefinitions.*;
import static jhi.germinate.server.database.codegen.tables.Maps.*;

/**
 * @author Sebastian Raubach
 */
public class MapLinkageGroupServerResource extends BaseServerResource implements BrapiMapLinkageGroupServerResource
{
	private Integer mapDbId;

	@Override
	public void doInit()
	{
		super.doInit();

		try
		{
			this.mapDbId = Integer.parseInt(getRequestAttributes().get("mapDbId").toString());
		}
		catch (NullPointerException | NumberFormatException e)
		{
			e.printStackTrace();
		}
	}

	@Get
	public BaseResult<ArrayResult<LinkageGroup>> getMapByIdLinkageGroups()
	{
		try (DSLContext context = Database.getContext())
		{
			SelectJoinStep<?> step = context.select(
				MAPDEFINITIONS.CHROMOSOME.as("linkageGroupName"),
				DSL.count(MAPDEFINITIONS.MARKER_ID).as("markerCount"),
				DSL.max(MAPDEFINITIONS.DEFINITION_START)
			)
											.hint("SQL_CALC_FOUND_ROWS")
											.from(MAPS)
											.leftJoin(MAPDEFINITIONS).on(MAPDEFINITIONS.MAP_ID.eq(MAPS.ID));

			step.where(MAPS.VISIBILITY.eq(true))
				.and(MAPS.ID.eq(mapDbId));

			List<LinkageGroup> result = step.groupBy(MAPDEFINITIONS.CHROMOSOME)
											.limit(pageSize)
											.offset(pageSize * currentPage)
											.fetchInto(LinkageGroup.class);

			long totalCount = context.fetchOne("SELECT FOUND_ROWS()").into(Long.class);
			return new BaseResult<>(new ArrayResult<LinkageGroup>().setData(result), currentPage, pageSize, totalCount);
		}
	}
}

package jhi.germinate.brapi.server.resource.genotyping.map;

import org.jooq.*;
import org.jooq.impl.DSL;
import org.restlet.data.Status;
import org.restlet.resource.ResourceException;

import java.sql.*;
import java.util.List;

import jhi.germinate.brapi.resource.*;
import jhi.germinate.brapi.resource.base.BaseResult;
import jhi.germinate.brapi.server.resource.BaseServerResource;
import jhi.germinate.server.Database;

import static jhi.germinate.server.database.tables.Mapdefinitions.*;
import static jhi.germinate.server.database.tables.Maps.*;

/**
 * @author Sebastian Raubach
 */
public class MapLinkageGroupServerResource extends BaseServerResource<ArrayResult<LinkageGroupResult>>
{
	private String mapDbId;

	@Override
	public void doInit()
	{
		super.doInit();

		this.mapDbId = getRequestAttributes().get("mapDbId").toString();
	}

	@Override
	public BaseResult<ArrayResult<LinkageGroupResult>> getJson()
	{
		try (Connection conn = Database.getConnection();
			 DSLContext context = Database.getContext(conn))
		{
			SelectJoinStep<?> step = context.select(
				MAPDEFINITIONS.CHROMOSOME.as("linkageGroupName"),
				DSL.count(MAPDEFINITIONS.MARKER_ID).as("markerCount"),
				DSL.max(MAPDEFINITIONS.DEFINITION_START)
			)
											.hint("SQL_CALC_FOUND_ROWS")
											.from(MAPS)
											.leftJoin(MAPDEFINITIONS).on(MAPDEFINITIONS.MAP_ID.eq(MAPS.ID));

			step.where(MAPS.VISIBILITY.eq(true));

			List<LinkageGroupResult> result = step.groupBy(MAPDEFINITIONS.CHROMOSOME)
												  .fetchInto(LinkageGroupResult.class);

			long totalCount = context.fetchOne("SELECT FOUND_ROWS()").into(Long.class);
			return new BaseResult<>(new ArrayResult<LinkageGroupResult>().setData(result), currentPage, pageSize, totalCount);
		}
		catch (SQLException e)
		{
			e.printStackTrace();
			throw new ResourceException(Status.SERVER_ERROR_INTERNAL);
		}
	}
}

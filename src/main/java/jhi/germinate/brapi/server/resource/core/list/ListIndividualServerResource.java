package jhi.germinate.brapi.server.resource.core.list;

import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.jooq.tools.StringUtils;
import org.restlet.data.Status;
import org.restlet.resource.*;

import java.sql.*;
import java.util.*;

import jhi.germinate.brapi.resource.list.ListResult;
import jhi.germinate.brapi.resource.base.BaseResult;
import jhi.germinate.server.Database;
import jhi.germinate.server.auth.*;
import jhi.germinate.server.database.tables.pojos.ViewTableGroups;
import jhi.germinate.server.util.CollectionUtils;

import static jhi.germinate.server.database.tables.Germinatebase.*;
import static jhi.germinate.server.database.tables.Groupmembers.*;
import static jhi.germinate.server.database.tables.Locations.*;
import static jhi.germinate.server.database.tables.Markers.*;
import static jhi.germinate.server.database.tables.ViewTableGroups.*;

/**
 * @author Sebastian Raubach
 */
public class ListIndividualServerResource extends ListBaseServerResource<ListResult>
{
	protected String listDbId;

	@Override
	public void doInit()
	{
		super.doInit();

		try
		{
			this.listDbId = getRequestAttributes().get("listDbId").toString();
		}
		catch (Exception e)
		{
		}
	}

	@Put
	@MinUserType(UserType.AUTH_USER)
	public BaseResult<ListResult> putJson(ListResult updatedList)
	{
		// TODO: Check if they're authorized to do this!

		try (Connection conn = Database.getConnection();
			 DSLContext context = Database.getContext(conn))
		{
			ViewTableGroups result = context.selectFrom(VIEW_TABLE_GROUPS)
											.where(VIEW_TABLE_GROUPS.GROUP_VISIBILITY.eq(true))
											.and(VIEW_TABLE_GROUPS.GROUP_ID.cast(String.class).eq(listDbId))
											.fetchAnyInto(ViewTableGroups.class);

			if (result != null)
			{
				// Remove all group members
				context.deleteFrom(GROUPMEMBERS)
					   .where(GROUPMEMBERS.GROUP_ID.eq(result.getGroupId()))
					   .execute();

				// Then set the new ones
				switch (result.getGroupTypeId())
				{
					case 1:
						context.insertInto(GROUPMEMBERS, GROUPMEMBERS.FOREIGN_ID, GROUPMEMBERS.GROUP_ID)
							   .select(DSL.select(LOCATIONS.ID, DSL.val(result.getGroupId()))
										  .from(LOCATIONS)
										  .where(LOCATIONS.ID.in(updatedList.getData())))
							   .execute();
						break;
					case 2:
						context.insertInto(GROUPMEMBERS, GROUPMEMBERS.FOREIGN_ID, GROUPMEMBERS.GROUP_ID)
							   .select(DSL.select(MARKERS.ID, DSL.val(result.getGroupId()))
										  .from(MARKERS)
										  .where(MARKERS.ID.in(updatedList.getData())))
							   .execute();
						break;
					case 3:
						context.insertInto(GROUPMEMBERS, GROUPMEMBERS.FOREIGN_ID, GROUPMEMBERS.GROUP_ID)
							   .select(DSL.select(GERMINATEBASE.ID, DSL.val(result.getGroupId()))
										  .from(GERMINATEBASE)
										  .where(GERMINATEBASE.ID.in(updatedList.getData())))
							   .execute();
						break;
				}

				this.listDbId = Integer.toString(result.getGroupId());

				return getJson();
			}
			else
			{
				throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND);
			}
		}
		catch (SQLException e)
		{
			e.printStackTrace();
			throw new ResourceException(Status.SERVER_ERROR_INTERNAL);
		}
	}

	@Override
	public BaseResult<ListResult> getJson()
	{
		if (StringUtils.isEmpty(listDbId))
			throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST);

		return getList(listDbId, pageSize, currentPage);
	}

	protected BaseResult<ListResult> getList(String listDbId, int pageSize, int currentPage) {
		try (Connection conn = Database.getConnection();
			 DSLContext context = Database.getContext(conn))
		{
			List<ListResult> results = getLists(context, Collections.singletonList(VIEW_TABLE_GROUPS.GROUP_ID.cast(String.class).eq(listDbId)));
			ListResult result = CollectionUtils.isEmpty(results) ? null : results.get(0);

			if (result != null)
			{
				switch (result.getListType())
				{
					case "germinatebase":
						result.setData(context.select(GERMINATEBASE.NAME)
											  .hint("SQL_CALC_FOUND_ROWS")
											  .from(GERMINATEBASE)
											  .leftJoin(GROUPMEMBERS).on(GROUPMEMBERS.FOREIGN_ID.eq(GERMINATEBASE.ID))
											  .where(GROUPMEMBERS.GROUP_ID.cast(String.class).eq(listDbId))
											  .limit(pageSize)
											  .offset(pageSize * currentPage)
											  .fetchInto(String.class));
						break;
					case "locations":
						result.setData(context.select(LOCATIONS.SITE_NAME)
											  .hint("SQL_CALC_FOUND_ROWS")
											  .from(LOCATIONS)
											  .leftJoin(GROUPMEMBERS).on(GROUPMEMBERS.FOREIGN_ID.eq(LOCATIONS.ID))
											  .where(GROUPMEMBERS.GROUP_ID.cast(String.class).eq(listDbId))
											  .limit(pageSize)
											  .offset(pageSize * currentPage)
											  .fetchInto(String.class));
						break;
					case "markers":
						result.setData(context.select(MARKERS.MARKER_NAME)
											  .hint("SQL_CALC_FOUND_ROWS")
											  .from(MARKERS)
											  .leftJoin(GROUPMEMBERS).on(GROUPMEMBERS.FOREIGN_ID.eq(MARKERS.ID))
											  .where(GROUPMEMBERS.GROUP_ID.cast(String.class).eq(listDbId))
											  .limit(pageSize)
											  .offset(pageSize * currentPage)
											  .fetchInto(String.class));
						break;
				}

				long totalCount = context.fetchOne("SELECT FOUND_ROWS()").into(Long.class);
				return new BaseResult<>(result, currentPage, pageSize, totalCount);
			}
			else
			{
				return new BaseResult<>(null, currentPage, pageSize, 0);
			}
		}
		catch (SQLException e)
		{
			e.printStackTrace();
			throw new ResourceException(Status.SERVER_ERROR_INTERNAL);
		}
	}
}

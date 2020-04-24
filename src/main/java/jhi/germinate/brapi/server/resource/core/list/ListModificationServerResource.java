package jhi.germinate.brapi.server.resource.core.list;

import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.restlet.data.Status;
import org.restlet.resource.*;

import java.sql.*;
import java.util.*;

import jhi.germinate.brapi.resource.base.BaseResult;
import jhi.germinate.brapi.resource.list.Lists;
import jhi.germinate.server.Database;
import jhi.germinate.server.auth.*;
import jhi.germinate.server.database.tables.pojos.ViewTableGroups;

import static jhi.germinate.server.database.tables.Germinatebase.*;
import static jhi.germinate.server.database.tables.Groupmembers.*;
import static jhi.germinate.server.database.tables.Locations.*;
import static jhi.germinate.server.database.tables.Markers.*;
import static jhi.germinate.server.database.tables.ViewTableGroups.*;

/**
 * @author Sebastian Raubach
 */
public class ListModificationServerResource extends ListIndividualServerResource
{
	@Override
	public BaseResult<Lists> getJson()
	{
		throw new ResourceException(Status.SERVER_ERROR_NOT_IMPLEMENTED);
	}

	@Override
	public BaseResult<Lists> putJson(Lists updatedLists)
	{
		throw new ResourceException(Status.SERVER_ERROR_NOT_IMPLEMENTED);
	}

	@Post
	@MinUserType(UserType.AUTH_USER)
	public BaseResult<Lists> postJson(String[] ids)
	{
		if (ids == null)
			throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST);

		// TODO: Check if they're authorized to do this

		List<String> stringIds = Arrays.asList(ids);
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
										  .where(LOCATIONS.ID.in(stringIds)))
							   .execute();
						break;
					case 2:
						context.insertInto(GROUPMEMBERS, GROUPMEMBERS.FOREIGN_ID, GROUPMEMBERS.GROUP_ID)
							   .select(DSL.select(MARKERS.ID, DSL.val(result.getGroupId()))
										  .from(MARKERS)
										  .where(MARKERS.ID.in(stringIds)))
							   .execute();
						break;
					case 3:
						context.insertInto(GROUPMEMBERS, GROUPMEMBERS.FOREIGN_ID, GROUPMEMBERS.GROUP_ID)
							   .select(DSL.select(GERMINATEBASE.ID, DSL.val(result.getGroupId()))
										  .from(GERMINATEBASE)
										  .where(GERMINATEBASE.ID.in(stringIds)))
							   .execute();
						break;
				}

				return getList(Integer.toString(result.getGroupId()), pageSize, currentPage);
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
}

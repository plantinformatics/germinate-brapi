package jhi.germinate.brapi.server.resource.core.list;

import org.jooq.*;
import org.jooq.impl.DSL;
import org.jooq.tools.StringUtils;
import org.restlet.data.Status;
import org.restlet.resource.*;

import java.sql.*;
import java.util.*;

import jhi.germinate.server.Database;
import jhi.germinate.server.auth.*;
import jhi.germinate.server.database.tables.records.*;
import uk.ac.hutton.ics.brapi.resource.base.*;
import uk.ac.hutton.ics.brapi.resource.core.list.Lists;
import uk.ac.hutton.ics.brapi.server.core.list.BrapiListServerResource;

import static jhi.germinate.server.database.tables.Germinatebase.*;
import static jhi.germinate.server.database.tables.Groupmembers.*;
import static jhi.germinate.server.database.tables.Groups.*;
import static jhi.germinate.server.database.tables.Grouptypes.*;
import static jhi.germinate.server.database.tables.Locations.*;
import static jhi.germinate.server.database.tables.Markers.*;
import static jhi.germinate.server.database.tables.ViewTableGroups.*;

/**
 * @author Sebastian Raubach
 */
public class ListServerResource extends ListBaseServerResource implements BrapiListServerResource
{
	public static final String PARAM_LIST_TYPE   = "listType";
	public static final String PARAM_LIST_NAME   = "listName";
	public static final String PARAM_LIST_DB_ID  = "listDbId";
	public static final String PARAM_LIST_SOURCE = "listSource";

	private String listType;
	private String listName;
	private String listDbId;
	private String listSource;

	@Override
	public void doInit()
	{
		super.doInit();

		this.listType = getQueryValue(PARAM_LIST_TYPE);
		this.listName = getQueryValue(PARAM_LIST_NAME);
		this.listDbId = getQueryValue(PARAM_LIST_DB_ID);
		this.listSource = getQueryValue(PARAM_LIST_SOURCE);
	}

	@Post
	@MinUserType(UserType.AUTH_USER)
	public BaseResult<ArrayResult<Lists>> postLists(Lists[] newLists)
	{
		// TODO: Check if they're authorized to do this

		try (Connection conn = Database.getConnection();
			 DSLContext context = Database.getContext(conn))
		{
			List<Integer> groupIds = new ArrayList<>();
			for (Lists lists : newLists)
			{
				GrouptypesRecord groupType = context.selectFrom(GROUPTYPES)
													.where(GROUPTYPES.TARGET_TABLE.eq(lists.getListType()))
													.fetchAny();

				if (groupType == null)
					continue;

				// Create the group
				GroupsRecord group = context.newRecord(GROUPS);
				group.setName(lists.getListName());
				group.setDescription(lists.getListDescription());
				group.setVisibility(true);
				group.setGrouptypeId(groupType.getId());
				group.setCreatedOn(lists.getDateCreated());
				group.setUpdatedOn(lists.getDateModified());
				try
				{
					group.setCreatedBy(Integer.parseInt(lists.getListOwnerPersonDbId()));
				}
				catch (Exception e)
				{
				}
				group.store();
				groupIds.add(group.getId());

				// Insert group members
				switch (groupType.getId())
				{
					case 1:
						context.insertInto(GROUPMEMBERS, GROUPMEMBERS.FOREIGN_ID, GROUPMEMBERS.GROUP_ID)
							   .select(DSL.select(LOCATIONS.ID, DSL.val(group.getId()))
										  .from(LOCATIONS)
										  .where(LOCATIONS.ID.in(lists.getData())))
							   .execute();
						break;
					case 2:
						context.insertInto(GROUPMEMBERS, GROUPMEMBERS.FOREIGN_ID, GROUPMEMBERS.GROUP_ID)
							   .select(DSL.select(MARKERS.ID, DSL.val(group.getId()))
										  .from(MARKERS)
										  .where(MARKERS.ID.in(lists.getData())))
							   .execute();
						break;
					case 3:
						context.insertInto(GROUPMEMBERS, GROUPMEMBERS.FOREIGN_ID, GROUPMEMBERS.GROUP_ID)
							   .select(DSL.select(GERMINATEBASE.ID, DSL.val(group.getId()))
										  .from(GERMINATEBASE)
										  .where(GERMINATEBASE.ID.in(lists.getData())))
							   .execute();
						break;
				}
			}

			List<Lists> lists = getLists(context, Collections.singletonList(VIEW_TABLE_GROUPS.GROUP_ID.in(groupIds)));

			long totalCount = context.fetchOne("SELECT FOUND_ROWS()").into(Long.class);
			return new BaseResult<>(new ArrayResult<Lists>()
				.setData(lists), currentPage, pageSize, totalCount);
		}
		catch (SQLException e)
		{
			e.printStackTrace();
			throw new ResourceException(Status.SERVER_ERROR_INTERNAL);
		}
	}

	@Get
	public BaseResult<ArrayResult<Lists>> getLists()
	{
		try (Connection conn = Database.getConnection();
			 DSLContext context = Database.getContext(conn))
		{
			List<Condition> conditions = new ArrayList<>();

			if (!StringUtils.isEmpty(listType))
				conditions.add(VIEW_TABLE_GROUPS.GROUP_TYPE.eq(listType));
			if (!StringUtils.isEmpty(listName))
				conditions.add(VIEW_TABLE_GROUPS.GROUP_NAME.eq(listName));
			if (!StringUtils.isEmpty(listDbId))
				conditions.add(VIEW_TABLE_GROUPS.GROUP_ID.cast(String.class).eq(listDbId));

			List<Lists> result = getLists(context, conditions);

			long totalCount = context.fetchOne("SELECT FOUND_ROWS()").into(Long.class);
			return new BaseResult<>(new ArrayResult<Lists>()
				.setData(result), currentPage, pageSize, totalCount);
		}
		catch (SQLException e)
		{
			e.printStackTrace();
			throw new ResourceException(Status.SERVER_ERROR_INTERNAL);
		}
	}
}

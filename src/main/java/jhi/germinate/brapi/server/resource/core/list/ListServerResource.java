package jhi.germinate.brapi.server.resource.core.list;

import org.jooq.*;
import org.jooq.impl.DSL;
import org.jooq.tools.StringUtils;
import org.restlet.data.Status;
import org.restlet.resource.*;

import java.sql.*;
import java.util.*;
import java.util.stream.Collectors;

import jhi.germinate.brapi.resource.*;
import jhi.germinate.brapi.resource.base.BaseResult;
import jhi.germinate.brapi.server.resource.BaseServerResource;
import jhi.germinate.server.Database;
import jhi.germinate.server.database.tables.pojos.ViewTableGroups;
import jhi.germinate.server.database.tables.records.*;

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
public class ListServerResource extends BaseServerResource<ArrayResult<ListResult>>
{
	public static final String PARAM_LIST_TYPE   = "listType";
	public static final String PARAM_LIST_NAME   = "listName";
	public static final String PARAM_LIST_DB_ID  = "listDbId";
	public static final String PARAM_LIST_SOURCE = "listSource";

	private String listType;
	private String listName;
	private String listDbId;
	private String listSource;

	public static List<ListResult> mapTo(List<ViewTableGroups> input)
	{
		return input.stream()
					.map(l -> new ListResult()
						.setDateCreated(l.getCreatedOn())
						.setDateModified(l.getUpdatedOn())
						.setListDbId(toString(l.getGroupId()))
						.setListDescription(l.getGroupDescription())
						.setListName(l.getGroupName())
						.setListOwnerName(toString(l.getUserName()))
						.setListOwnerPersonDbId(toString(l.getUserId()))
						.setListSize(l.getCount())
						.setListType(l.getGroupType()))
					.collect(Collectors.toList());
	}

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
	public BaseResult<ArrayResult<ListResult>> postJson(ListResult[] newLists)
	{
		try (Connection conn = Database.getConnection();
			 DSLContext context = Database.getContext(conn))
		{
			List<Integer> groupIds = new ArrayList<>();
			for (ListResult list : newLists)
			{
				GrouptypesRecord groupType = context.selectFrom(GROUPTYPES)
													.where(GROUPTYPES.TARGET_TABLE.eq(list.getListType()))
													.fetchAny();

				if (groupType == null)
					continue;

				// Create the group
				GroupsRecord group = context.newRecord(GROUPS);
				group.setName(list.getListName());
				group.setDescription(list.getListDescription());
				group.setVisibility(true);
				group.setGrouptypeId(groupType.getId());
				group.setCreatedOn(list.getDateCreated());
				group.setUpdatedOn(list.getDateModified());
				try
				{
					group.setCreatedBy(Integer.parseInt(list.getListOwnerPersonDbId()));
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
										  .where(LOCATIONS.ID.in(list.getData())))
							   .execute();
						break;
					case 2:
						context.insertInto(GROUPMEMBERS, GROUPMEMBERS.FOREIGN_ID, GROUPMEMBERS.GROUP_ID)
							   .select(DSL.select(MARKERS.ID, DSL.val(group.getId()))
										  .from(MARKERS)
										  .where(MARKERS.ID.in(list.getData())))
							   .execute();
						break;
					case 3:
						context.insertInto(GROUPMEMBERS, GROUPMEMBERS.FOREIGN_ID, GROUPMEMBERS.GROUP_ID)
							   .select(DSL.select(GERMINATEBASE.ID, DSL.val(group.getId()))
										  .from(GERMINATEBASE)
										  .where(GERMINATEBASE.ID.in(list.getData())))
							   .execute();
						break;
				}
			}

			// Then get the newly created groups as a result
			List<ViewTableGroups> locations = context.select()
													 .hint("SQL_CALC_FOUND_ROWS")
													 .from(VIEW_TABLE_GROUPS)
													 .where(VIEW_TABLE_GROUPS.GROUP_ID.in(groupIds))
													 .fetchInto(ViewTableGroups.class);

			List<ListResult> lists = mapTo(locations);

			long totalCount = context.fetchOne("SELECT FOUND_ROWS()").into(Long.class);
			return new BaseResult<>(new ArrayResult<ListResult>()
				.setData(lists), currentPage, pageSize, totalCount);
		}
		catch (SQLException e)
		{
			e.printStackTrace();
			throw new ResourceException(Status.SERVER_ERROR_INTERNAL);
		}
	}

	@Override
	public BaseResult<ArrayResult<ListResult>> getJson()
	{
		try (Connection conn = Database.getConnection();
			 DSLContext context = Database.getContext(conn))
		{
			SelectConditionStep<Record> step = context.select()
													  .hint("SQL_CALC_FOUND_ROWS")
													  .from(VIEW_TABLE_GROUPS)
													  .where(VIEW_TABLE_GROUPS.GROUP_VISIBILITY.eq(true));

			if (!StringUtils.isEmpty(listType))
				step.and(VIEW_TABLE_GROUPS.GROUP_TYPE.eq(listType));
			if (!StringUtils.isEmpty(listName))
				step.and(VIEW_TABLE_GROUPS.GROUP_NAME.eq(listName));
			if (!StringUtils.isEmpty(listDbId))
				step.and(VIEW_TABLE_GROUPS.GROUP_ID.cast(String.class).eq(listDbId));

			List<ViewTableGroups> locations = step.limit(pageSize)
												  .offset(pageSize * currentPage)
												  .fetchInto(ViewTableGroups.class);

			List<ListResult> lists = mapTo(locations);

			long totalCount = context.fetchOne("SELECT FOUND_ROWS()").into(Long.class);
			return new BaseResult<>(new ArrayResult<ListResult>()
				.setData(lists), currentPage, pageSize, totalCount);
		}
		catch (SQLException e)
		{
			e.printStackTrace();
			throw new ResourceException(Status.SERVER_ERROR_INTERNAL);
		}
	}
}

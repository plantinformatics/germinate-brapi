package jhi.germinate.brapi.server.resource.core.list;

import org.jooq.*;
import org.jooq.tools.StringUtils;
import org.restlet.data.Status;
import org.restlet.resource.*;

import java.sql.*;
import java.util.List;

import jhi.germinate.brapi.resource.*;
import jhi.germinate.brapi.resource.base.BaseResult;
import jhi.germinate.brapi.server.resource.BaseServerResource;
import jhi.germinate.server.Database;
import jhi.germinate.server.database.tables.pojos.ViewTableGroups;

import static jhi.germinate.server.database.tables.ViewTableGroups.*;

/**
 * @author Sebastian Raubach
 */
public class SearchListServerResource extends BaseServerResource<ArrayResult<ListResult>>
{
	@Override
	public BaseResult<ArrayResult<ListResult>> getJson()
	{
		throw new ResourceException(Status.SERVER_ERROR_NOT_IMPLEMENTED);
	}

	@Post
	public BaseResult<ArrayResult<ListResult>> postJson(ListSearch search)
	{
		try (Connection conn = Database.getConnection();
			 DSLContext context = Database.getContext(conn))
		{
			SelectConditionStep<Record> step = context.select()
													  .hint("SQL_CALC_FOUND_ROWS")
													  .from(VIEW_TABLE_GROUPS)
													  .where(VIEW_TABLE_GROUPS.GROUP_VISIBILITY.eq(true));

			if (search.getDateCreatedRangeEnd() != null)
				step.and(VIEW_TABLE_GROUPS.CREATED_ON.le(search.getDateCreatedRangeEnd()));
			if (search.getDateCreatedRangeStart() != null)
				step.and(VIEW_TABLE_GROUPS.CREATED_ON.ge(search.getDateCreatedRangeStart()));

			if (search.getDateModifiedRangeEnd() != null)
				step.and(VIEW_TABLE_GROUPS.UPDATED_ON.le(search.getDateModifiedRangeEnd()));
			if (search.getDateModifiedRangeStart() != null)
				step.and(VIEW_TABLE_GROUPS.UPDATED_ON.ge(search.getDateModifiedRangeStart()));

			if (search.getListDbIds() != null)
				step.and(VIEW_TABLE_GROUPS.GROUP_ID.in(search.getListDbIds()));

			if (search.getListNames() != null)
				step.and(VIEW_TABLE_GROUPS.GROUP_NAME.in(search.getListNames()));

			if (search.getListOwnerNames() != null)
				step.and(VIEW_TABLE_GROUPS.USER_NAME.in(search.getListOwnerNames()));
			if (search.getListOwnerPersonDbIds() != null)
				step.and(VIEW_TABLE_GROUPS.USER_ID.in(search.getListOwnerPersonDbIds()));

			if (!StringUtils.isEmpty(search.getListType()))
				step.and(VIEW_TABLE_GROUPS.GROUP_TYPE.eq(search.getListType()));

			List<ViewTableGroups> locations = step.limit(pageSize)
												  .offset(pageSize * currentPage)
												  .fetchInto(ViewTableGroups.class);

			List<ListResult> lists = ListServerResource.mapTo(locations);

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

package jhi.germinate.brapi.server.resource.core.list;

import org.jooq.*;
import org.jooq.tools.StringUtils;
import org.restlet.data.Status;
import org.restlet.resource.*;

import java.sql.*;
import java.util.*;

import jhi.germinate.server.Database;
import uk.ac.hutton.ics.brapi.resource.base.*;
import uk.ac.hutton.ics.brapi.resource.core.list.*;
import uk.ac.hutton.ics.brapi.server.core.list.BrapiSearchListServerResource;

import static jhi.germinate.server.database.codegen.tables.ViewTableGroups.*;

/**
 * @author Sebastian Raubach
 */
public class SearchListServerResource extends ListBaseServerResource implements BrapiSearchListServerResource
{
	@Post
	public BaseResult<ArrayResult<Lists>> postListSearch(ListSearch search)
	{
		if (search == null)
			throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST);

		try (DSLContext context = Database.getContext())
		{
			List<Condition> conditions = new ArrayList<>();

			if (search.getDateCreatedRangeEnd() != null)
				conditions.add(VIEW_TABLE_GROUPS.CREATED_ON.le(search.getDateCreatedRangeEnd()));
			if (search.getDateCreatedRangeStart() != null)
				conditions.add(VIEW_TABLE_GROUPS.CREATED_ON.ge(search.getDateCreatedRangeStart()));

			if (search.getDateModifiedRangeEnd() != null)
				conditions.add(VIEW_TABLE_GROUPS.UPDATED_ON.le(search.getDateModifiedRangeEnd()));
			if (search.getDateModifiedRangeStart() != null)
				conditions.add(VIEW_TABLE_GROUPS.UPDATED_ON.ge(search.getDateModifiedRangeStart()));

			if (search.getListDbIds() != null)
				conditions.add(VIEW_TABLE_GROUPS.GROUP_ID.in(search.getListDbIds()));

			if (search.getListNames() != null)
				conditions.add(VIEW_TABLE_GROUPS.GROUP_NAME.in(search.getListNames()));

			if (search.getListOwnerNames() != null)
				conditions.add(VIEW_TABLE_GROUPS.USER_NAME.in(search.getListOwnerNames()));
			if (search.getListOwnerPersonDbIds() != null)
				conditions.add(VIEW_TABLE_GROUPS.USER_ID.in(search.getListOwnerPersonDbIds()));

			if (!StringUtils.isEmpty(search.getListType()))
				conditions.add(VIEW_TABLE_GROUPS.GROUP_TYPE.eq(search.getListType()));

			List<Lists> lists = getLists(context, conditions);

			long totalCount = context.fetchOne("SELECT FOUND_ROWS()").into(Long.class);
			return new BaseResult<>(new ArrayResult<Lists>()
				.setData(lists), currentPage, pageSize, totalCount);
		}
	}

	@Post
	public BaseResult<SearchResult> postListSearchAsync(ListSearch listSearch)
	{
		throw new ResourceException(Status.SERVER_ERROR_NOT_IMPLEMENTED);
	}

	@Get
	public BaseResult<ArrayResult<Lists>> getListSearchAsync()
	{
		throw new ResourceException(Status.SERVER_ERROR_NOT_IMPLEMENTED);
	}
}

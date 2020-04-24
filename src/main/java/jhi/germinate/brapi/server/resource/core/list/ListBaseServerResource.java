package jhi.germinate.brapi.server.resource.core.list;

import org.jooq.*;

import java.util.List;
import java.util.stream.Collectors;

import jhi.germinate.brapi.resource.list.Lists;
import jhi.germinate.brapi.server.resource.BaseServerResource;
import jhi.germinate.server.database.tables.pojos.ViewTableGroups;

import static jhi.germinate.server.database.tables.ViewTableGroups.*;

/**
 * @author Sebastian Raubach
 */
public abstract class ListBaseServerResource<T> extends BaseServerResource<T>
{
	protected List<Lists> getLists(DSLContext context, List<Condition> conditions)
	{
		SelectConditionStep<Record> step = context.select()
												  .hint("SQL_CALC_FOUND_ROWS")
												  .from(VIEW_TABLE_GROUPS)
												  .where(VIEW_TABLE_GROUPS.GROUP_VISIBILITY.eq(true));

		if (conditions != null)
		{
			for (Condition condition : conditions)
			{
				step.and(condition);
			}
		}

		List<ViewTableGroups> groups = step.limit(pageSize)
										   .offset(pageSize * currentPage)
										   .fetchInto(ViewTableGroups.class);

		return groups.stream()
					 .map(l -> new Lists()
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
}

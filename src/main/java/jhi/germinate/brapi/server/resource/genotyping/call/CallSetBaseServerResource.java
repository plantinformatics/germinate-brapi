package jhi.germinate.brapi.server.resource.genotyping.call;

import org.jooq.*;
import org.jooq.impl.DSL;

import java.util.*;

import jhi.germinate.brapi.resource.call.CallSet;
import jhi.germinate.brapi.server.resource.BaseServerResource;
import jhi.germinate.server.util.*;

import static jhi.germinate.server.database.tables.Datasetmembers.*;
import static jhi.germinate.server.database.tables.Germinatebase.*;

/**
 * @author Sebastian Raubach
 */
public abstract class CallSetBaseServerResource<T> extends BaseServerResource<T>
{
	protected List<CallSet> getCallSets(DSLContext context, List<Condition> conditions)
	{
		SelectJoinStep<?> step = context.select(
			DSL.concat(DATASETMEMBERS.DATASET_ID, DSL.val("-"), GERMINATEBASE.ID).as("callSetDbId"),
			GERMINATEBASE.NAME.as("callSetName"),
			DATASETMEMBERS.CREATED_ON.as("created"),
			DATASETMEMBERS.UPDATED_ON.as("updated"),
			DATASETMEMBERS.DATASET_ID.as("studyDbId")
		)
										.hint("SQL_CALC_FOUND_ROWS")
										.from(DATASETMEMBERS)
										.leftJoin(GERMINATEBASE).on(DATASETMEMBERS.DATASETMEMBERTYPE_ID.eq(2).and(DATASETMEMBERS.FOREIGN_ID.eq(GERMINATEBASE.ID)));

		if (!CollectionUtils.isEmpty(conditions))
		{
			for (Condition condition : conditions)
				step.where(condition);
		}

		List<CallSet> result = step.limit(pageSize)
								   .offset(pageSize * currentPage)
								   .fetchInto(CallSet.class);

		result.forEach(c -> {
			if (!StringUtils.isEmpty(c.getStudyDbId()))
				c.setVariantSetDbIds(Collections.singletonList(c.getStudyDbId()));
		});

		return result;
	}
}

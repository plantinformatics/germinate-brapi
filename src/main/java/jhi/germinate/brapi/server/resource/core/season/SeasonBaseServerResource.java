package jhi.germinate.brapi.server.resource.core.season;

import org.jooq.*;
import org.jooq.impl.DSL;

import java.util.List;
import java.util.stream.Collectors;

import jhi.germinate.brapi.resource.season.Season;
import jhi.germinate.brapi.server.resource.BaseServerResource;

import static jhi.germinate.server.database.tables.Datasets.*;

/**
 * @author Sebastian Raubach
 */
public abstract class SeasonBaseServerResource<T> extends BaseServerResource<T>
{
	protected List<Season> getSeasons(DSLContext context, List<Condition> conditions)
	{
		SelectConditionStep<?> step = context.selectDistinct(DSL.year(DATASETS.DATE_START))
											 .hint("SQL_CALC_FOUND_ROWS")
											 .from(DATASETS)
											 .where(DATASETS.DATASET_STATE_ID.eq(1))
											 .and(DATASETS.DATE_START.isNotNull());

		if (conditions != null)
		{
			for (Condition condition : conditions)
				step.and(condition);
		}

		List<Integer> years = step.limit(pageSize)
								  .offset(pageSize * currentPage)
								  .fetchInto(Integer.class);

		return years.stream()
					.map(y -> new Season()
						.setYear(y)
						.setSeasonDbId(Integer.toString(y))
						.setSeasonName(Integer.toString(y)))
					.collect(Collectors.toList());
	}
}

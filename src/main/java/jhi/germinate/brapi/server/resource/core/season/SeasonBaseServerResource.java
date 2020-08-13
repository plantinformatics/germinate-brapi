package jhi.germinate.brapi.server.resource.core.season;

import org.jooq.*;
import org.jooq.impl.DSL;

import java.util.List;
import java.util.stream.Collectors;

import uk.ac.hutton.ics.brapi.resource.core.season.Season;
import uk.ac.hutton.ics.brapi.server.base.BaseServerResource;

import static jhi.germinate.server.database.tables.Datasets.*;

/**
 * @author Sebastian Raubach
 */
public abstract class SeasonBaseServerResource extends BaseServerResource
{
	protected List<Season> getSeasons(DSLContext context, List<Condition> conditions)
	{
		SelectConditionStep<?> step = context.selectDistinct(DSL.year(DATASETS.DATE_START))
											 .hint("SQL_CALC_FOUND_ROWS")
											 .from(DATASETS)
											 .where(DATASETS.DATASET_STATE_ID.eq(1))
											 .and(DATASETS.IS_EXTERNAL.eq(false))
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

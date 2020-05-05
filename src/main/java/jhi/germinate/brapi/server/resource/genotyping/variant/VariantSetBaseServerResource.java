package jhi.germinate.brapi.server.resource.genotyping.variant;

import org.jooq.*;
import org.jooq.impl.DSL;

import java.util.List;

import jhi.germinate.brapi.resource.variant.VariantSet;
import jhi.germinate.brapi.server.resource.BaseServerResource;

import static jhi.germinate.server.database.tables.Datasetmembers.*;
import static jhi.germinate.server.database.tables.Datasets.*;

/**
 * @author Sebastian Raubach
 */
public abstract class VariantSetBaseServerResource<T> extends BaseServerResource<T>
{
	protected List<VariantSet> getVariantSets(DSLContext context, List<Condition> conditions)
	{
		SelectConditionStep<?> step = context.select(
			DATASETS.ID.as("studyDbId"),
			DATASETS.ID.as("variantSetDbId"),
			DATASETS.NAME.as("variantSetName"),
			DSL.selectCount().from(DATASETMEMBERS).where(DATASETMEMBERS.DATASET_ID.eq(DATASETS.ID).and(DATASETMEMBERS.DATASETMEMBERTYPE_ID.eq(2))).asField("callSetCount"),
			DSL.selectCount().from(DATASETMEMBERS).where(DATASETMEMBERS.DATASET_ID.eq(DATASETS.ID).and(DATASETMEMBERS.DATASETMEMBERTYPE_ID.eq(1))).asField("variantCount")
		)
											 .hint("SQL_CALC_FOUND_ROWS")
											 .from(DATASETS)
											 .where(DATASETS.DATASETTYPE_ID.eq(1))
											 .and(DATASETS.DATASET_STATE_ID.eq(1));

		// TODO: Set availableFormats. Make links point to endpoints that dynamically create them.

		if (conditions != null)
		{
			for (Condition condition : conditions)
				step.and(condition);
		}

		return step.limit(pageSize)
				   .offset(pageSize * currentPage)
				   .fetchInto(VariantSet.class);
	}
}

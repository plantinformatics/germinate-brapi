package jhi.germinate.brapi.server.resource.genotyping.variant;

import org.jooq.*;
import org.jooq.impl.DSL;
import org.restlet.ext.servlet.ServletUtils;

import java.net.URI;
import java.util.*;

import jhi.germinate.brapi.server.Brapi;
import uk.ac.hutton.ics.brapi.resource.genotyping.variant.*;
import uk.ac.hutton.ics.brapi.server.base.BaseServerResource;

import static jhi.germinate.server.database.tables.Datasetmembers.*;
import static jhi.germinate.server.database.tables.Datasets.*;

/**
 * @author Sebastian Raubach
 */
public abstract class VariantSetBaseServerResource extends BaseServerResource
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
											 .and(DATASETS.IS_EXTERNAL.eq(false))
											 .and(DATASETS.DATASET_STATE_ID.eq(1));

		if (conditions != null)
		{
			for (Condition condition : conditions)
				step.and(condition);
		}

		List<VariantSet> result = step.limit(pageSize)
									  .offset(pageSize * currentPage)
									  .fetchInto(VariantSet.class);

		result.forEach(vs -> {
			try
			{
				String serverBase = Brapi.getServerBase(ServletUtils.getRequest(getRequest()));
				URI uri = URI.create(serverBase + "/api" + Brapi.BRAPI.urlPrefix + "/files/genotypes/" + vs.getVariantSetDbId());
				vs.setAvailableFormats(Collections.singletonList(new Format()
					.setDataFormat("Flapjack")
					.setFileFormat("text/tab-separated-values")
					.setFileURL(uri)));
			}
			catch (IllegalArgumentException e)
			{
				e.printStackTrace();
			}
		});

		return result;
	}
}

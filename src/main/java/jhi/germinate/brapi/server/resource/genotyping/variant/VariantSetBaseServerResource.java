package jhi.germinate.brapi.server.resource.genotyping.variant;

import jhi.germinate.brapi.server.Brapi;
import org.jooq.*;
import org.jooq.impl.DSL;
import uk.ac.hutton.ics.brapi.resource.genotyping.variant.*;
import uk.ac.hutton.ics.brapi.server.base.TokenBaseServerResource;

import javax.servlet.http.HttpServletRequest;
import java.net.URI;
import java.util.*;

import static jhi.germinate.server.database.codegen.tables.Datasetmembers.*;
import static jhi.germinate.server.database.codegen.tables.Datasets.*;

public interface VariantSetBaseServerResource
{
	default List<VariantSet> getVariantSets(DSLContext context, List<Condition> conditions, HttpServletRequest req, int page, int pageSize)
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
									  .offset(pageSize * page)
									  .fetchInto(VariantSet.class);

		// TODO: Put this back in once we can configure the proxy reverse to actually forward the context path
		result.forEach(vs -> {
			try
			{
				URI uri = URI.create(Brapi.getServerBase(req) + "/api" + Brapi.BRAPI.urlPrefix + "/files/genotypes/" + vs.getVariantSetDbId());
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

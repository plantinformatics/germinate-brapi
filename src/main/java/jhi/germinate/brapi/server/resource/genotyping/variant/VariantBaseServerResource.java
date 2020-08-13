package jhi.germinate.brapi.server.resource.genotyping.variant;

import com.google.gson.JsonElement;

import org.jooq.*;

import java.util.*;
import java.util.stream.Collectors;

import jhi.germinate.server.util.CollectionUtils;
import uk.ac.hutton.ics.brapi.resource.genotyping.variant.Variant;
import uk.ac.hutton.ics.brapi.server.base.TokenBaseServerResource;

import static jhi.germinate.server.database.tables.Datasetmembers.*;
import static jhi.germinate.server.database.tables.ViewTableMarkers.*;

/**
 * @author Sebastian Raubach
 */
public abstract class VariantBaseServerResource extends TokenBaseServerResource
{
	protected List<Variant> getVariantsInternal(DSLContext context, List<Condition> conditions)
	{
		SelectConditionStep<?> step = context.select()
											 .hint("SQL_CALC_FOUND_ROWS")
											 .from(DATASETMEMBERS)
											 .leftJoin(VIEW_TABLE_MARKERS).on(DATASETMEMBERS.FOREIGN_ID.eq(VIEW_TABLE_MARKERS.MARKER_ID))
											 .where(DATASETMEMBERS.DATASETMEMBERTYPE_ID.eq(1));

		if (!CollectionUtils.isEmpty(conditions))
		{
			for (Condition condition : conditions)
				step.and(condition);
		}

		return step.limit(pageSize)
				   .offset(pageSize * currentPage)
				   .stream()
				   .map(m -> {
					   Variant result = new Variant()
						   .setVariantDbId(m.get(DATASETMEMBERS.DATASET_ID) + "-" + m.get(VIEW_TABLE_MARKERS.MARKER_ID))
						   .setCreated(m.get(VIEW_TABLE_MARKERS.CREATED_ON))
						   .setUpdated(m.get(VIEW_TABLE_MARKERS.UPDATED_ON))
						   .setVariantType(m.get(VIEW_TABLE_MARKERS.MARKER_TYPE));

					   List<String> names = new ArrayList<>();
					   names.add(m.get(VIEW_TABLE_MARKERS.MARKER_NAME));

					   if (m.get(VIEW_TABLE_MARKERS.MARKER_SYNONYMS) != null)
					   {
						   for (JsonElement name : m.get(VIEW_TABLE_MARKERS.MARKER_SYNONYMS))
							   names.add(name.getAsString());
					   }

					   result.setVariantNames(names);
					   result.setVariantSetDbId(Collections.singletonList(Integer.toString(m.get(DATASETMEMBERS.DATASET_ID))));

					   return result;
				   })
				   .collect(Collectors.toList());
	}
}

package jhi.germinate.brapi.server.resource.genotyping.variant;

import com.google.gson.JsonElement;

import org.jooq.*;

import java.util.*;
import java.util.stream.Collectors;

import jhi.germinate.brapi.resource.variant.Variant;
import jhi.germinate.brapi.server.resource.TokenBaseServerResource;
import jhi.germinate.server.database.tables.pojos.ViewTableMarkers;
import jhi.germinate.server.util.CollectionUtils;

import static jhi.germinate.server.database.tables.Datasetmembers.*;
import static jhi.germinate.server.database.tables.ViewTableMarkers.*;

/**
 * @author Sebastian Raubach
 */
public abstract class VariantBaseServerResource<T> extends TokenBaseServerResource<T>
{
	protected List<Variant> getVariants(DSLContext context, List<Condition> conditions)
	{
		Map<Integer, List<Integer>> markerToDataset = context.selectFrom(DATASETMEMBERS)
															 .where(DATASETMEMBERS.DATASETMEMBERTYPE_ID.eq(1))
															 .fetchGroups(DATASETMEMBERS.FOREIGN_ID, DATASETMEMBERS.DATASET_ID);


		SelectJoinStep<?> step = context.select()
										.hint("SQL_CALC_FOUND_ROWS")
										.from(VIEW_TABLE_MARKERS);

		if (!CollectionUtils.isEmpty(conditions))
		{
			for (Condition condition : conditions)
				step.where(condition);
		}

		return step.limit(pageSize)
				   .offset(pageSize * currentPage)
				   .fetchInto(ViewTableMarkers.class)
				   .stream()
				   .map(m -> {
					   Variant result = new Variant()
						   .setVariantDbId(Integer.toString(m.getMarkerId()))
						   .setCreated(m.getCreatedOn())
						   .setUpdated(m.getUpdatedOn())
						   .setVariantType(m.getMarkerType());

					   List<String> names = new ArrayList<>();
					   names.add(m.getMarkerName());

					   if (m.getMarkerSynonyms() != null)
					   {
						   for (JsonElement name : m.getMarkerSynonyms())
							   names.add(name.getAsString());
					   }

					   result.setVariantNames(names);

					   List<Integer> datasetIds = markerToDataset.get(m.getMarkerId());
					   if (!CollectionUtils.isEmpty(datasetIds))
					   {
						   result.setVariantSetDbId(datasetIds.stream()
															  .map(i -> Integer.toString(i))
															  .collect(Collectors.toList()));
					   }

					   return result;
				   })
				   .collect(Collectors.toList());
	}
}

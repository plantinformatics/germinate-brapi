package jhi.germinate.brapi.server.resource.genotyping.variant;

import jhi.germinate.server.AuthenticationFilter;
import jhi.germinate.server.resource.datasets.DatasetTableResource;
import jhi.germinate.server.util.CollectionUtils;
import org.jooq.*;
import uk.ac.hutton.ics.brapi.resource.genotyping.variant.Variant;

import jakarta.servlet.http.*;
import jakarta.ws.rs.core.SecurityContext;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

import static jhi.germinate.server.database.codegen.tables.Datasetmembers.*;
import static jhi.germinate.server.database.codegen.tables.ViewTableMarkers.*;

public interface VariantBaseServerResource
{
	default List<Variant> getVariantsInternal(DSLContext context, List<Condition> conditions, int page, int pageSize, HttpServletRequest req, HttpServletResponse resp, SecurityContext securityContext)
		throws SQLException
	{
		AuthenticationFilter.UserDetails userDetails = (AuthenticationFilter.UserDetails) securityContext.getUserPrincipal();
		List<Integer> datasetIds = DatasetTableResource.getDatasetIdsForUser(req, resp, userDetails, "genotype");

		SelectConditionStep<?> step = context.select()
											 .hint("SQL_CALC_FOUND_ROWS")
											 .from(DATASETMEMBERS)
											 .leftJoin(VIEW_TABLE_MARKERS).on(DATASETMEMBERS.FOREIGN_ID.eq(VIEW_TABLE_MARKERS.MARKER_ID))
											 .where(DATASETMEMBERS.DATASET_ID.in(datasetIds))
											 .and(DATASETMEMBERS.DATASETMEMBERTYPE_ID.eq(1));

		if (!CollectionUtils.isEmpty(conditions))
		{
			for (Condition condition : conditions)
				step.and(condition);
		}

		return step.limit(pageSize)
				   .offset(pageSize * page)
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
						   Collections.addAll(names, m.get(VIEW_TABLE_MARKERS.MARKER_SYNONYMS));
					   }

					   result.setVariantNames(names);
					   result.setVariantSetDbId(Collections.singletonList(Integer.toString(m.get(DATASETMEMBERS.DATASET_ID))));

					   return result;
				   })
				   .collect(Collectors.toList());
	}
}

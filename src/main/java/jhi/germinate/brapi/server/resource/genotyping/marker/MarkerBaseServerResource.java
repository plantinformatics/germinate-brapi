package jhi.germinate.brapi.server.resource.genotyping.marker;

import org.jooq.*;

import java.util.List;

import uk.ac.hutton.ics.brapi.resource.genotyping.map.MarkerPosition;
import uk.ac.hutton.ics.brapi.server.base.BaseServerResource;

import static jhi.germinate.server.database.tables.Mapdefinitions.*;
import static jhi.germinate.server.database.tables.Maps.*;
import static jhi.germinate.server.database.tables.Markers.*;

/**
 * @author Sebastian Raubach
 */
public abstract class MarkerBaseServerResource extends BaseServerResource
{
	protected List<MarkerPosition> getMarkerPositions(DSLContext context, List<Condition> conditions)
	{
		SelectConditionStep<?> step = context.select(
			MAPDEFINITIONS.CHROMOSOME.as("linkageGroupName"),
			MAPS.ID.as("mapDbId"),
			MAPS.NAME.as("mapName"),
			MAPDEFINITIONS.DEFINITION_START.as("position"),
			MARKERS.ID.as("variantDbId"),
			MARKERS.MARKER_NAME.as("variantName")
		)
											 .hint("SQL_CALC_FOUND_ROWS")
											 .from(MAPS)
											 .leftJoin(MAPDEFINITIONS).on(MAPDEFINITIONS.MAP_ID.eq(MAPS.ID))
											 .leftJoin(MARKERS).on(MARKERS.ID.eq(MAPDEFINITIONS.MARKER_ID))
											 .where(MAPS.VISIBILITY.eq(true));

		if (conditions != null)
		{
			for (Condition condition : conditions)
				step.and(condition);
		}

		return step.limit(pageSize)
				   .offset(pageSize * currentPage)
				   .fetchInto(MarkerPosition.class);
	}
}

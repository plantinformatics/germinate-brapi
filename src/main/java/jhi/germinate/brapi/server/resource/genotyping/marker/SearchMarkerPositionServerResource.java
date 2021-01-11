package jhi.germinate.brapi.server.resource.genotyping.marker;

import org.jooq.*;
import org.restlet.data.Status;
import org.restlet.resource.*;

import java.sql.*;
import java.util.*;

import jhi.germinate.server.Database;
import jhi.germinate.server.util.CollectionUtils;
import uk.ac.hutton.ics.brapi.resource.base.*;
import uk.ac.hutton.ics.brapi.resource.genotyping.map.*;
import uk.ac.hutton.ics.brapi.server.genotyping.marker.BrapiSearchMarkerPositionServerResource;

import static jhi.germinate.server.database.codegen.tables.Mapdefinitions.*;
import static jhi.germinate.server.database.codegen.tables.Maps.*;
import static jhi.germinate.server.database.codegen.tables.Markers.*;

/**
 * @author Sebastian Raubach
 */
public class SearchMarkerPositionServerResource extends MarkerBaseServerResource implements BrapiSearchMarkerPositionServerResource
{
	@Post
	public BaseResult<ArrayResult<MarkerPosition>> postMarkerPositionSearch(MarkerPositionSearch search)
	{
		if (search == null)
			throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST);

		try (DSLContext context = Database.getContext())
		{
			List<Condition> conditions = new ArrayList<>();

			if (!CollectionUtils.isEmpty(search.getMapDbIds()))
				conditions.add(MAPS.ID.cast(String.class).in(search.getMapDbIds()));
			if (!CollectionUtils.isEmpty(search.getLinkageGroupNames()))
				conditions.add(MAPDEFINITIONS.CHROMOSOME.in(search.getLinkageGroupNames()));
			if (!CollectionUtils.isEmpty(search.getVariantDbIds()))
				conditions.add(MARKERS.ID.cast(String.class).in(search.getVariantDbIds()));
			if (search.getMaxPosition() != null)
				conditions.add(MAPDEFINITIONS.DEFINITION_START.le(search.getMaxPosition()));
			if (search.getMinPosition() != null)
				conditions.add(MAPDEFINITIONS.DEFINITION_START.ge(search.getMinPosition()));

			List<MarkerPosition> result = getMarkerPositions(context, conditions);

			long totalCount = context.fetchOne("SELECT FOUND_ROWS()").into(Long.class);
			return new BaseResult<>(new ArrayResult<MarkerPosition>()
				.setData(result), currentPage, pageSize, totalCount);
		}
	}

	@Post
	public BaseResult<SearchResult> postMarkerPositionSearchAsync(MarkerPositionSearch markerPositionSearch)
	{
		throw new ResourceException(Status.SERVER_ERROR_NOT_IMPLEMENTED);
	}

	@Get
	public BaseResult<ArrayResult<MarkerPosition>> getMarkerPositionSearchAsync()
	{
		throw new ResourceException(Status.SERVER_ERROR_NOT_IMPLEMENTED);
	}


}

package jhi.germinate.brapi.server.resource.genotyping.variant;

import org.jooq.*;
import org.jooq.impl.DSL;
import org.restlet.data.Status;
import org.restlet.resource.*;

import java.sql.*;
import java.util.*;

import jhi.germinate.server.Database;
import jhi.germinate.server.util.CollectionUtils;
import uk.ac.hutton.ics.brapi.resource.base.*;
import uk.ac.hutton.ics.brapi.resource.genotyping.variant.*;
import uk.ac.hutton.ics.brapi.server.genotyping.variant.BrapiSearchVariantServerResource;

import static jhi.germinate.server.database.codegen.tables.Datasetmembers.*;
import static jhi.germinate.server.database.codegen.tables.Markers.*;
import static jhi.germinate.server.database.codegen.tables.ViewTableMarkers.*;

/**
 * @author Sebastian Raubach
 */
public class SearchVariantServerResource extends VariantBaseServerResource implements BrapiSearchVariantServerResource
{
	@Post
	public TokenBaseResult<ArrayResult<Variant>> postVariantSearch(VariantSearch search)
	{
		try (Connection conn = Database.getConnection();
			 DSLContext context = Database.getContext(conn))
		{
			List<Condition> conditions = new ArrayList<>();

			if (!CollectionUtils.isEmpty(search.getVariantDbIds()))
				conditions.add(MARKERS.ID.cast(String.class).in(search.getVariantDbIds()));
			if (!CollectionUtils.isEmpty(search.getVariantSetDbIds()))
				conditions.add(DSL.exists(DSL.selectOne().from(DATASETMEMBERS)
											 .where(DATASETMEMBERS.DATASETMEMBERTYPE_ID.eq(1))
											 .and(DATASETMEMBERS.DATASET_ID.cast(String.class).in(search.getVariantSetDbIds()))
											 .and(DATASETMEMBERS.FOREIGN_ID.eq(VIEW_TABLE_MARKERS.MARKER_ID))));

			List<Variant> variants = getVariantsInternal(context, conditions);
			long totalCount = context.fetchOne("SELECT FOUND_ROWS()").into(Long.class);
			return new TokenBaseResult<>(new ArrayResult<Variant>()
				.setData(variants), currentPage, pageSize, totalCount);
		}
		catch (SQLException e)
		{
			e.printStackTrace();
			throw new ResourceException(Status.SERVER_ERROR_INTERNAL);
		}
	}

	@Post
	public BaseResult<SearchResult> postVariantSearchAsync(VariantSearch variantSearch)
	{
		throw new ResourceException(Status.SERVER_ERROR_NOT_IMPLEMENTED);
	}

	@Get
	public TokenBaseResult<ArrayResult<Variant>> getVariantSearchAsync()
	{
		throw new ResourceException(Status.SERVER_ERROR_NOT_IMPLEMENTED);
	}
}

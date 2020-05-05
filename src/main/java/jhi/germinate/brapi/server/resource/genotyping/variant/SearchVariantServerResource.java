package jhi.germinate.brapi.server.resource.genotyping.variant;

import org.jooq.*;
import org.jooq.impl.DSL;
import org.restlet.data.Status;
import org.restlet.resource.*;

import java.sql.*;
import java.util.*;

import jhi.germinate.brapi.resource.base.*;
import jhi.germinate.brapi.resource.variant.*;
import jhi.germinate.server.Database;
import jhi.germinate.server.util.CollectionUtils;

import static jhi.germinate.server.database.tables.Datasetmembers.*;
import static jhi.germinate.server.database.tables.Markers.*;
import static jhi.germinate.server.database.tables.ViewTableMarkers.*;

/**
 * @author Sebastian Raubach
 */
public class SearchVariantServerResource extends VariantBaseServerResource<ArrayResult<Variant>>
{
	@Override
	public TokenBaseResult<ArrayResult<Variant>> getJson()
	{
		throw new ResourceException(Status.SERVER_ERROR_NOT_IMPLEMENTED);
	}

	@Post
	public TokenBaseResult<ArrayResult<Variant>> postJson(VariantSearch search)
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

			List<Variant> variants = getVariants(context, conditions);
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
}

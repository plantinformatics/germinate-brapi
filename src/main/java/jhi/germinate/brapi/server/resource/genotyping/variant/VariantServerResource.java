package jhi.germinate.brapi.server.resource.genotyping.variant;

import org.jooq.*;
import org.jooq.impl.DSL;
import org.restlet.data.Status;
import org.restlet.resource.ResourceException;

import java.sql.*;
import java.util.*;

import jhi.germinate.brapi.resource.base.ArrayResult;
import jhi.germinate.brapi.resource.base.TokenBaseResult;
import jhi.germinate.brapi.resource.variant.Variant;
import jhi.germinate.server.Database;
import jhi.germinate.server.util.StringUtils;

import static jhi.germinate.server.database.tables.Datasetmembers.*;
import static jhi.germinate.server.database.tables.Markers.*;
import static jhi.germinate.server.database.tables.ViewTableMarkers.*;

/**
 * @author Sebastian Raubach
 */
public class VariantServerResource extends VariantBaseServerResource<ArrayResult<Variant>>
{
	private static final String PARAM_VARIANT_DB_ID     = "variantDbId";
	private static final String PARAM_VARIANT_SET_DB_ID = "variantSetDbId";

	private String variantDbId;
	private String variantSetDbId;

	@Override
	public void doInit()
	{
		super.doInit();

		this.variantDbId = getQueryValue(PARAM_VARIANT_DB_ID);
		this.variantSetDbId = getQueryValue(PARAM_VARIANT_SET_DB_ID);
	}

	@Override
	public TokenBaseResult<ArrayResult<Variant>> getJson()
	{
		try (Connection conn = Database.getConnection();
			 DSLContext context = Database.getContext(conn))
		{
			List<Condition> conditions = new ArrayList<>();

			if (!StringUtils.isEmpty(variantDbId))
				conditions.add(MARKERS.ID.cast(String.class).eq(variantDbId));
			if (!StringUtils.isEmpty(variantSetDbId))
				conditions.add(DSL.exists(DSL.selectOne().from(DATASETMEMBERS)
											 .where(DATASETMEMBERS.DATASETMEMBERTYPE_ID.eq(1))
											 .and(DATASETMEMBERS.DATASET_ID.cast(String.class).eq(variantSetDbId))
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

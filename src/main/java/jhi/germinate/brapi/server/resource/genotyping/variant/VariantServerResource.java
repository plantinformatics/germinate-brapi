package jhi.germinate.brapi.server.resource.genotyping.variant;

import org.jooq.*;
import org.jooq.impl.DSL;
import org.restlet.data.Status;
import org.restlet.resource.ResourceException;

import java.sql.*;
import java.util.*;

import jhi.germinate.brapi.resource.base.*;
import jhi.germinate.brapi.resource.variant.Variant;
import jhi.germinate.server.Database;
import jhi.germinate.server.util.StringUtils;

import static jhi.germinate.server.database.tables.Datasetmembers.*;
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
				conditions.add(DSL.concat(DATASETMEMBERS.DATASET_ID, DSL.val("-"), VIEW_TABLE_MARKERS.MARKER_ID).eq(variantDbId));
			if (!StringUtils.isEmpty(variantSetDbId))
				conditions.add(DATASETMEMBERS.DATASET_ID.cast(String.class).eq(variantSetDbId));

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

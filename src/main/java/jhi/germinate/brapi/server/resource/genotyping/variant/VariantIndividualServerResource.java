package jhi.germinate.brapi.server.resource.genotyping.variant;

import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.restlet.data.Status;
import org.restlet.resource.ResourceException;

import java.sql.*;
import java.util.*;

import jhi.germinate.brapi.resource.base.TokenBaseResult;
import jhi.germinate.brapi.resource.variant.Variant;
import jhi.germinate.server.Database;
import jhi.germinate.server.util.CollectionUtils;

import static jhi.germinate.server.database.tables.Datasetmembers.*;
import static jhi.germinate.server.database.tables.ViewTableMarkers.*;

/**
 * @author Sebastian Raubach
 */
public class VariantIndividualServerResource extends VariantBaseServerResource<Variant>
{
	private String variantDbId;

	@Override
	public void doInit()
	{
		super.doInit();

		try
		{
			this.variantDbId = getRequestAttributes().get("variantDbId").toString();
		}
		catch (Exception e)
		{
		}
	}

	@Override
	public TokenBaseResult<Variant> getJson()
	{
		try (Connection conn = Database.getConnection();
			 DSLContext context = Database.getContext(conn))
		{
			List<Variant> variants = getVariants(context, Collections.singletonList(DSL.concat(DATASETMEMBERS.DATASET_ID, DSL.val("-"), VIEW_TABLE_MARKERS.MARKER_ID).eq(variantDbId)));

			if (CollectionUtils.isEmpty(variants))
				return new TokenBaseResult<>(null, currentPage, pageSize, 0);
			else
				return new TokenBaseResult<>(variants.get(0), currentPage, pageSize, 1);
		}
		catch (SQLException e)
		{
			e.printStackTrace();
			throw new ResourceException(Status.SERVER_ERROR_INTERNAL);
		}
	}
}

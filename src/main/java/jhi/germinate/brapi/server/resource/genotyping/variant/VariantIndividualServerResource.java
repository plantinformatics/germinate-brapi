package jhi.germinate.brapi.server.resource.genotyping.variant;

import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.restlet.data.Status;
import org.restlet.resource.*;

import java.sql.*;
import java.util.*;

import jhi.germinate.server.Database;
import jhi.germinate.server.util.CollectionUtils;
import uk.ac.hutton.ics.brapi.resource.base.BaseResult;
import uk.ac.hutton.ics.brapi.resource.genotyping.variant.Variant;
import uk.ac.hutton.ics.brapi.server.genotyping.variant.BrapiVariantIndividualServerResource;

import static jhi.germinate.server.database.tables.Datasetmembers.*;
import static jhi.germinate.server.database.tables.ViewTableMarkers.*;

/**
 * @author Sebastian Raubach
 */
public class VariantIndividualServerResource extends VariantBaseServerResource implements BrapiVariantIndividualServerResource
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

	@Get
	public BaseResult<Variant> getVariantById()
	{
		try (Connection conn = Database.getConnection();
			 DSLContext context = Database.getContext(conn))
		{
			List<Variant> variants = getVariantsInternal(context, Collections.singletonList(DSL.concat(DATASETMEMBERS.DATASET_ID, DSL.val("-"), VIEW_TABLE_MARKERS.MARKER_ID).eq(variantDbId)));

			if (CollectionUtils.isEmpty(variants))
				return new BaseResult<>(null, currentPage, pageSize, 0);
			else
				return new BaseResult<>(variants.get(0), currentPage, pageSize, 1);
		}
		catch (SQLException e)
		{
			e.printStackTrace();
			throw new ResourceException(Status.SERVER_ERROR_INTERNAL);
		}
	}
}

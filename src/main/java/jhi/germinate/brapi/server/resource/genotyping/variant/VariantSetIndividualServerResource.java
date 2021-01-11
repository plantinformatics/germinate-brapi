package jhi.germinate.brapi.server.resource.genotyping.variant;

import org.jooq.DSLContext;
import org.restlet.data.Status;
import org.restlet.resource.*;

import java.sql.*;
import java.util.*;

import jhi.germinate.server.Database;
import jhi.germinate.server.util.CollectionUtils;
import uk.ac.hutton.ics.brapi.resource.base.BaseResult;
import uk.ac.hutton.ics.brapi.resource.genotyping.variant.VariantSet;
import uk.ac.hutton.ics.brapi.server.genotyping.variant.BrapiVariantSetIndividualServerResource;

import static jhi.germinate.server.database.codegen.tables.Datasets.*;

/**
 * @author Sebastian Raubach
 */
public class VariantSetIndividualServerResource extends VariantSetBaseServerResource implements BrapiVariantSetIndividualServerResource
{
	private String variantSetDbId;

	@Override
	public void doInit()
	{
		super.doInit();

		try
		{
			this.variantSetDbId = getRequestAttributes().get("variantSetDbId").toString();
		}
		catch (Exception e)
		{
		}
	}

	@Get
	public BaseResult<VariantSet> getVariantSetById()
	{
		try (DSLContext context = Database.getContext())
		{
			List<VariantSet> results = getVariantSets(context, Collections.singletonList(DATASETS.ID.cast(String.class).eq(variantSetDbId)));
			VariantSet result = CollectionUtils.isEmpty(results) ? null : results.get(0);

			return new BaseResult<>(result, currentPage, pageSize, 1);
		}
	}
}

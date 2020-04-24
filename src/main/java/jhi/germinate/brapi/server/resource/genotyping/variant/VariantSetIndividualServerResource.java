package jhi.germinate.brapi.server.resource.genotyping.variant;

import org.jooq.*;
import org.restlet.data.Status;
import org.restlet.resource.ResourceException;

import java.sql.*;
import java.util.*;

import jhi.germinate.brapi.resource.base.BaseResult;
import jhi.germinate.brapi.resource.variant.VariantSet;
import jhi.germinate.server.Database;
import jhi.germinate.server.util.*;

import static jhi.germinate.server.database.tables.Datasets.*;

/**
 * @author Sebastian Raubach
 */
public class VariantSetIndividualServerResource extends VariantSetBaseServerResource<VariantSet>
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

	@Override
	public BaseResult<VariantSet> getJson()
	{
		if (StringUtils.isEmpty(variantSetDbId))
			throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST);

		try (Connection conn = Database.getConnection();
			 DSLContext context = Database.getContext(conn))
		{
			List<Condition> conditions = new ArrayList<>();

			conditions.add(DATASETS.ID.cast(String.class).eq(variantSetDbId));

			List<VariantSet> results = getVariantSets(context, conditions);
			VariantSet result = CollectionUtils.isEmpty(results) ? null : results.get(0);

			return new BaseResult<>(result, currentPage, pageSize, 1);
		}
		catch (SQLException e)
		{
			e.printStackTrace();
			throw new ResourceException(Status.SERVER_ERROR_INTERNAL);
		}
	}
}

package jhi.germinate.brapi.server.resource.genotyping.call;

import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.restlet.data.Status;
import org.restlet.resource.*;

import java.sql.*;
import java.util.*;

import jhi.germinate.server.Database;
import jhi.germinate.server.util.CollectionUtils;
import uk.ac.hutton.ics.brapi.resource.base.BaseResult;
import uk.ac.hutton.ics.brapi.resource.genotyping.call.CallSet;
import uk.ac.hutton.ics.brapi.server.genotyping.call.BrapiCallSetIndividualServerResource;

import static jhi.germinate.server.database.tables.Datasetmembers.*;
import static jhi.germinate.server.database.tables.Germinatebase.*;

/**
 * @author Sebastian Raubach
 */
public class CallSetIndividualServerResource extends CallSetBaseServerResource implements BrapiCallSetIndividualServerResource
{
	private String callSetDbId;

	@Override
	public void doInit()
	{
		super.doInit();

		try
		{
			this.callSetDbId = getRequestAttributes().get("callSetDbId").toString();
		}
		catch (Exception e)
		{
		}
	}

	@Get
	public BaseResult<CallSet> getCallSetById()
	{
		try (Connection conn = Database.getConnection();
			 DSLContext context = Database.getContext(conn))
		{
			List<CallSet> callSets = getCallSets(context, Collections.singletonList(DSL.concat(DATASETMEMBERS.DATASET_ID, DSL.val("-"), GERMINATEBASE.ID).eq(callSetDbId)));

			if (CollectionUtils.isEmpty(callSets))
				return new BaseResult<>(null, currentPage, pageSize, 0);
			else
				return new BaseResult<>(callSets.get(0), currentPage, pageSize, 1);
		}
		catch (SQLException e)
		{
			e.printStackTrace();
			throw new ResourceException(Status.SERVER_ERROR_INTERNAL);
		}
	}
}

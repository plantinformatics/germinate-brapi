package jhi.germinate.brapi.server.resource.core.trial;

import org.jooq.*;
import org.restlet.data.Status;
import org.restlet.resource.ResourceException;

import java.sql.*;
import java.util.*;

import jhi.germinate.brapi.resource.base.BaseResult;
import jhi.germinate.brapi.resource.trial.Trial;
import jhi.germinate.server.Database;
import jhi.germinate.server.util.CollectionUtils;

import static jhi.germinate.server.database.tables.ViewTableExperiments.*;

/**
 * @author Sebastian Raubach
 */
public class TrialIndividualServerResource extends TrialBaseServerResource<Trial>
{
	private String trialDbId;

	@Override
	public void doInit()
	{
		super.doInit();

		try
		{
			this.trialDbId = getRequestAttributes().get("trialDbId").toString();
		}
		catch (Exception e)
		{
		}
	}

	@Override
	public BaseResult<Trial> getJson()
	{
		try (Connection conn = Database.getConnection();
			 DSLContext context = Database.getContext(conn))
		{
			List<Trial> result = getTrials(context, Collections.singletonList(VIEW_TABLE_EXPERIMENTS.EXPERIMENT_ID.cast(String.class).eq(trialDbId)));

			if (CollectionUtils.isEmpty(result))
				return new BaseResult<>(null, currentPage, pageSize, 0);
			else
				return new BaseResult<>(result.get(0), currentPage, pageSize, 1);
		}
		catch (SQLException e)
		{
			e.printStackTrace();
			throw new ResourceException(Status.SERVER_ERROR_INTERNAL);
		}
	}
}

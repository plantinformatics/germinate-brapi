package jhi.germinate.brapi.server.resource.core.trial;

import org.jooq.DSLContext;
import org.restlet.data.Status;
import org.restlet.resource.*;

import java.sql.*;
import java.util.*;

import jhi.germinate.server.Database;
import jhi.germinate.server.util.CollectionUtils;
import uk.ac.hutton.ics.brapi.resource.base.BaseResult;
import uk.ac.hutton.ics.brapi.resource.core.trial.Trial;
import uk.ac.hutton.ics.brapi.server.core.trial.BrapiTrialIndividualServerResource;

import static jhi.germinate.server.database.codegen.tables.ViewTableExperiments.*;

/**
 * @author Sebastian Raubach
 */
public class TrialIndividualServerResource extends TrialBaseServerResource implements BrapiTrialIndividualServerResource
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

	@Get
	public BaseResult<Trial> getTrialById()
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

	@Put
	public BaseResult<Trial> putTrialById(Trial trial)
	{
		throw new ResourceException(Status.SERVER_ERROR_NOT_IMPLEMENTED);
	}
}

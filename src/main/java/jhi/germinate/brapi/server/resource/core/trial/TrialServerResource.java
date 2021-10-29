package jhi.germinate.brapi.server.resource.core.trial;

import jhi.germinate.resource.enums.UserType;
import jhi.germinate.server.Database;
import jhi.germinate.server.util.*;
import org.jooq.*;
import org.jooq.impl.DSL;
import uk.ac.hutton.ics.brapi.resource.base.*;
import uk.ac.hutton.ics.brapi.resource.core.trial.Trial;
import uk.ac.hutton.ics.brapi.server.core.trial.BrapiTrialServerResource;

import javax.annotation.security.PermitAll;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.io.IOException;
import java.sql.Date;
import java.sql.*;
import java.util.*;

import static jhi.germinate.server.database.codegen.tables.Datasets.*;
import static jhi.germinate.server.database.codegen.tables.Experiments.*;

/**
 * @author Sebastian Raubach
 */
@Path("brapi/v2/trials")
public class TrialServerResource extends TrialBaseServerResource implements BrapiTrialServerResource
{
	@GET
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Secured
	@PermitAll
	public BaseResult<ArrayResult<Trial>> getTrials(@QueryParam("active") String active,
													@QueryParam("commonCropName") String commonCropName,
													@QueryParam("contactDbId") String contactDbId,
													@QueryParam("programDbId") String programDbId,
													@QueryParam("locationDbId") String locationDbId,
													@QueryParam("searchDateRangeStart") String searchDateRangeStart,
													@QueryParam("searchDateRangeEnd") String searchDateRangeEnd,
													@QueryParam("studyDbId") String studyDbId,
													@QueryParam("trialDbId") String trialDbId,
													@QueryParam("trialName") String trialName,
													@QueryParam("trialPUI") String trialPUI,
													@QueryParam("sortBy") String sortBy,
													@QueryParam("sortOrder") String sortOrder,
													@QueryParam("externalReferenceID") String externalReferenceID,
													@QueryParam("externalReferenceSource") String externalReferenceSource)
		throws SQLException, IOException
	{
		try (Connection conn = Database.getConnection())
		{
			DSLContext context = Database.getContext(conn);
			List<Condition> conditions = new ArrayList<>();

			if (searchDateRangeStart != null)
			{
				try
				{
					conditions.add(EXPERIMENTS.EXPERIMENT_DATE.ge(new Date(DateTimeUtils.parseDate(searchDateRangeStart).getTime())));
				}
				catch (Exception e)
				{
				}
			}
			if (searchDateRangeEnd != null)
			{
				try
				{
					conditions.add(EXPERIMENTS.EXPERIMENT_DATE.le(new Date(DateTimeUtils.parseDate(searchDateRangeEnd).getTime())));
				}
				catch (Exception e)
				{
				}
			}
			if (!StringUtils.isEmpty(studyDbId))
				conditions.add(DSL.exists(DSL.selectOne().from(DATASETS).where(DATASETS.EXPERIMENT_ID.eq(EXPERIMENTS.ID)).and(DATASETS.ID.cast(String.class).eq(studyDbId))));
			if (!StringUtils.isEmpty(trialDbId))
				conditions.add(EXPERIMENTS.ID.cast(String.class).eq(trialDbId));
			if (!StringUtils.isEmpty(trialName))
				conditions.add(EXPERIMENTS.EXPERIMENT_NAME.eq(trialName));

			List<Trial> result = getTrials(context, conditions);

			long totalCount = context.fetchOne("SELECT FOUND_ROWS()").into(Long.class);
			return new BaseResult<>(new ArrayResult<Trial>()
				.setData(result), page, pageSize, totalCount);
		}
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Secured(UserType.DATA_CURATOR)
	public BaseResult<ArrayResult<Trial>> postTrials(Trial[] newTrials)
		throws SQLException, IOException
	{
		resp.sendError(Response.Status.NOT_IMPLEMENTED.getStatusCode());
		return null;
	}

	@GET
	@Path("/{trialsDbId}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Secured
	@PermitAll
	public BaseResult<Trial> getTrialById(@QueryParam("trialsDbId") String trialsDbId)
		throws SQLException, IOException
	{
		try (Connection conn = Database.getConnection())
		{
			DSLContext context = Database.getContext(conn);
			List<Trial> result = getTrials(context, Collections.singletonList(EXPERIMENTS.ID.cast(String.class).eq(trialsDbId)));

			if (CollectionUtils.isEmpty(result))
				return new BaseResult<>(null, page, pageSize, 0);
			else
				return new BaseResult<>(result.get(0), page, pageSize, 1);
		}
	}

	@PUT
	@Path("/{trialsDbId}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Secured(UserType.DATA_CURATOR)
	public BaseResult<Trial> putTrialById(@QueryParam("trialsDbId") String trialsDbId, Trial trial)
		throws SQLException, IOException
	{
		resp.sendError(Response.Status.NOT_IMPLEMENTED.getStatusCode());
		return null;
	}

}

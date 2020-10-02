package jhi.germinate.brapi.server.resource.genotyping.call;

import org.jooq.*;
import org.jooq.impl.DSL;
import org.restlet.data.Status;
import org.restlet.resource.*;

import java.sql.*;
import java.util.*;

import jhi.germinate.server.Database;
import jhi.germinate.server.util.StringUtils;
import uk.ac.hutton.ics.brapi.resource.base.*;
import uk.ac.hutton.ics.brapi.resource.genotyping.call.CallSet;
import uk.ac.hutton.ics.brapi.server.genotyping.call.BrapiCallSetServerResource;

import static jhi.germinate.server.database.codegen.tables.Datasetmembers.*;
import static jhi.germinate.server.database.codegen.tables.Germinatebase.*;

/**
 * @author Sebastian Raubach
 */
public class CallSetServerResource extends CallSetBaseServerResource implements BrapiCallSetServerResource
{
	private static final String PARAM_CALL_SET_DB_ID    = "callSetDbId";
	private static final String PARAM_CALL_SET_NAME     = "callSetName";
	private static final String PARAM_VARIANT_SET_DB_ID = "variantSetDbId";
	private static final String PARAM_SAMPLE_DB_ID      = "sampleDbId";
	private static final String PARAM_GERMPLASM_DB_ID   = "germplasmDbId";

	private String callSetDbId;
	private String callSetName;
	private String variantSetDbId;
	private String sampleDbId;
	private String germplasmDbId;

	@Override
	public void doInit()
	{
		super.doInit();

		this.callSetDbId = getQueryValue(PARAM_CALL_SET_DB_ID);
		this.callSetName = getQueryValue(PARAM_CALL_SET_NAME);
		this.variantSetDbId = getQueryValue(PARAM_VARIANT_SET_DB_ID);
		this.sampleDbId = getQueryValue(PARAM_SAMPLE_DB_ID);
		this.germplasmDbId = getQueryValue(PARAM_GERMPLASM_DB_ID);
	}

	@Get
	public BaseResult<ArrayResult<CallSet>> getCallsets()
	{
		try (Connection conn = Database.getConnection();
			 DSLContext context = Database.getContext(conn))
		{
			List<Condition> conditions = new ArrayList<>();

			if (!StringUtils.isEmpty(callSetDbId))
				conditions.add(DSL.concat(DATASETMEMBERS.DATASET_ID, DSL.val("-"), GERMINATEBASE.ID).eq(callSetDbId));
			if (!StringUtils.isEmpty(callSetName))
				conditions.add(GERMINATEBASE.NAME.eq(callSetName));
			if (!StringUtils.isEmpty(variantSetDbId))
				conditions.add(DATASETMEMBERS.DATASET_ID.cast(String.class).eq(variantSetDbId));
			if (!StringUtils.isEmpty(germplasmDbId))
				conditions.add(GERMINATEBASE.ID.cast(String.class).eq(germplasmDbId));

			List<CallSet> callSets = getCallSets(context, conditions);

			long totalCount = context.fetchOne("SELECT FOUND_ROWS()").into(Long.class);
			return new BaseResult<>(new ArrayResult<CallSet>()
				.setData(callSets), currentPage, pageSize, totalCount);
		}
		catch (SQLException e)
		{
			e.printStackTrace();
			throw new ResourceException(Status.SERVER_ERROR_INTERNAL);
		}
	}
}

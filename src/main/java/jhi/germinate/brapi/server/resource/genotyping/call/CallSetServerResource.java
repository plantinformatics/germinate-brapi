package jhi.germinate.brapi.server.resource.genotyping.call;

import jhi.germinate.brapi.server.Brapi;
import jhi.germinate.brapi.server.util.*;
import jhi.germinate.server.Database;
import jhi.germinate.server.database.codegen.tables.records.*;
import jhi.germinate.server.util.*;
import org.jooq.*;
import org.jooq.impl.DSL;
import uk.ac.hutton.ics.brapi.resource.base.*;
import uk.ac.hutton.ics.brapi.resource.genotyping.call.*;
import uk.ac.hutton.ics.brapi.resource.genotyping.variant.Genotype;
import uk.ac.hutton.ics.brapi.server.genotyping.call.BrapiCallSetServerResource;

import javax.annotation.security.PermitAll;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.io.*;
import java.sql.*;
import java.util.*;
import java.util.stream.*;

import static jhi.germinate.server.database.codegen.tables.Datasetmembers.*;
import static jhi.germinate.server.database.codegen.tables.Datasets.*;
import static jhi.germinate.server.database.codegen.tables.Germinatebase.*;
import static jhi.germinate.server.database.codegen.tables.Markers.*;

@Path("brapi/v2/callsets")
@Secured
@PermitAll
public class CallSetServerResource extends CallSetBaseServerResource implements BrapiCallSetServerResource
{
	@GET
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public BaseResult<ArrayResult<CallSet>> getCallsets(@QueryParam("callSetDbId") String callSetDbId,
														@QueryParam("callSetName") String callSetName,
														@QueryParam("variantSetDbId") String variantSetDbId,
														@QueryParam("sampleDbId") String sampleDbId,
														@QueryParam("germplasmDbId") String germplasmDbId)
		throws IOException, SQLException
	{
		try (Connection conn = Database.getConnection())
		{
			DSLContext context = Database.getContext(conn);
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
				.setData(callSets), page, pageSize, totalCount);
		}
	}

	@GET
	@Path("/{callSetDbId}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public BaseResult<CallSet> getCallSetById(@PathParam("callSetDbId") String callSetDbId)
		throws IOException, SQLException
	{
		try (Connection conn = Database.getConnection())
		{
			DSLContext context = Database.getContext(conn);
			List<CallSet> callSets = getCallSets(context, Collections.singletonList(DSL.concat(DATASETMEMBERS.DATASET_ID, DSL.val("-"), GERMINATEBASE.ID).eq(callSetDbId)));

			if (CollectionUtils.isEmpty(callSets))
				return new BaseResult<>(null, page, pageSize, 0);
			else
				return new BaseResult<>(callSets.get(0), page, pageSize, 1);
		}
	}
}

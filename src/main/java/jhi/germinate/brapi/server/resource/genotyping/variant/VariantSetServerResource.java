package jhi.germinate.brapi.server.resource.genotyping.variant;

import jhi.germinate.server.Database;
import jhi.germinate.server.util.*;
import org.jooq.*;
import org.jooq.impl.DSL;
import uk.ac.hutton.ics.brapi.resource.base.*;
import uk.ac.hutton.ics.brapi.resource.genotyping.call.CallSet;
import uk.ac.hutton.ics.brapi.resource.genotyping.variant.*;
import uk.ac.hutton.ics.brapi.server.base.BaseServerResource;
import uk.ac.hutton.ics.brapi.server.genotyping.variant.BrapiVariantSetServerResource;

import javax.annotation.security.PermitAll;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.io.IOException;
import java.sql.*;
import java.util.*;

import static jhi.germinate.server.database.codegen.tables.Datasetmembers.*;
import static jhi.germinate.server.database.codegen.tables.Datasets.*;

@Path("brapi/v2/variantsets")
@Secured
@PermitAll
public class VariantSetServerResource extends BaseServerResource implements BrapiVariantSetServerResource, VariantSetBaseServerResource
{
	@GET
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public BaseResult<ArrayResult<VariantSet>> getVariantSets(@QueryParam("variantSetDbId") String variantSetDbId,
															  @QueryParam("variantDbId") String variantDbId,
															  @QueryParam("callSetDbId") String callSetDbId,
															  @QueryParam("studyDbId") String studyDbId,
															  @QueryParam("studyName") String studyName)
		throws SQLException, IOException
	{
		try (Connection conn = Database.getConnection())
		{
			DSLContext context = Database.getContext(conn);
			List<Condition> conditions = new ArrayList<>();

			if (!StringUtils.isEmpty(variantSetDbId))
				conditions.add(DATASETS.ID.cast(String.class).eq(variantSetDbId));
			if (!StringUtils.isEmpty(variantDbId))
				conditions.add(DSL.exists(DSL.selectOne().from(DATASETMEMBERS).where(DATASETMEMBERS.DATASET_ID.eq(DATASETS.ID)).and(DATASETMEMBERS.DATASETMEMBERTYPE_ID.eq(1)).and(DATASETMEMBERS.FOREIGN_ID.cast(String.class).eq(variantDbId))));
			if (!StringUtils.isEmpty(callSetDbId))
				conditions.add(DSL.exists(DSL.selectOne().from(DATASETMEMBERS).where(DATASETMEMBERS.DATASET_ID.eq(DATASETS.ID)).and(DATASETMEMBERS.DATASETMEMBERTYPE_ID.eq(2)).and(DATASETMEMBERS.FOREIGN_ID.cast(String.class).eq(callSetDbId))));
			if (!StringUtils.isEmpty(studyDbId))
				conditions.add(DATASETS.ID.cast(String.class).eq(studyDbId));
			if (!StringUtils.isEmpty(studyName))
				conditions.add(DATASETS.NAME.cast(String.class).eq(studyName));

			List<VariantSet> result = getVariantSets(context, conditions, req, page, pageSize);

			long totalCount = context.fetchOne("SELECT FOUND_ROWS()").into(Long.class);
			return new BaseResult<>(new ArrayResult<VariantSet>()
				.setData(result), page, pageSize, totalCount);
		}
	}

	@GET
	@Path("/{variantSetDbId}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public BaseResult<VariantSet> getVariantSetById(@PathParam("variantSetDbId") String variantSetDbId)
		throws SQLException, IOException
	{
		try (Connection conn = Database.getConnection())
		{
			DSLContext context = Database.getContext(conn);
			List<VariantSet> results = getVariantSets(context, Collections.singletonList(DATASETS.ID.cast(String.class).eq(variantSetDbId)), req, page, pageSize);
			VariantSet result = CollectionUtils.isEmpty(results) ? null : results.get(0);

			return new BaseResult<>(result, page, pageSize, 1);
		}

	}


	@GET
	@Path("/{variantSetDbId}/callsets")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public BaseResult<ArrayResult<CallSet>> getVariantSetByIdCallSet(@PathParam("variantSetDbId") String variantSetDbId,
																	 @QueryParam("callSetDbId") String callSetDbId,
																	 @QueryParam("callSetName") String callSetName)
		throws SQLException, IOException
	{
		resp.sendError(Response.Status.NOT_IMPLEMENTED.getStatusCode());
		return null;
	}

	@POST
	@Path("/extract")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public BaseResult<VariantSet> postVariantSetExtract(VariantSetExtract extract)
		throws SQLException, IOException
	{
		resp.sendError(Response.Status.NOT_IMPLEMENTED.getStatusCode());
		return null;
	}
}

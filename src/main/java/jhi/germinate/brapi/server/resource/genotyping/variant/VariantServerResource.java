package jhi.germinate.brapi.server.resource.genotyping.variant;

import jakarta.ws.rs.core.*;
import jhi.germinate.brapi.server.Brapi;
import jhi.germinate.brapi.server.util.*;
import jhi.germinate.server.*;
import jhi.germinate.server.database.codegen.tables.records.*;
import jhi.germinate.server.resource.datasets.DatasetTableResource;
import jhi.germinate.server.util.*;
import org.jooq.*;
import org.jooq.impl.DSL;
import uk.ac.hutton.ics.brapi.resource.base.*;
import uk.ac.hutton.ics.brapi.resource.genotyping.call.*;
import uk.ac.hutton.ics.brapi.resource.genotyping.variant.*;
import uk.ac.hutton.ics.brapi.resource.genotyping.variant.Variant;
import uk.ac.hutton.ics.brapi.server.base.BaseServerResource;
import uk.ac.hutton.ics.brapi.server.genotyping.variant.BrapiVariantServerResource;

import jakarta.annotation.security.PermitAll;
import jakarta.ws.rs.*;

import java.io.File;
import java.io.IOException;
import java.sql.*;
import java.util.*;
import java.util.stream.*;

import static jhi.germinate.server.database.codegen.tables.Datasetmembers.*;
import static jhi.germinate.server.database.codegen.tables.Datasets.*;
import static jhi.germinate.server.database.codegen.tables.Germinatebase.*;
import static jhi.germinate.server.database.codegen.tables.Markers.*;

@Path("brapi/v2/variants")
@Secured
@PermitAll
public class VariantServerResource extends BaseServerResource implements BrapiVariantServerResource, VariantBaseServerResource
{
	@Override
	@GET
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public BaseResult<ArrayResult<Variant>> getVariants(@QueryParam("variantDbId") String variantDbId,
														@QueryParam("variantSetDbId") String variantSetDbId,
														@QueryParam("referenceDbId") String referenceDbId,
														@QueryParam("referenceSetDbId") String referenceSetDbId,
														@QueryParam("externalReferenceId") String externalReferenceId,
														@QueryParam("externalReferenceSource") String externalReferenceSource)
		throws SQLException, IOException
	{
		try (Connection conn = Database.getConnection())
		{
			DSLContext context = Database.getContext(conn);

			List<Condition> conditions = new ArrayList<>();

			if (!StringUtils.isEmpty(variantDbId))
				conditions.add(DSL.concat(DATASETMEMBERS.DATASET_ID, DSL.val("-"), MARKERS.ID).eq(variantDbId));
			// TODO: Other parameters

			List<Variant> variants = getVariantsInternal(context, conditions, page, pageSize, req, resp, securityContext);

			if (CollectionUtils.isEmpty(variants))
				return new BaseResult<>(null, page, pageSize, 0);
			else
				return new BaseResult<>(new ArrayResult<Variant>().setData(variants), page, pageSize, 1);
		}
	}

	@Override
	@GET
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/{variantDbId}")
	public BaseResult<Variant> getVariantById(@PathParam("variantDbId") String variantDbId)
		throws IOException, SQLException
	{
		try (Connection conn = Database.getConnection())
		{
			DSLContext context = Database.getContext(conn);
			List<Variant> variants = getVariantsInternal(context, Collections.singletonList(DSL.concat(DATASETMEMBERS.DATASET_ID, DSL.val("-"), MARKERS.ID).eq(variantDbId)), page, pageSize, req, resp, securityContext);

			if (CollectionUtils.isEmpty(variants))
				return new BaseResult<>(null, page, pageSize, 0);
			else
				return new BaseResult<>(variants.get(0), page, pageSize, 1);
		}
	}

	@Override
	public BaseResult<CallResult<Call>> getVariantByIdCalls(@PathParam("variantDbId") String variantDbId,
															@QueryParam("expandHomozygotes") Boolean expandHomozygotes,
															@QueryParam("unknownString") String unknownString,
															@QueryParam("sepPhased") String sepPhased,
															@QueryParam("sepUnphased") String sepUnphased)
		throws SQLException, IOException
	{
		if (StringUtils.isEmpty(variantDbId) || !variantDbId.contains("-"))
		{
			resp.sendError(Response.Status.NOT_FOUND.getStatusCode());
			return null;
		}

		AuthenticationFilter.UserDetails userDetails = (AuthenticationFilter.UserDetails) securityContext.getUserPrincipal();
		List<Integer> datasetIds = DatasetTableResource.getDatasetIdsForUser(req, userDetails, "genotype");

		try (Connection conn = Database.getConnection())
		{
			DSLContext context = Database.getContext(conn);
			GenotypeEncodingParams params = new GenotypeEncodingParams();
			params.setUnknownString(unknownString);
			params.setSepPhased(sepPhased);
			params.setSepUnphased(sepUnphased);
			params.setUnknownString(unknownString);
			try
			{
				params.setCollapse(!expandHomozygotes);
			}
			catch (Exception e)
			{
			}

			String[] parts = variantDbId.split("-");

			DatasetsRecord dataset = context.selectFrom(DATASETS)
											.where(DATASETS.ID.in(datasetIds))
											.and(DATASETS.IS_EXTERNAL.eq(false))
											.and(DATASETS.ID.cast(String.class).eq(parts[0]))
											.fetchAny();
			MarkersRecord marker = context.selectFrom(MARKERS)
										  .where(MARKERS.ID.cast(String.class).eq(parts[1]))
										  .fetchAny();

			if (dataset == null || StringUtils.isEmpty(dataset.getSourceFile()) || marker == null)
			{
				resp.sendError(Response.Status.BAD_REQUEST.getStatusCode());
				return null;
			}

			Hdf5DataExtractor extractor = new Hdf5DataExtractor(new File(Brapi.BRAPI.hdf5BaseFolder, dataset.getSourceFile()));
			List<String> alleles = extractor.getAllelesForMarker(marker.getMarkerName(), params);
			List<String> germplasmNames = extractor.getLines();
			Map<String, Integer> germplasmNamesToIds = context.selectFrom(GERMINATEBASE)
															  .where(GERMINATEBASE.NAME.in(germplasmNames))
															  .fetchMap(GERMINATEBASE.NAME, GERMINATEBASE.ID);

			List<Call> calls = IntStream.range(0, alleles.size())
										.skip(pageSize * page)
										.limit(pageSize)
										.mapToObj(i -> new Call()
											.setCallSetDbId(dataset.getId() + "-" + germplasmNamesToIds.get(germplasmNames.get(i)))
											.setCallSetName(germplasmNames.get(i))
											.setGenotypeValue(alleles.get(i))
											.setVariantDbId(variantDbId)
											.setVariantName(marker.getMarkerName()))
										.collect(Collectors.toList());

			CallResult<Call> callResult = new CallResult<Call>()
				.setData(calls)
				.setExpandHomozygotes(!params.isCollapse())
				.setSepPhased(params.getSepPhased())
				.setSepUnphased(params.getSepUnphased())
				.setUnknownString(params.getUnknownString());
			return new BaseResult<>(callResult, page, pageSize, alleles.size());
		}
	}
}

package jhi.germinate.brapi.server.resource.genotyping.variant;

import jhi.germinate.brapi.server.Brapi;
import jhi.germinate.brapi.server.util.*;
import jhi.germinate.server.Database;
import jhi.germinate.server.database.codegen.tables.records.*;
import jhi.germinate.server.util.*;
import org.jooq.*;
import org.jooq.impl.DSL;
import uk.ac.hutton.ics.brapi.resource.base.*;
import uk.ac.hutton.ics.brapi.resource.genotyping.call.*;
import uk.ac.hutton.ics.brapi.resource.genotyping.variant.Variant;
import uk.ac.hutton.ics.brapi.resource.genotyping.variant.*;
import uk.ac.hutton.ics.brapi.server.base.TokenBaseServerResource;
import uk.ac.hutton.ics.brapi.server.genotyping.variant.BrapiVariantTokenServerResource;

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
import static jhi.germinate.server.database.codegen.tables.ViewTableMarkers.*;

@Path("brapi/v2/variants")
@Secured
@PermitAll
public class VariantTokenServerResource extends TokenBaseServerResource implements BrapiVariantTokenServerResource, VariantBaseServerResource
{
	@GET
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public TokenBaseResult<ArrayResult<Variant>> getAllVariants(@QueryParam("variantDbId") String variantDbId,
																@QueryParam("variantSetDbId") String variantSetDbId)
		throws IOException, SQLException
	{
		try (Connection conn = Database.getConnection())
		{
			DSLContext context = Database.getContext(conn);
			List<Condition> conditions = new ArrayList<>();

			if (!StringUtils.isEmpty(variantDbId))
				conditions.add(DSL.concat(DATASETMEMBERS.DATASET_ID, DSL.val("-"), VIEW_TABLE_MARKERS.MARKER_ID).eq(variantDbId));
			if (!StringUtils.isEmpty(variantSetDbId))
				conditions.add(DATASETMEMBERS.DATASET_ID.cast(String.class).eq(variantSetDbId));

			List<Variant> variants = getVariantsInternal(context, conditions, page, pageSize);
			long totalCount = context.fetchOne("SELECT FOUND_ROWS()").into(Long.class);
			return new TokenBaseResult<>(new ArrayResult<Variant>()
				.setData(variants), page, pageSize, totalCount);
		}
	}

	@GET
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/{variantDbId}/calls")
	public TokenBaseResult<CallResult<Call>> getVariantByIdCalls(@PathParam("variantDbId") String variantDbId,
																 @QueryParam("expandHomozygotes") String expandHomozygotes,
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
				params.setCollapse(!Boolean.parseBoolean(expandHomozygotes));
			}
			catch (Exception e)
			{
			}

			String[] parts = variantDbId.split("-");

			DatasetsRecord dataset = context.selectFrom(DATASETS)
											.where(DATASETS.DATASET_STATE_ID.eq(1))
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
											.setGenotype(new Genotype()
												.setValues(Collections.singletonList(alleles.get(i))))
											.setVariantDbId(variantDbId)
											.setVariantName(marker.getMarkerName()))
										.collect(Collectors.toList());

			CallResult<Call> callResult = new CallResult<Call>()
				.setData(calls)
				.setExpandHomozygotes(!params.isCollapse())
				.setSepPhased(params.getSepPhased())
				.setSepUnphased(params.getSepUnphased())
				.setUnknownString(params.getUnknownString());
			return new TokenBaseResult<>(callResult, page, pageSize, alleles.size());
		}
	}
}

package jhi.germinate.brapi.server.resource.genotyping.call;

import jhi.germinate.brapi.server.Brapi;
import jhi.germinate.brapi.server.util.*;
import jhi.germinate.server.Database;
import jhi.germinate.server.database.codegen.tables.records.*;
import jhi.germinate.server.util.*;
import org.jooq.DSLContext;
import uk.ac.hutton.ics.brapi.resource.base.TokenBaseResult;
import uk.ac.hutton.ics.brapi.resource.genotyping.call.*;
import uk.ac.hutton.ics.brapi.resource.genotyping.variant.Genotype;
import uk.ac.hutton.ics.brapi.server.base.TokenBaseServerResource;
import uk.ac.hutton.ics.brapi.server.genotyping.call.BrapiCallSetTokenServerResource;

import javax.annotation.security.PermitAll;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.io.*;
import java.sql.*;
import java.util.*;
import java.util.stream.*;

import static jhi.germinate.server.database.codegen.tables.Datasets.*;
import static jhi.germinate.server.database.codegen.tables.Germinatebase.*;
import static jhi.germinate.server.database.codegen.tables.Markers.*;

@Path("brapi/v2/callsets")
@Secured
@PermitAll
public class CallSetTokenServerResource extends TokenBaseServerResource implements BrapiCallSetTokenServerResource
{
	@GET
	@Path("/{callSetDbId}/calls")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public TokenBaseResult<CallResult<Call>> getCallSetByIdCalls(@PathParam("callSetDbId") String callSetDbId,
																 @QueryParam("expandHomozygotes") String expandHomozygotes,
																 @QueryParam("unknownString") String unknownString,
																 @QueryParam("sepPhased") String sepPhased,
																 @QueryParam("sepUnphased") String sepUnphased)
		throws IOException, SQLException
	{
		if (StringUtils.isEmpty(callSetDbId) || !callSetDbId.contains("-"))
		{
			resp.sendError(Response.Status.NOT_FOUND.getStatusCode());
			return null;
		}

		try (Connection conn = Database.getConnection())
		{
			DSLContext context = Database.getContext(conn);
			String[] parts = callSetDbId.split("-");

			DatasetsRecord dataset = context.selectFrom(DATASETS)
											.where(DATASETS.DATASET_STATE_ID.eq(1))
											.and(DATASETS.IS_EXTERNAL.eq(false))
											.and(DATASETS.ID.cast(String.class).eq(parts[0]))
											.fetchAny();
			GerminatebaseRecord germplasm = context.selectFrom(GERMINATEBASE)
												   .where(GERMINATEBASE.ID.cast(String.class).eq(parts[1]))
												   .fetchAny();

			if (dataset == null || StringUtils.isEmpty(dataset.getSourceFile()) || germplasm == null)
			{
				resp.sendError(Response.Status.BAD_REQUEST.getStatusCode());
				return null;
			}

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

			Hdf5DataExtractor extractor = new Hdf5DataExtractor(new File(Brapi.BRAPI.hdf5BaseFolder, dataset.getSourceFile()));
			List<String> alleles = extractor.getAllelesForLine(germplasm.getName(), params);
			List<String> markerNames = extractor.getMarkers();
			Map<String, Integer> markerNamesToIds = context.selectFrom(MARKERS)
														   .where(MARKERS.MARKER_NAME.in(markerNames))
														   .fetchMap(MARKERS.MARKER_NAME, MARKERS.ID);

			List<Call> calls = IntStream.range(0, alleles.size())
										.skip(pageSize * page)
										.limit(pageSize)
										.mapToObj(i -> new Call()
											.setCallSetDbId(callSetDbId)
											.setCallSetName(germplasm.getName())
											.setGenotype(new Genotype()
												.setValues(Collections.singletonList(alleles.get(i))))
											.setVariantDbId(dataset.getId() + "-" + markerNamesToIds.get(markerNames.get(i)))
											.setVariantName(markerNames.get(i)))
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

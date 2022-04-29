package jhi.germinate.brapi.server.resource.genotyping.variant;

import jhi.germinate.brapi.server.Brapi;
import jhi.germinate.brapi.server.util.*;
import jhi.germinate.server.*;
import jhi.germinate.server.database.codegen.tables.records.DatasetsRecord;
import jhi.germinate.server.resource.datasets.DatasetTableResource;
import jhi.germinate.server.util.*;
import org.jooq.DSLContext;
import uk.ac.hutton.ics.brapi.resource.base.*;
import uk.ac.hutton.ics.brapi.resource.genotyping.call.*;
import uk.ac.hutton.ics.brapi.resource.genotyping.variant.Variant;
import uk.ac.hutton.ics.brapi.resource.genotyping.variant.*;
import uk.ac.hutton.ics.brapi.server.base.TokenBaseServerResource;
import uk.ac.hutton.ics.brapi.server.genotyping.variant.BrapiVariantSetTokenServerResource;

import jakarta.annotation.security.PermitAll;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import java.io.*;
import java.sql.*;
import java.util.*;
import java.util.stream.*;

import static jhi.germinate.server.database.codegen.tables.Datasets.*;
import static jhi.germinate.server.database.codegen.tables.Germinatebase.*;
import static jhi.germinate.server.database.codegen.tables.Markers.*;

@Path("brapi/v2/variantsets")
@Secured
@PermitAll
public class VariantSetTokenServerResource extends TokenBaseServerResource implements BrapiVariantSetTokenServerResource, VariantSetBaseServerResource
{
	@GET
	@Path("/{variantSetDbId}/calls")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public TokenBaseResult<CallResult<Call>> getVariantSetByIdCalls(@PathParam("variantSetDbId") String variantSetDbId,
																	@QueryParam("expandHomozygotes") String expandHomozygotes,
																	@QueryParam("unknownString") String unknownString,
																	@QueryParam("sepPhased") String sepPhased,
																	@QueryParam("sepUnphased") String sepUnphased)
		throws SQLException, IOException
	{
		AuthenticationFilter.UserDetails userDetails = (AuthenticationFilter.UserDetails) securityContext.getUserPrincipal();
		List<Integer> datasetIds = DatasetTableResource.getDatasetIdsForUser(req, resp, userDetails, "genotype");

		if (StringUtils.isEmpty(variantSetDbId))
		{
			resp.sendError(Response.Status.BAD_REQUEST.getStatusCode());
			return null;
		}

		try (Connection conn = Database.getConnection())
		{
			DSLContext context = Database.getContext(conn);
			String[] parts = variantSetDbId.split("-");

			DatasetsRecord dataset = context.selectFrom(DATASETS)
											.where(DATASETS.ID.in(datasetIds))
											.and(DATASETS.IS_EXTERNAL.eq(false))
											.and(DATASETS.ID.cast(String.class).eq(parts[0]))
											.fetchAny();

			if (dataset == null || StringUtils.isEmpty(dataset.getSourceFile()))
			{
				resp.sendError(Response.Status.BAD_REQUEST.getStatusCode());
				return null;
			}

			Hdf5DataExtractor extractor = new Hdf5DataExtractor(new File(Brapi.BRAPI.hdf5BaseFolder, dataset.getSourceFile()));
			int markerCount = extractor.getMarkerCount();
			int germplasmCount = extractor.getLineCount();

			// Determine the coordinates in the matrix where we start and where we end (based on a reading-order like reading text, i.e. top left to bottom right per row)
			int gStart = (page * pageSize) / markerCount;
			int mStart = (page * pageSize) % markerCount;
			int gEnd = gStart + (mStart + pageSize) / markerCount;
			int mEnd = (mStart + pageSize) % markerCount;

			if (gStart > germplasmCount - 1)
			{
				gStart = germplasmCount - 1;
			}
			if (gEnd > germplasmCount - 1)
			{
				gEnd = germplasmCount - 1;
				mEnd = markerCount;
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

			List<String> alleles = new ArrayList<>();

			int g = gStart;
			while (true)
			{
				String germplasm = extractor.getLine(g);
				List<String> a = extractor.getAllelesForLine(germplasm, params);

				if (g < gEnd)
				{
					int start = g == gStart ? mStart : 0;
					// If we're still in a row further up, add everything
					alleles.addAll(a.subList(start, markerCount));
				}
				else
				{
					int start = gStart < gEnd ? 0 : mStart;
					// Else, add everything up to the end marker, then break
					alleles.addAll(a.subList(start, mEnd));
					break;
				}

				g++;
			}

			// Get germplasm in the HDF5 file and their ids from the database
			List<String> germplasmNames = extractor.getLines();
			Map<String, Integer> germplasmNamesToIds = context.selectFrom(GERMINATEBASE)
															  .where(GERMINATEBASE.NAME.in(germplasmNames))
															  .fetchMap(GERMINATEBASE.NAME, GERMINATEBASE.ID);
			// Get markers in the HDF5 file and their ids from the database
			List<String> markerNames = extractor.getMarkers();
			Map<String, Integer> markerNamesToIds = context.selectFrom(MARKERS)
														   .where(MARKERS.MARKER_NAME.in(markerNames))
														   .fetchMap(MARKERS.MARKER_NAME, MARKERS.ID);

			final int gStartFinal = gStart;
			List<Call> calls = IntStream.range(0, alleles.size())
										.mapToObj(i -> {
											int germplasmIndex = gStartFinal + (mStart + i) / markerCount;
											int markerIndex = (mStart + i) % markerCount;

											return new Call()
												.setCallSetDbId(dataset.getId() + "-" + germplasmNamesToIds.get(germplasmNames.get(germplasmIndex)))
												.setCallSetName(germplasmNames.get(germplasmIndex))
												.setGenotype(new Genotype()
													.setValues(Collections.singletonList(alleles.get(i))))
												.setVariantDbId(dataset.getId() + "-" + markerNamesToIds.get(markerNames.get(markerIndex)))
												.setVariantName(markerNames.get(markerIndex));
										})
										.collect(Collectors.toList());

			CallResult<Call> callResult = new CallResult<Call>()
				.setData(calls)
				.setExpandHomozygotes(!params.isCollapse())
				.setSepPhased(params.getSepPhased())
				.setSepUnphased(params.getSepUnphased())
				.setUnknownString(params.getUnknownString());
			return new TokenBaseResult<>(callResult, page, pageSize, markerCount * germplasmCount);
		}
	}

	@GET
	@Path("/{variantSetDbId}/variants")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public TokenBaseResult<BaseResult<Variant>> getVariantSetByIdVariants(@PathParam("variantSetDbId") String variantSetDbId,
																		  @QueryParam("variantDbId") String variantDbId)
		throws IOException, SQLException
	{
		resp.sendError(Response.Status.NOT_IMPLEMENTED.getStatusCode());
		return null;
	}
}

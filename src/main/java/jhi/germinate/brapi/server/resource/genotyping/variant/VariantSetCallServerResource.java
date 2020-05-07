package jhi.germinate.brapi.server.resource.genotyping.variant;

import org.jooq.DSLContext;
import org.restlet.data.Status;
import org.restlet.resource.ResourceException;

import java.io.File;
import java.sql.*;
import java.util.*;
import java.util.stream.*;

import jhi.germinate.brapi.resource.base.TokenBaseResult;
import jhi.germinate.brapi.resource.call.*;
import jhi.germinate.brapi.resource.variant.Genotype;
import jhi.germinate.brapi.server.Brapi;
import jhi.germinate.brapi.server.resource.TokenBaseServerResource;
import jhi.germinate.brapi.server.util.*;
import jhi.germinate.server.Database;
import jhi.germinate.server.database.tables.records.DatasetsRecord;
import jhi.germinate.server.util.StringUtils;

import static jhi.germinate.server.database.tables.Datasets.*;
import static jhi.germinate.server.database.tables.Germinatebase.*;
import static jhi.germinate.server.database.tables.Markers.*;

/**
 * @author Sebastian Raubach
 */
public class VariantSetCallServerResource extends TokenBaseServerResource<CallResult<Call>>
{
	private static final String                 PARAM_EXPAND_HOMOZYGOTES = "expandHomozygotes";
	private static final String                 PARAM_UNKNOWN_STRING     = "unknownString";
	private static final String                 PARAM_SEP_PHASED         = "sepPhased";
	private static final String                 PARAM_SEP_UNPHASED       = "sepUnphased";
	private              GenotypeEncodingParams params                   = new GenotypeEncodingParams();

	private String variantSetDbId;

	@Override
	public void doInit()
	{
		super.doInit();

		try
		{
			String expand = getQueryValue(PARAM_EXPAND_HOMOZYGOTES);

			if (!StringUtils.isEmpty(expand))
				params.setCollapse(!Boolean.parseBoolean(expand));
		}
		catch (Exception e)
		{
		}
		String unknownString = getQueryValue(PARAM_UNKNOWN_STRING);
		if (unknownString != null)
			params.setUnknownString(unknownString);
		String sepPhased = getQueryValue(PARAM_SEP_PHASED);
		if (sepPhased != null)
			params.setSepPhased(sepPhased);
		String sepUnphased = getQueryValue(PARAM_SEP_UNPHASED);
		if (sepUnphased != null)
			params.setSepUnphased(sepUnphased);

		try
		{
			this.variantSetDbId = getRequestAttributes().get("variantSetDbId").toString();
		}
		catch (Exception e)
		{
		}
	}

	@Override
	public TokenBaseResult<CallResult<Call>> getJson()
	{
		if (StringUtils.isEmpty(variantSetDbId))
			throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST);

		try (Connection conn = Database.getConnection();
			 DSLContext context = Database.getContext(conn))
		{
			String[] parts = variantSetDbId.split("-");

			DatasetsRecord dataset = context.selectFrom(DATASETS)
											.where(DATASETS.DATASET_STATE_ID.eq(1))
											.and(DATASETS.ID.cast(String.class).eq(parts[0]))
											.fetchAny();

			if (dataset == null || StringUtils.isEmpty(dataset.getSourceFile()))
				throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST);

			Hdf5DataExtractor extractor = new Hdf5DataExtractor(new File(Brapi.BRAPI.hdf5BaseFolder, dataset.getSourceFile()));
			int markerCount = extractor.getMarkerCount();
			int germplasmCount = extractor.getLineCount();

			// Determine the coordinates in the matrix where we start and where we end (based on a reading-order like reading text, i.e. top left to bottom right per row)
			int gStart = (currentPage * pageSize) / markerCount;
			int mStart = (currentPage * pageSize) % markerCount;
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
			return new TokenBaseResult<>(callResult, currentPage, pageSize, markerCount * germplasmCount);
		}
		catch (SQLException e)
		{
			e.printStackTrace();
			throw new ResourceException(Status.SERVER_ERROR_INTERNAL);
		}
	}
}

package jhi.germinate.brapi.server.resource.genotyping.call;

import org.jooq.DSLContext;
import org.restlet.data.Status;
import org.restlet.resource.*;

import java.io.File;
import java.sql.*;
import java.util.*;
import java.util.stream.*;

import jhi.germinate.brapi.server.Brapi;
import jhi.germinate.brapi.server.util.*;
import jhi.germinate.server.Database;
import jhi.germinate.server.database.tables.records.*;
import jhi.germinate.server.util.StringUtils;
import uk.ac.hutton.ics.brapi.resource.base.TokenBaseResult;
import uk.ac.hutton.ics.brapi.resource.genotyping.call.*;
import uk.ac.hutton.ics.brapi.resource.genotyping.variant.Genotype;
import uk.ac.hutton.ics.brapi.server.base.TokenBaseServerResource;
import uk.ac.hutton.ics.brapi.server.genotyping.call.BrapiCallSetIndividualCallServerResource;

import static jhi.germinate.server.database.tables.Datasets.*;
import static jhi.germinate.server.database.tables.Germinatebase.*;
import static jhi.germinate.server.database.tables.Markers.*;

/**
 * @author Sebastian Raubach
 */
public class CallSetCallServerResource extends TokenBaseServerResource implements BrapiCallSetIndividualCallServerResource
{
	private static final String PARAM_EXPAND_HOMOZYGOTES = "expandHomozygotes";
	private static final String PARAM_UNKNOWN_STRING     = "unknownString";
	private static final String PARAM_SEP_PHASED         = "sepPhased";
	private static final String PARAM_SEP_UNPHASED       = "sepUnphased";

	private String callSetDbId;

	private GenotypeEncodingParams params = new GenotypeEncodingParams();

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
			this.callSetDbId = getRequestAttributes().get("callSetDbId").toString();
		}
		catch (Exception e)
		{
		}
	}

	@Get
	public TokenBaseResult<CallResult<Call>> getCallSetCalls()
	{
		if (StringUtils.isEmpty(callSetDbId) || !callSetDbId.contains("-"))
			throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND);

		try (Connection conn = Database.getConnection();
			 DSLContext context = Database.getContext(conn))
		{
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
				throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST);

			Hdf5DataExtractor extractor = new Hdf5DataExtractor(new File(Brapi.BRAPI.hdf5BaseFolder, dataset.getSourceFile()));
			List<String> alleles = extractor.getAllelesForLine(germplasm.getName(), params);
			List<String> markerNames = extractor.getMarkers();
			Map<String, Integer> markerNamesToIds = context.selectFrom(MARKERS)
														   .where(MARKERS.MARKER_NAME.in(markerNames))
														   .fetchMap(MARKERS.MARKER_NAME, MARKERS.ID);

			List<Call> calls = IntStream.range(0, alleles.size())
										.skip(pageSize * currentPage)
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
			return new TokenBaseResult<>(callResult, currentPage, pageSize, alleles.size());
		}
		catch (SQLException e)
		{
			e.printStackTrace();
			throw new ResourceException(Status.SERVER_ERROR_INTERNAL);
		}
	}
}

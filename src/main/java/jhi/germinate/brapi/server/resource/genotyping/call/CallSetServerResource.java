package jhi.germinate.brapi.server.resource.genotyping.call;

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
import uk.ac.hutton.ics.brapi.resource.genotyping.variant.Genotype;
import uk.ac.hutton.ics.brapi.server.genotyping.call.BrapiCallSetServerResource;

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
import static jhi.germinate.server.database.codegen.tables.ViewTableMarkers.*;
import static jhi.germinate.server.database.codegen.tables.Mapdefinitions.*;
import static jhi.germinate.server.database.codegen.tables.Maps.*;

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
														@QueryParam("germplasmDbId") String germplasmDbId,
														@QueryParam("externalReferenceId") String externalReferenceId,
														@QueryParam("externalReferenceSource") String externalReferenceSource)
		throws IOException, SQLException
	{
		AuthenticationFilter.UserDetails userDetails = (AuthenticationFilter.UserDetails) securityContext.getUserPrincipal();
		List<Integer> datasets = DatasetTableResource.getDatasetIdsForUser(req, resp, userDetails, "genotype");

		try (Connection conn = Database.getConnection())
		{
			DSLContext context = Database.getContext(conn);
			List<Condition> conditions = new ArrayList<>();

			conditions.add(DATASETMEMBERS.DATASET_ID.in(datasets));

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

	@Override
	@GET
	@Path("/{callSetDbId}/calls")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public BaseResult<CallResult<Call>> getCallSetByIdCalls(@PathParam("callSetDbId") String callSetDbId,
															@QueryParam("expandHomozygotes") Boolean expandHomozygotes,
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

		AuthenticationFilter.UserDetails userDetails = (AuthenticationFilter.UserDetails) securityContext.getUserPrincipal();
		List<Integer> datasets = DatasetTableResource.getDatasetIdsForUser(req, resp, userDetails, "genotype");

		try (Connection conn = Database.getConnection())
		{
			DSLContext context = Database.getContext(conn);
			String[] parts = callSetDbId.split("-");

			DatasetsRecord dataset = context.selectFrom(DATASETS)
											.where(DATASETS.ID.in(datasets))
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
			params.setUnknownString(unknownString != null && !unknownString.isEmpty() ? unknownString : "N");
			params.setSepPhased(sepPhased != null && !sepPhased.isEmpty() ? sepPhased : "|");
			params.setSepUnphased(sepUnphased != null && !sepUnphased.isEmpty() ? sepUnphased : "/");
			params.setUnknownString(unknownString != null && !unknownString.isEmpty() ? unknownString : "N");
			
			try
			{
				params.setCollapse(!expandHomozygotes);
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
											.setGenotypeValue(alleles.get(i))
											.setVariantDbId(dataset.getId() + "-" + markerNamesToIds.get(markerNames.get(i)))
											.setVariantName(markerNames.get(i)))
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

	@GET
	@Path("/{callSetDbId}/calls/mapid/{mapid}/position/{positionStart}/{positionEnd}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public BaseResult<CallResult<Call>> getGenotypesByPositionRange(@PathParam("callSetDbId") String callSetDbId,
																  @PathParam("positionStart") Double positionStart,
																  @PathParam("positionEnd") Double positionEnd,
																  @PathParam("mapid") Integer mapid,
																  @QueryParam("expandHomozygotes") Boolean expandHomozygotes,
																  @QueryParam("unknownString") String unknownString,
																  @QueryParam("sepPhased") String sepPhased,
															      @QueryParam("sepUnphased") String sepUnphased)
		throws IOException, SQLException, Exception
	{
		if (StringUtils.isEmpty(callSetDbId) || !callSetDbId.contains("-"))
		{
			resp.sendError(Response.Status.NOT_FOUND.getStatusCode());
			return null;
		}

		if (positionEnd < positionStart) {
			throw new Exception("positionEndd must be bigger than positionStart");
		}

		AuthenticationFilter.UserDetails userDetails = (AuthenticationFilter.UserDetails) securityContext.getUserPrincipal();
		List<Integer> datasets = DatasetTableResource.getDatasetIdsForUser(req, resp, userDetails, "genotype");

		try (Connection conn = Database.getConnection())
		{
			DSLContext context = Database.getContext(conn);
			String[] parts = callSetDbId.split("-");

			DatasetsRecord dataset = context.selectFrom(DATASETS)
											.where(DATASETS.ID.in(datasets))
											.and(DATASETS.IS_EXTERNAL.eq(false))
											.and(DATASETS.ID.cast(String.class).eq(parts[0]))
											.fetchAny();
			GerminatebaseRecord germplasm = context.selectFrom(GERMINATEBASE)
												   .where(GERMINATEBASE.ID.cast(String.class).eq(parts[1]))
												   .fetchAny();
			
			
			List<Integer> mapIds = context.select(MAPS.ID)
       									  .from(MAPS)
										  .fetch()
										  .into(Integer.class);
			
			if (!mapIds.contains(mapid)) {
				throw new IllegalArgumentException("mapid not found in the database");
			}

			List<Double> positions = context.select(MAPDEFINITIONS.DEFINITION_START)
											.from(MAPS)
											.leftJoin(MAPDEFINITIONS).on(MAPDEFINITIONS.MAP_ID.eq(MAPS.ID))
											.where(MAPS.ID.eq(mapid))
											.and(MAPS.VISIBILITY.eq(true).or(MAPS.USER_ID.eq(userDetails.getId())))
											.fetch()
											.into(Double.class);

			if (dataset == null || StringUtils.isEmpty(dataset.getSourceFile()) || germplasm == null)
			{
				resp.sendError(Response.Status.BAD_REQUEST.getStatusCode());
				return null;
			}

			GenotypeEncodingParams params = new GenotypeEncodingParams();
			params.setUnknownString(unknownString != null && !unknownString.isEmpty() ? unknownString : "N");
			params.setSepPhased(sepPhased != null && !sepPhased.isEmpty() ? sepPhased : "|");
			params.setSepUnphased(sepUnphased != null && !sepUnphased.isEmpty() ? sepUnphased : "/");
			params.setUnknownString(unknownString != null && !unknownString.isEmpty() ? unknownString : "N");
			
			try
			{
				params.setCollapse(!expandHomozygotes);
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

			List<String> filteredAlleles = new ArrayList<>();
			List<String> filteredMarkerNames = new ArrayList<>();
			List<Double> filteredPositions = new ArrayList<>();

			for (int i = 0; i < alleles.size(); i++) {
				Double position = positions.get(i);
				if (position >= positionStart && position <= positionEnd) {
					filteredAlleles.add(alleles.get(i));
					filteredMarkerNames.add(markerNames.get(i));
					filteredPositions.add(position);
				}
			}

			List<Call> calls = IntStream.range(0, filteredAlleles.size())
										.skip(pageSize * page)
										.limit(pageSize)
										.mapToObj(i -> new Call()
											.setCallSetDbId(callSetDbId)
											.setCallSetName(germplasm.getName())
											.setGenotypeValue(alleles.get(i))
											.setVariantName(filteredMarkerNames.get(i) + "-" + filteredPositions.get(i))
											)
										.collect(Collectors.toList());

			CallResult<Call> callResult = new CallResult<Call>()
				.setData(calls)
				.setExpandHomozygotes(!params.isCollapse())
				.setSepPhased(params.getSepPhased())
				.setSepUnphased(params.getSepUnphased())
				.setUnknownString(params.getUnknownString());
			return new BaseResult<>(callResult, page, pageSize, filteredAlleles.size());
		}
	}

	@GET
	@Path("/dataset/{datasetID}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public BaseResult<ArrayResult<CallSet>> getGermplasmByDatasetID(@PathParam("datasetID") String DatasetID)
													
		throws IOException, SQLException
	{
		AuthenticationFilter.UserDetails userDetails = (AuthenticationFilter.UserDetails) securityContext.getUserPrincipal();
		List<Integer> datasets = DatasetTableResource.getDatasetIdsForUser(req, resp, userDetails, "genotype");

		try (Connection conn = Database.getConnection())
		{
			DSLContext context = Database.getContext(conn);
			List<Condition> conditions = new ArrayList<>();

			conditions.add(DATASETMEMBERS.DATASET_ID.in(datasets));

			if (!StringUtils.isEmpty(DatasetID))
				conditions.add(DATASETMEMBERS.DATASET_ID.cast(String.class).eq(DatasetID));

			List<CallSet> callSets = getCallSets(context, conditions);

			long totalCount = context.fetchOne("SELECT FOUND_ROWS()").into(Long.class);
			return new BaseResult<>(new ArrayResult<CallSet>()
				.setData(callSets), page, pageSize, totalCount);
		}
	}

	@GET
	@Path("/{callSetDbId}/calls/markerid/{markerIDStart}/{markerIDEnd}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public BaseResult<CallResult<Call>> getGenotypesByMarkerIdRange(@PathParam("callSetDbId") String callSetDbId,
																  @PathParam("markerIDStart") Integer markerIDStart,
																  @PathParam("markerIDEnd") Integer markerIDEnd,
																  @QueryParam("expandHomozygotes") Boolean expandHomozygotes,
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

		AuthenticationFilter.UserDetails userDetails = (AuthenticationFilter.UserDetails) securityContext.getUserPrincipal();
		List<Integer> datasets = DatasetTableResource.getDatasetIdsForUser(req, resp, userDetails, "genotype");

		try (Connection conn = Database.getConnection())
		{
			DSLContext context = Database.getContext(conn);
			String[] parts = callSetDbId.split("-");

			DatasetsRecord dataset = context.selectFrom(DATASETS)
											.where(DATASETS.ID.in(datasets))
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
			params.setUnknownString(unknownString != null && !unknownString.isEmpty() ? unknownString : "N");
			params.setSepPhased(sepPhased != null && !sepPhased.isEmpty() ? sepPhased : "|");
			params.setSepUnphased(sepUnphased != null && !sepUnphased.isEmpty() ? sepUnphased : "/");
			params.setUnknownString(unknownString != null && !unknownString.isEmpty() ? unknownString : "N");
			
			try
			{
				params.setCollapse(!expandHomozygotes);
			}
			catch (Exception e)
			{
			}

			Hdf5DataExtractor extractor = new Hdf5DataExtractor(new File(Brapi.BRAPI.hdf5BaseFolder, dataset.getSourceFile()));
			List<String> alleles = extractor.getAllelesForLine(germplasm.getName(), params);
			List<String> markerNames = extractor.getMarkers();
			List<String> markerIDs = extractor.getMarkersIds();
			

			List<String> filteredAlleles = new ArrayList<>();
			List<String> filteredMarkerIds = new ArrayList<>();
			List<String> filteredMarkerNames = new ArrayList<>();


			for (int i = 0; i < alleles.size(); i++) {
				int markerID = Integer.parseInt(markerIDs.get(i));
				if (markerID >= markerIDStart && markerID <= markerIDEnd) {
					filteredAlleles.add(alleles.get(i));
					filteredMarkerIds.add(markerIDs.get(i));
					filteredMarkerNames.add(markerNames.get(i));
				}
			}

			List<Call> calls = IntStream.range(0, filteredAlleles.size())
										.skip(pageSize * page)
										.limit(pageSize)
										.mapToObj(i -> new Call()
											.setCallSetDbId(callSetDbId)
											.setCallSetName(germplasm.getName())
											.setGenotypeValue(alleles.get(i))
											.setVariantDbId(dataset.getId() + "-" + filteredMarkerIds.get(i))
											.setVariantName(filteredMarkerNames.get(i))
											)
										.collect(Collectors.toList());

			CallResult<Call> callResult = new CallResult<Call>()
				.setData(calls)
				.setExpandHomozygotes(!params.isCollapse())
				.setSepPhased(params.getSepPhased())
				.setSepUnphased(params.getSepUnphased())
				.setUnknownString(params.getUnknownString());
			return new BaseResult<>(callResult, page, pageSize, filteredAlleles.size());
		}
	}


	@GET
	@Path("/{callSetDbId}/calls/mapid/{mapid}/chromosome/{chromosome}/position/{positionStart}/{positionEnd}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public BaseResult<CallResult<Call>> getGenotypesByPositionAndChromosome(@PathParam("callSetDbId") String callSetDbId,
																  @PathParam("positionStart") Double positionStart,
																  @PathParam("positionEnd") Double positionEnd,
																  @PathParam("chromosome") Integer chromosome,
																  @PathParam("mapid") Integer mapid,
																  @QueryParam("expandHomozygotes") Boolean expandHomozygotes,
																  @QueryParam("unknownString") String unknownString,
																  @QueryParam("sepPhased") String sepPhased,
															      @QueryParam("sepUnphased") String sepUnphased)
		throws IOException, SQLException, Exception
	{
		if (StringUtils.isEmpty(callSetDbId) || !callSetDbId.contains("-"))
		{
			resp.sendError(Response.Status.NOT_FOUND.getStatusCode());
			return null;
		}

		if (positionEnd < positionStart) {
			throw new Exception("positionEndd must be bigger than positionStart");
		}

		AuthenticationFilter.UserDetails userDetails = (AuthenticationFilter.UserDetails) securityContext.getUserPrincipal();
		List<Integer> datasets = DatasetTableResource.getDatasetIdsForUser(req, resp, userDetails, "genotype");

		try (Connection conn = Database.getConnection())
		{
			DSLContext context = Database.getContext(conn);
			String[] parts = callSetDbId.split("-");

			DatasetsRecord dataset = context.selectFrom(DATASETS)
											.where(DATASETS.ID.in(datasets))
											.and(DATASETS.IS_EXTERNAL.eq(false))
											.and(DATASETS.ID.cast(String.class).eq(parts[0]))
											.fetchAny();
			GerminatebaseRecord germplasm = context.selectFrom(GERMINATEBASE)
												   .where(GERMINATEBASE.ID.cast(String.class).eq(parts[1]))
												   .fetchAny();
			

			List<Integer> mapIds = context.select(MAPS.ID)
       									  .from(MAPS)
										  .fetch()
										  .into(Integer.class);
			
			if (!mapIds.contains(mapid)) {
				throw new IllegalArgumentException("mapid not found in the database");
			}

			List<Integer> chromosomeCounts = context.selectCount()
													.from(MAPS)
													.leftJoin(MAPDEFINITIONS).on(MAPDEFINITIONS.MAP_ID.eq(MAPS.ID))
													.where(MAPS.ID.eq(mapid))
													.and(MAPS.VISIBILITY.eq(true)
														.or(MAPS.USER_ID.eq(userDetails.getId())))
													.groupBy(MAPDEFINITIONS.CHROMOSOME)
													.fetch()
													.into(Integer.class);


			List<Double> positions = context.select(MAPDEFINITIONS.DEFINITION_START)
				.from(MAPS)
				.leftJoin(MAPDEFINITIONS).on(MAPDEFINITIONS.MAP_ID.eq(MAPS.ID))
				.where(MAPS.ID.eq(mapid))
				.and(MAPS.VISIBILITY.eq(true).or(MAPS.USER_ID.eq(userDetails.getId())))
				.fetch()
				.into(Double.class);
			
			List<Integer> chromosomes = context.select(MAPDEFINITIONS.CHROMOSOME)
				.from(MAPS)
				.leftJoin(MAPDEFINITIONS).on(MAPDEFINITIONS.MAP_ID.eq(MAPS.ID))
				.where(MAPS.ID.eq(mapid))
				.and(MAPS.VISIBILITY.eq(true).or(MAPS.USER_ID.eq(userDetails.getId())))
				.fetch()
				.into(Integer.class);
				
			if (dataset == null || StringUtils.isEmpty(dataset.getSourceFile()) || germplasm == null)
			{
				resp.sendError(Response.Status.BAD_REQUEST.getStatusCode());
				return null;
			}

			GenotypeEncodingParams params = new GenotypeEncodingParams();
			params.setUnknownString(unknownString != null && !unknownString.isEmpty() ? unknownString : "N");
			params.setSepPhased(sepPhased != null && !sepPhased.isEmpty() ? sepPhased : "|");
			params.setSepUnphased(sepUnphased != null && !sepUnphased.isEmpty() ? sepUnphased : "/");
			params.setUnknownString(unknownString != null && !unknownString.isEmpty() ? unknownString : "N");
			
			try
			{
				params.setCollapse(!expandHomozygotes);
			}
			catch (Exception e)
			{
			}

			Hdf5DataExtractor extractor = new Hdf5DataExtractor(new File(Brapi.BRAPI.hdf5BaseFolder, dataset.getSourceFile()));
			List<String> alleles = extractor.getAllelesForLine(germplasm.getName(), params);
			List<String> markerNames = extractor.getMarkers();


			if (alleles.size()!= chromosomeCounts.stream().mapToInt(Integer::intValue).sum()) {
				throw new Exception("wrong mapID is selected");
			}

			// CHROMOSOME
			List<List<Integer>> processedListChromosomes = new ArrayList<>();
			int index = 0;
			for (int count : chromosomeCounts) {
				List<Integer> sublistC = chromosomes.subList(index, index + count);
				processedListChromosomes.add(sublistC);
				index += count;
			}

			int indexOfChromosome = -1;
			for (int i = 0; i < processedListChromosomes.size(); i++) {
				List<Integer> sublist = processedListChromosomes.get(i);
				if (sublist.contains(chromosome)) {
					indexOfChromosome = i;
					break;
				}
			}

			if (indexOfChromosome == -1) {
				throw new Exception("Chromosome is not found");
			}			

			// ALLELES
			List<List<String>> processedListAlleles = new ArrayList<>();
			index = 0;
			for (int count : chromosomeCounts) {
				List<String> sublistA = alleles.subList(index, index + count);
				processedListAlleles.add(sublistA);
				index += count;
			}
			List<String> nthSublistOfAlleles = processedListAlleles.get(indexOfChromosome);

			//POSITION
			List<List<Double>> processedListPositions = new ArrayList<>();
			index = 0;
			for (int count : chromosomeCounts) {
				List<Double> sublistP = positions.subList(index, index + count);
				processedListPositions.add(sublistP);
				index += count;
			}
			List<Double> nthSublistOfPositions = processedListPositions.get(indexOfChromosome);

			//MARKERS
			List<List<String>> processedListMarkerNames = new ArrayList<>();
			index = 0;
			for (int count : chromosomeCounts) {
				List<String> sublistM = markerNames.subList(index, index + count);
				processedListMarkerNames.add(sublistM);
				index += count;
			}
			List<String> nthSublistOfMarkerNames = processedListMarkerNames.get(indexOfChromosome);
			

			List<String> filteredAlleles = new ArrayList<>();
			List<String> filteredMarkerNames = new ArrayList<>();
			List<Double> filteredPositions = new ArrayList<>();


			for (int i = 0; i < nthSublistOfAlleles.size(); i++) {
				Double position = nthSublistOfPositions.get(i);
				if (position >= positionStart && position <= positionEnd) {
					filteredAlleles.add(nthSublistOfAlleles.get(i));
					filteredMarkerNames.add(nthSublistOfMarkerNames.get(i));
					filteredPositions.add(position);
				}
			}

			List<Call> calls = IntStream.range(0, filteredAlleles.size())
										.skip(pageSize * page)
										.limit(pageSize)
										.mapToObj(i -> new Call()
											.setCallSetDbId(callSetDbId)
											.setCallSetName(germplasm.getName())
											.setGenotypeValue(filteredAlleles.get(i))
											.setVariantName(filteredMarkerNames.get(i) + "-" + filteredPositions.get(i))
											)
										.collect(Collectors.toList());

			CallResult<Call> callResult = new CallResult<Call>()
				.setData(calls)
				.setExpandHomozygotes(!params.isCollapse())
				.setSepPhased(params.getSepPhased())
				.setSepUnphased(params.getSepUnphased())
				.setUnknownString(params.getUnknownString());
			return new BaseResult<>(callResult, page, pageSize, filteredAlleles.size());
		}
	}
}


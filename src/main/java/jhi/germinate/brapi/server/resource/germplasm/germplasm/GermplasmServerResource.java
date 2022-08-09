package jhi.germinate.brapi.server.resource.germplasm.germplasm;

import jhi.germinate.resource.enums.UserType;
import jhi.germinate.server.*;
import jhi.germinate.server.database.codegen.tables.Germinatebase;
import jhi.germinate.server.database.codegen.tables.records.ViewTableLocationsRecord;
import jhi.germinate.server.resource.datasets.DatasetTableResource;
import jhi.germinate.server.util.*;
import org.jooq.*;
import org.jooq.impl.*;
import uk.ac.hutton.ics.brapi.resource.base.*;
import uk.ac.hutton.ics.brapi.resource.germplasm.germplasm.Collection;
import uk.ac.hutton.ics.brapi.resource.germplasm.germplasm.*;
import uk.ac.hutton.ics.brapi.server.germplasm.germplasm.BrapiGermplasmServerResource;

import jakarta.annotation.security.PermitAll;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.*;
import java.util.*;
import java.util.stream.Collectors;

import static jhi.germinate.server.database.codegen.tables.Biologicalstatus.*;
import static jhi.germinate.server.database.codegen.tables.Countries.*;
import static jhi.germinate.server.database.codegen.tables.Germinatebase.*;
import static jhi.germinate.server.database.codegen.tables.Institutions.*;
import static jhi.germinate.server.database.codegen.tables.Locations.*;
import static jhi.germinate.server.database.codegen.tables.Pedigreedefinitions.*;
import static jhi.germinate.server.database.codegen.tables.Pedigrees.*;
import static jhi.germinate.server.database.codegen.tables.Storage.*;
import static jhi.germinate.server.database.codegen.tables.Storagedata.*;
import static jhi.germinate.server.database.codegen.tables.Synonyms.*;
import static jhi.germinate.server.database.codegen.tables.Taxonomies.*;
import static jhi.germinate.server.database.codegen.tables.ViewTableLocations.*;

@Path("brapi/v2/germplasm")
public class GermplasmServerResource extends GermplasmBaseServerResource implements BrapiGermplasmServerResource
{
	@GET
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Secured
	@PermitAll
	public BaseResult<ArrayResult<Germplasm>> getGermplasm(@QueryParam("accessionNumber") String accessionNumber,
														   @QueryParam("collection") String collection,
														   @QueryParam("binomialName") String binomialName,
														   @QueryParam("genus") String genus,
														   @QueryParam("species") String species,
														   @QueryParam("synonym") String synonym,
														   @QueryParam("parentDbId") String parentDbId,
														   @QueryParam("progenyDbId") String progenyDbId,
														   @QueryParam("commonCropName") String commonCropName,
														   @QueryParam("programDbId") String programDbId,
														   @QueryParam("trialDbId") String trialDbId,
														   @QueryParam("studyDbId") String studyDbId,
														   @QueryParam("germplasmDbId") String germplasmDbId,
														   @QueryParam("germplasmName") String germplasmName,
														   @QueryParam("germplasmPUI") String germplasmPUI,
														   @QueryParam("externalReferenceId") String externalReferenceId,
														   @QueryParam("externalReferenceSource") String externalReferenceSource)
		throws IOException, SQLException
	{
		try (Connection conn = Database.getConnection())
		{
			DSLContext context = Database.getContext(conn);
			List<Condition> conditions = new ArrayList<>();

			if (!StringUtils.isEmpty(germplasmPUI))
				conditions.add(GERMINATEBASE.PUID.eq(germplasmPUI));
			if (!StringUtils.isEmpty(germplasmDbId))
				conditions.add(GERMINATEBASE.ID.cast(String.class).eq(germplasmDbId));
			if (!StringUtils.isEmpty(germplasmName))
				conditions.add(GERMINATEBASE.NAME.eq(germplasmName));
			if (!StringUtils.isEmpty(commonCropName))
				conditions.add(TAXONOMIES.CROPNAME.eq(commonCropName));
			if (!StringUtils.isEmpty(accessionNumber))
				conditions.add(GERMINATEBASE.NAME.eq(accessionNumber));
			if (!StringUtils.isEmpty(genus))
				conditions.add(TAXONOMIES.GENUS.eq(genus));
			if (!StringUtils.isEmpty(species))
				conditions.add(TAXONOMIES.SPECIES.eq(species));
			if (!StringUtils.isEmpty(synonym))
			{
				String cleaned = synonym.replaceAll("[^a-zA-Z0-9_-]", "");
				conditions.add(DSL.condition("JSON_CONTAINS(" + SYNONYMS.SYNONYMS_.getName() + ", '\"" + cleaned + "\"')"));
			}
			if (!StringUtils.isEmpty(parentDbId))
				conditions.add(GERMINATEBASE.ENTITYPARENT_ID.cast(String.class).eq(parentDbId));
			if (!StringUtils.isEmpty(progenyDbId))
			{
				Germinatebase g = GERMINATEBASE.as("g");
				conditions.add(DSL.exists(DSL.selectOne().from(g).where(g.ENTITYPARENT_ID.eq(GERMINATEBASE.ID).and(g.ID.cast(String.class).eq(progenyDbId)))));
			}

			return getGermplasm(context, conditions);
		}
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Secured(UserType.DATA_CURATOR)
	public BaseResult<ArrayResult<Germplasm>> postGermplasm(Germplasm[] newGermplasm)
		throws IOException, SQLException
	{
		try (Connection conn = Database.getConnection())
		{
			DSLContext context = Database.getContext(conn);
			List<Integer> newIds = Arrays.stream(newGermplasm)
										 .map(g -> {
											 try
											 {
												 return addGermplasm(context, g, false);
											 }
											 catch (IOException e)
											 {
												 e.printStackTrace();

												 return null;
											 }
										 })
										 .filter(Objects::nonNull)
										 .collect(Collectors.toList());

			return getGermplasm(context, Collections.singletonList(GERMINATEBASE.ID.in(newIds)));
		}
	}

	@GET
	@Path("/{germplasmDbId}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Secured
	@PermitAll
	public BaseResult<Germplasm> getGermplasmById(@PathParam("germplasmDbId") String germplasmDbId)
		throws IOException, SQLException
	{
		try (Connection conn = Database.getConnection())
		{
			DSLContext context = Database.getContext(conn);
			BaseResult<ArrayResult<Germplasm>> tempResult = getGermplasm(context, Collections.singletonList(GERMINATEBASE.ID.cast(String.class).eq(germplasmDbId)));

			Germplasm germplasm = CollectionUtils.isEmpty(tempResult.getResult().getData()) ? null : tempResult.getResult().getData().get(0);
			return new BaseResult<>(germplasm, page, pageSize, 1);
		}
	}

	@PUT
	@Path("/{germplasmDbId}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Secured(UserType.DATA_CURATOR)
	public BaseResult<Germplasm> putGermplasmById(@PathParam("germplasmDbId") String germplasmDbId, Germplasm germplasm)
		throws IOException, SQLException
	{
		if (StringUtils.isEmpty(germplasmDbId) || germplasm == null || germplasm.getGermplasmDbId() != null && !Objects.equals(germplasm.getGermplasmDbId(), germplasmDbId))
		{
			resp.sendError(Response.Status.BAD_REQUEST.getStatusCode());
			return null;
		}

		try (Connection conn = Database.getConnection())
		{
			DSLContext context = Database.getContext(conn);
			addGermplasm(context, germplasm, true);

			BaseResult<ArrayResult<Germplasm>> tempResult = getGermplasm(context, Collections.singletonList(GERMINATEBASE.ID.cast(String.class).eq(germplasmDbId)));

			if (!CollectionUtils.isEmpty(tempResult.getResult().getData()))
				return new BaseResult<>(tempResult.getResult().getData().get(0), page, pageSize, 1);
			else
				return new BaseResult<>(null, page, pageSize, 0);
		}
	}

	@GET
	@Path("/{germplasmDbId}/mcpd")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public BaseResult<Mcpd> getGermplasmMcpd(@PathParam("germplasmDbId") String germplasmDbId)
		throws IOException, SQLException
	{
		if (StringUtils.isEmpty(germplasmDbId))
		{
			resp.sendError(Response.Status.BAD_REQUEST.getStatusCode());
			return null;
		}

		AuthenticationFilter.UserDetails userDetails = (AuthenticationFilter.UserDetails) securityContext.getUserPrincipal();
		List<Integer> datasetIds = DatasetTableResource.getDatasetIdsForUser(req, resp, userDetails, "pedigree");

		try (Connection conn = Database.getConnection())
		{
			DSLContext context = Database.getContext(conn);
			Mcpd result = context.select(
									 GERMINATEBASE.NAME.as("accessionNumber"),
									 GERMINATEBASE.ACQDATE.as("acquisitionDate"),
									 GERMINATEBASE.COLLDATE.as("acquisitionDate"),
									 GERMINATEBASE.COLLSRC_ID.as("acquisitionSourceCode"),
									 DSL.inline(null, SQLDataType.VARCHAR).as("alternateIDs"),
									 PEDIGREEDEFINITIONS.DEFINITION.as("ancestralData"),
									 BIOLOGICALSTATUS.ID.cast(String.class).as("biologicalStatusOfAccessionCode"),
									 TAXONOMIES.CROPNAME.as("commonCropName"),
									 COUNTRIES.COUNTRY_CODE3.as("countryOfOrigin"),
									 TAXONOMIES.GENUS.as("genus"),
									 GERMINATEBASE.ID.as("germplasmDbId"),
									 GERMINATEBASE.PUID.as("germplasmPUI"),
									 INSTITUTIONS.CODE.as("instituteCode"),
									 GERMINATEBASE.MLSSTATUS_ID.as("mlsStatus"),
									 TAXONOMIES.SPECIES.as("species"),
									 TAXONOMIES.SPECIES_AUTHOR.as("speciesAuthority"),
									 TAXONOMIES.SUBTAXA.as("subtaxon"),
									 TAXONOMIES.SUBTAXA_AUTHOR.as("subtaxonAuthority")
								 )
								 .from(GERMINATEBASE)
								 .leftJoin(BIOLOGICALSTATUS).on(BIOLOGICALSTATUS.ID.eq(GERMINATEBASE.BIOLOGICALSTATUS_ID))
								 .leftJoin(LOCATIONS).on(LOCATIONS.ID.eq(GERMINATEBASE.LOCATION_ID))
								 .leftJoin(COUNTRIES).on(COUNTRIES.ID.eq(LOCATIONS.COUNTRY_ID))
								 .leftJoin(INSTITUTIONS).on(INSTITUTIONS.ID.eq(GERMINATEBASE.INSTITUTION_ID))
								 .leftJoin(PEDIGREEDEFINITIONS).on(PEDIGREEDEFINITIONS.GERMINATEBASE_ID.eq(GERMINATEBASE.ID).and(PEDIGREEDEFINITIONS.DATASET_ID.in(datasetIds)))
								 .leftJoin(TAXONOMIES).on(TAXONOMIES.ID.eq(GERMINATEBASE.TAXONOMY_ID))
								 .leftJoin(SYNONYMS).on(SYNONYMS.FOREIGN_ID.eq(GERMINATEBASE.ID).and(SYNONYMS.SYNONYMTYPE_ID.eq(1)))
								 .where(GERMINATEBASE.ID.cast(String.class).eq(germplasmDbId))
								 .limit(pageSize)
								 .offset(pageSize * page)
								 .fetchAnyInto(Mcpd.class);

			if (result != null)
			{
				Map<Integer, String[]> synonyms = context.selectFrom(SYNONYMS).where(SYNONYMS.SYNONYMTYPE_ID.eq(1)).and(SYNONYMS.FOREIGN_ID.cast(String.class).eq(result.getGermplasmDbId())).fetchMap(SYNONYMS.FOREIGN_ID, SYNONYMS.SYNONYMS_);
				Map<Integer, ViewTableLocationsRecord> origins = context.select().from(VIEW_TABLE_LOCATIONS).leftJoin(GERMINATEBASE).on(GERMINATEBASE.LOCATION_ID.eq(VIEW_TABLE_LOCATIONS.LOCATION_ID)).where(GERMINATEBASE.LOCATION_ID.isNotNull()).and(VIEW_TABLE_LOCATIONS.LOCATION_TYPE.eq("collectingsites")).and(VIEW_TABLE_LOCATIONS.LOCATION_LATITUDE.isNotNull()).and(VIEW_TABLE_LOCATIONS.LOCATION_LONGITUDE.isNotNull()).fetchMap(GERMINATEBASE.ID, ViewTableLocationsRecord.class);
				Map<Integer, List<String>> storage = new HashMap<>();
				context.select().from(STORAGE).leftJoin(STORAGEDATA).on(STORAGEDATA.STORAGE_ID.eq(STORAGE.ID)).where(STORAGEDATA.GERMINATEBASE_ID.cast(String.class).eq(result.getGermplasmDbId())).forEach(r -> {
					List<String> list = storage.get(r.get(STORAGEDATA.GERMINATEBASE_ID));

					if (list == null)
						list = new ArrayList<>();

					list.add(Integer.toString(r.get(STORAGE.ID)));

					storage.put(r.get(STORAGEDATA.GERMINATEBASE_ID), list);
				});

				Integer id = Integer.parseInt(result.getGermplasmDbId());
				String[] synonym = synonyms.get(id);
				if (synonym != null)
				{
					List<String> mapped = Arrays.stream(synonym).collect(Collectors.toList());
					result.setAlternateIDs(mapped);
					result.setAccessionNames(mapped);
				}

				ViewTableLocationsRecord location = origins.get(id);
				if (location != null)
				{
					// Then take care of the lat, lng and elv
					BigDecimal lat = location.getLocationLatitude();
					BigDecimal lng = location.getLocationLongitude();
					BigDecimal elv = location.getLocationElevation();

					result.setCollectingInfo(new Collection()
						.setCollectingSite(new Collsite()
							.setCoordinateUncertainty(location.getLocationCoordinateUncertainty() != null ? Integer.toString(location.getLocationCoordinateUncertainty()) : null)
							.setElevation(elv != null ? elv.toPlainString() : null)
							.setLatitudeDecimal(lat != null ? lat.toPlainString() : null)
							.setLongitudeDecimal(lng != null ? lng.toPlainString() : null)
							.setLocationDescription(location.getLocationName()))
					);
				}

				result.setStorageTypeCodes(storage.get(id));
			}

			return new BaseResult<>(result, page, pageSize, 1);
		}
	}
}

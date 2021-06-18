package jhi.germinate.brapi.server.resource.germplasm.germplasm;

import jhi.germinate.server.database.codegen.tables.records.*;
import jhi.germinate.server.util.*;
import org.jooq.*;
import uk.ac.hutton.ics.brapi.resource.base.*;
import uk.ac.hutton.ics.brapi.resource.core.location.*;
import uk.ac.hutton.ics.brapi.resource.germplasm.germplasm.*;
import uk.ac.hutton.ics.brapi.server.base.BaseServerResource;

import javax.ws.rs.core.Response;
import java.io.IOException;
import java.math.*;
import java.sql.Timestamp;
import java.util.*;
import java.util.stream.Collectors;

import static jhi.germinate.server.database.codegen.tables.Biologicalstatus.*;
import static jhi.germinate.server.database.codegen.tables.Countries.*;
import static jhi.germinate.server.database.codegen.tables.Germinatebase.*;
import static jhi.germinate.server.database.codegen.tables.Institutions.*;
import static jhi.germinate.server.database.codegen.tables.Locations.*;
import static jhi.germinate.server.database.codegen.tables.Pedigreedefinitions.*;
import static jhi.germinate.server.database.codegen.tables.Pedigreenotations.*;
import static jhi.germinate.server.database.codegen.tables.Storage.*;
import static jhi.germinate.server.database.codegen.tables.Storagedata.*;
import static jhi.germinate.server.database.codegen.tables.Synonyms.*;
import static jhi.germinate.server.database.codegen.tables.Taxonomies.*;
import static jhi.germinate.server.database.codegen.tables.ViewTableLocations.*;

/**
 * @author Sebastian Raubach
 */
public abstract class GermplasmBaseServerResource extends BaseServerResource
{
	protected Integer addGermplasm(DSLContext context, Germplasm newGermplasm, boolean forceId)
		throws IOException
	{
		if (newGermplasm == null || StringUtils.isEmpty(newGermplasm.getAccessionNumber()))
		{
			resp.sendError(Response.Status.BAD_REQUEST.getStatusCode());
			return null;
		}

		GerminatebaseRecord existing = context.selectFrom(GERMINATEBASE).where(GERMINATEBASE.NAME.eq(newGermplasm.getAccessionNumber())).fetchAny();
		if (existing != null)
		{
			resp.sendError(Response.Status.CONFLICT.getStatusCode(), "Germplasm with this identifier already exists.");
			return null;
		}

		// Get country
		CountriesRecord country = null;
		if (!StringUtils.isEmpty(newGermplasm.getCountryOfOriginCode()))
		{
			country = context.selectFrom(COUNTRIES)
							 .where(COUNTRIES.COUNTRY_CODE3.eq(newGermplasm.getCountryOfOriginCode()))
							 .fetchAny();
		}

		// Get or create taxonomy
		TaxonomiesRecord taxonomy = null;
		if (!StringUtils.isEmpty(newGermplasm.getGenus()))
		{
			taxonomy = context.selectFrom(TAXONOMIES)
							  .where(TAXONOMIES.GENUS.eq(newGermplasm.getGenus()))
							  .and(TAXONOMIES.SPECIES.isNotDistinctFrom(newGermplasm.getSpecies()))
							  .and(TAXONOMIES.SPECIES_AUTHOR.isNotDistinctFrom(newGermplasm.getSpeciesAuthority()))
							  .and(TAXONOMIES.CROPNAME.isNotDistinctFrom(newGermplasm.getCommonCropName()))
							  .and(TAXONOMIES.SUBTAXA.isNotDistinctFrom(newGermplasm.getSubtaxa()))
							  .and(TAXONOMIES.SUBTAXA_AUTHOR.isNotDistinctFrom(newGermplasm.getSubtaxaAuthority()))
							  .fetchAny();

			if (taxonomy == null)
			{
				taxonomy = context.newRecord(TAXONOMIES);
				taxonomy.setGenus(newGermplasm.getGenus());
				taxonomy.setSpecies(newGermplasm.getSpecies());
				taxonomy.setSpeciesAuthor(newGermplasm.getSpeciesAuthority());
				taxonomy.setCropname(newGermplasm.getCommonCropName());
				taxonomy.setSubtaxa(newGermplasm.getSubtaxa());
				taxonomy.setSubtaxaAuthor(newGermplasm.getSubtaxaAuthority());
				taxonomy.setCreatedOn(new Timestamp(System.currentTimeMillis()));
				taxonomy.store();
			}
		}

		// Get or create location
		LocationsRecord location = null;
		if (!CollectionUtils.isEmpty(newGermplasm.getGermplasmOrigin()))
		{
			double[] values = new double[]{0d, 0d, 0d};
			final int[] count = new int[]{0};

			newGermplasm.getGermplasmOrigin()
						.stream()
						.filter(o -> o.getCoordinates() != null && o.getCoordinates().getGeometry() != null && o.getCoordinates().getGeometry().getCoordinates() != null && o.getCoordinates().getGeometry().getCoordinates().length >= 2)
						.forEach(o -> {
							double longitude = o.getCoordinates().getGeometry().getCoordinates()[0];
							double latitude = o.getCoordinates().getGeometry().getCoordinates()[1];
							Double elevation = o.getCoordinates().getGeometry().getCoordinates().length > 2 ? o.getCoordinates().getGeometry().getCoordinates()[2] : null;

							values[0] += longitude;
							values[1] += latitude;

							if (elevation != null)
								values[2] += elevation;

							count[0]++;
						});

			// If there's at least one valid location
			if (count[0] > 0)
			{
				BigDecimal lng = getBigDecimal(values[0]);
				BigDecimal lat = getBigDecimal(values[1]);
				BigDecimal elv = getBigDecimal(values[2]);

				SelectConditionStep<LocationsRecord> step = context.selectFrom(LOCATIONS)
																   .where(LOCATIONS.LONGITUDE.eq(lng))
																   .and(LOCATIONS.LATITUDE.eq(lat))
																   .and(LOCATIONS.ELEVATION.isNotDistinctFrom(elv))
																   .and(LOCATIONS.SITE_NAME.eq("Collecting site"));

				if (country != null)
					step.and(LOCATIONS.COUNTRY_ID.eq(country.getId()));

				location = step.fetchAny();
			}
		}

		InstitutionsRecord institution = null;

		if (!StringUtils.isEmpty(newGermplasm.getInstituteName()))
		{
			institution = context.selectFrom(INSTITUTIONS)
								 .where(INSTITUTIONS.NAME.eq(newGermplasm.getInstituteName()))
								 .and(INSTITUTIONS.CODE.isNotDistinctFrom(newGermplasm.getInstituteCode()))
								 .fetchAny();

			if (institution == null)
			{
				institution = context.newRecord(INSTITUTIONS);
				institution.setName(newGermplasm.getInstituteName());
				institution.setCode(newGermplasm.getInstituteCode());
				institution.setCreatedOn(new Timestamp(System.currentTimeMillis()));
				institution.store();
			}
		}

		// Create germplasm
		existing = context.newRecord(GERMINATEBASE);

		if (forceId)
		{
			try
			{
				existing.setId(Integer.parseInt(newGermplasm.getGermplasmDbId()));
			}
			catch (Exception e)
			{
				resp.sendError(Response.Status.BAD_REQUEST.getStatusCode(), "Id has to be an integer");
				return null;
			}
		}

		existing.setName(newGermplasm.getAccessionNumber());
		existing.setGeneralIdentifier(newGermplasm.getAccessionNumber());
		existing.setAcqdate(newGermplasm.getAcquisitionDate());
		if (!StringUtils.isEmpty(newGermplasm.getBiologicalStatusOfAccessionCode()))
		{
			try
			{
				existing.setBiologicalstatusId(Integer.parseInt(newGermplasm.getBiologicalStatusOfAccessionCode()));
			}
			catch (Exception e)
			{
			}
		}
		existing.setTaxonomyId(taxonomy != null ? taxonomy.getId() : null);
		existing.setLocationId(location != null ? location.getId() : null);
		existing.setPuid(newGermplasm.getGermplasmPUI());
		existing.setInstitutionId(institution != null ? institution.getId() : null);
		existing.setCreatedOn(new Timestamp(System.currentTimeMillis()));
		existing.store();

		// Create pedigree
		if (!StringUtils.isEmpty(newGermplasm.getPedigree()))
		{
			PedigreenotationsRecord notation = context.selectFrom(PEDIGREENOTATIONS)
													  .where(PEDIGREENOTATIONS.NAME.eq("MCPD"))
													  .fetchAny();

			if (notation == null)
			{
				notation = context.newRecord(PEDIGREENOTATIONS);
				notation.setName("MCPD");
				notation.setDescription("MCPD");
				notation.store();
			}

			PedigreedefinitionsRecord pedigree = context.newRecord(PEDIGREEDEFINITIONS);
			pedigree.setGerminatebaseId(existing.getId());
			pedigree.setDefinition(newGermplasm.getPedigree());
			pedigree.setPedigreenotationId(notation.getId());
			pedigree.setCreatedOn(new Timestamp(System.currentTimeMillis()));
			pedigree.store();
		}

		final Integer germplasmId = existing.getId();
		// Create storagedata
		if (!CollectionUtils.isEmpty(newGermplasm.getStorageTypes()))
		{
			newGermplasm.getStorageTypes()
						.forEach(s -> {
							try
							{
								StoragedataRecord storage = context.newRecord(STORAGEDATA);
								storage.setGerminatebaseId(germplasmId);
								storage.setStorageId(Integer.parseInt(s.getCode()));
								storage.setCreatedOn(new Timestamp(System.currentTimeMillis()));
								storage.store();
							}
							catch (Exception e)
							{
								e.printStackTrace();
							}
						});
		}

		if (!CollectionUtils.isEmpty(newGermplasm.getSynonyms()))
		{
			String[] array = newGermplasm.getSynonyms().stream().map(Synonym::getSynonym).toArray(String[]::new);

			SynonymsRecord synonyms = context.newRecord(SYNONYMS);
			synonyms.setForeignId(germplasmId);
			synonyms.setSynonymtypeId(1);
			synonyms.setSynonyms(array);
			synonyms.setCreatedOn(new Timestamp(System.currentTimeMillis()));
			synonyms.store();
		}

		return existing.getId();
	}

	private BigDecimal getBigDecimal(Double value)
	{
		try
		{
			BigDecimal result = new BigDecimal(value, MathContext.DECIMAL64);
			result = result.setScale(10, RoundingMode.HALF_UP);
			return result;
		}
		catch (Exception e)
		{
			return null;
		}
	}

	protected BaseResult<ArrayResult<Germplasm>> getGermplasm(DSLContext context, List<Condition> conditions)
	{
		SelectJoinStep<?> step = context.select(
			GERMINATEBASE.NAME.as("accessionNumber"),
			GERMINATEBASE.ACQDATE.as("acquisitionDate"),
			BIOLOGICALSTATUS.ID.cast(String.class).as("biologicalStatusOfAccessionCode"),
			BIOLOGICALSTATUS.SAMPSTAT.as("biologicalStatusOfAccessionDescription"),
			TAXONOMIES.CROPNAME.as("commonCropName"),
			COUNTRIES.COUNTRY_CODE3.as("countryOfOriginCode"),
			GERMINATEBASE.NAME.as("defaultDisplayName"),
			TAXONOMIES.GENUS.as("genus"),
			GERMINATEBASE.ID.as("germplasmDbId"),
			GERMINATEBASE.NAME.as("germplasmName"),
			GERMINATEBASE.PUID.as("germplasmPUI"),
			INSTITUTIONS.CODE.as("instituteCode"),
			INSTITUTIONS.NAME.as("instituteName"),
			PEDIGREEDEFINITIONS.DEFINITION.as("pedigree"),
			TAXONOMIES.SPECIES.as("species"),
			TAXONOMIES.SPECIES_AUTHOR.as("speciesAuthority"),
			TAXONOMIES.SUBTAXA.as("subtaxa"),
			TAXONOMIES.SUBTAXA_AUTHOR.as("subtaxaAuthority")
		)
										.hint("SQL_CALC_FOUND_ROWS")
										.from(GERMINATEBASE)
										.leftJoin(BIOLOGICALSTATUS).on(BIOLOGICALSTATUS.ID.eq(GERMINATEBASE.BIOLOGICALSTATUS_ID))
										.leftJoin(LOCATIONS).on(LOCATIONS.ID.eq(GERMINATEBASE.LOCATION_ID))
										.leftJoin(COUNTRIES).on(COUNTRIES.ID.eq(LOCATIONS.COUNTRY_ID))
										.leftJoin(INSTITUTIONS).on(INSTITUTIONS.ID.eq(GERMINATEBASE.INSTITUTION_ID))
										.leftJoin(PEDIGREEDEFINITIONS).on(PEDIGREEDEFINITIONS.GERMINATEBASE_ID.eq(GERMINATEBASE.ID))
										.leftJoin(TAXONOMIES).on(TAXONOMIES.ID.eq(GERMINATEBASE.TAXONOMY_ID))
										.leftJoin(SYNONYMS).on(SYNONYMS.FOREIGN_ID.eq(GERMINATEBASE.ID).and(SYNONYMS.SYNONYMTYPE_ID.eq(1)));

		if (conditions != null)
		{
			for (Condition condition : conditions)
				step.where(condition);
		}

		List<Germplasm> result = step.limit(pageSize)
									 .offset(pageSize * page)
									 .fetchInto(Germplasm.class);

		long totalCount = context.fetchOne("SELECT FOUND_ROWS()").into(Long.class);

		List<String> ids = result.stream().map(Germplasm::getGermplasmDbId).collect(Collectors.toList());

		Map<Integer, String[]> synonyms = context.selectFrom(SYNONYMS).where(SYNONYMS.SYNONYMTYPE_ID.eq(1)).and(SYNONYMS.FOREIGN_ID.in(ids)).fetchMap(SYNONYMS.FOREIGN_ID, SYNONYMS.SYNONYMS_);
		Map<Integer, ViewTableLocationsRecord> origins = context.select().from(VIEW_TABLE_LOCATIONS).leftJoin(GERMINATEBASE).on(GERMINATEBASE.LOCATION_ID.eq(VIEW_TABLE_LOCATIONS.LOCATION_ID)).where(GERMINATEBASE.LOCATION_ID.isNotNull()).and(VIEW_TABLE_LOCATIONS.LOCATION_TYPE.eq("collectingsites")).and(VIEW_TABLE_LOCATIONS.LOCATION_LATITUDE.isNotNull()).and(VIEW_TABLE_LOCATIONS.LOCATION_LONGITUDE.isNotNull()).fetchMap(GERMINATEBASE.ID, ViewTableLocationsRecord.class);
		Map<Integer, List<Storage>> storage = new HashMap<>();
		context.select().from(STORAGE).leftJoin(STORAGEDATA).on(STORAGEDATA.STORAGE_ID.eq(STORAGE.ID)).where(STORAGEDATA.GERMINATEBASE_ID.in(ids)).forEach(r -> {
			List<Storage> list = storage.get(r.get(STORAGEDATA.GERMINATEBASE_ID));

			if (list == null)
				list = new ArrayList<>();

			list.add(new Storage()
				.setCode(Integer.toString(r.get(STORAGE.ID)))
				.setDescription(r.get(STORAGE.DESCRIPTION)));

			storage.put(r.get(STORAGEDATA.GERMINATEBASE_ID), list);
		});

		result.forEach(g -> {
			Integer id = Integer.parseInt(g.getGermplasmDbId());
			String[] synonym = synonyms.get(id);
			if (synonym != null)
			{
				List<Synonym> mapped = Arrays.stream(synonym).map(s -> new Synonym().setSynonym(s)).collect(Collectors.toList());
				g.setSynonyms(mapped);
			}

			ViewTableLocationsRecord location = origins.get(id);
			if (location != null)
			{
				// Then take care of the lat, lng and elv
				BigDecimal lat = location.getLocationLatitude();
				BigDecimal lng = location.getLocationLongitude();
				BigDecimal elv = location.getLocationElevation();

				if (lat != null && lng != null)
				{
					double[] coordinates;

					if (elv != null)
						coordinates = new double[]{lng.doubleValue(), lat.doubleValue(), elv.doubleValue()};
					else
						coordinates = new double[]{lng.doubleValue(), lat.doubleValue()};

					g.setGermplasmOrigin(Collections.singletonList(new Origin()
						.setCoordinateUncertainty(location.getLocationCoordinateUncertainty() != null ? Integer.toString(location.getLocationCoordinateUncertainty()) : null)
						.setCoordinates(new CoordinatesPoint()
							.setType("Feature")
							.setGeometry(new GeometryPoint()
								.setType("Point")
								.setCoordinates(coordinates)))));
				}
			}

			g.setStorageTypes(storage.get(id));
		});

		return new BaseResult<>(new ArrayResult<Germplasm>()
			.setData(result), page, pageSize, totalCount);
	}
}
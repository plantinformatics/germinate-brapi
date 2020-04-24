package jhi.germinate.brapi.server.resource.core.germplasm;

import com.google.gson.JsonArray;

import org.jooq.*;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

import jhi.germinate.brapi.resource.germplasm.*;
import jhi.germinate.brapi.resource.location.*;
import jhi.germinate.brapi.server.resource.BaseServerResource;
import jhi.germinate.server.database.tables.records.ViewTableLocationsRecord;

import static jhi.germinate.server.database.tables.Biologicalstatus.*;
import static jhi.germinate.server.database.tables.Countries.*;
import static jhi.germinate.server.database.tables.Germinatebase.*;
import static jhi.germinate.server.database.tables.Institutions.*;
import static jhi.germinate.server.database.tables.Locations.*;
import static jhi.germinate.server.database.tables.Pedigreedefinitions.*;
import static jhi.germinate.server.database.tables.Storage.*;
import static jhi.germinate.server.database.tables.Storagedata.*;
import static jhi.germinate.server.database.tables.Synonyms.*;
import static jhi.germinate.server.database.tables.Taxonomies.*;
import static jhi.germinate.server.database.tables.ViewTableLocations.*;

/**
 * @author Sebastian Raubach
 */
public abstract class GermplasmBaseServerResource<T> extends BaseServerResource<T>
{
	protected List<Germplasm> getGermplasm(DSLContext context, List<Condition> conditions)
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
									 .offset(pageSize * currentPage)
									 .fetchInto(Germplasm.class);

		List<String> ids = result.stream().map(Germplasm::getGermplasmDbId).collect(Collectors.toList());

		Map<Integer, JsonArray> synonyms = context.selectFrom(SYNONYMS).where(SYNONYMS.SYNONYMTYPE_ID.eq(1)).and(SYNONYMS.FOREIGN_ID.in(ids)).fetchMap(SYNONYMS.FOREIGN_ID, SYNONYMS.SYNONYMS_);
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
			JsonArray synonym = synonyms.get(id);
			if (synonym != null)
			{
				List<Synonym> mapped = new ArrayList<>();
				synonym.forEach(s -> mapped.add(new Synonym().setSynonym(s.getAsString())));
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

		return result;
	}
}

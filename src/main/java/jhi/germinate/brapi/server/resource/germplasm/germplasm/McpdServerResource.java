package jhi.germinate.brapi.server.resource.germplasm.germplasm;

import com.google.gson.JsonArray;

import org.jooq.DSLContext;
import org.jooq.impl.*;
import org.restlet.data.Status;
import org.restlet.resource.*;

import java.math.BigDecimal;
import java.sql.*;
import java.util.*;

import jhi.germinate.server.Database;
import jhi.germinate.server.database.tables.records.ViewTableLocationsRecord;
import jhi.germinate.server.util.StringUtils;
import uk.ac.hutton.ics.brapi.resource.base.BaseResult;
import uk.ac.hutton.ics.brapi.resource.germplasm.germplasm.Collection;
import uk.ac.hutton.ics.brapi.resource.germplasm.germplasm.*;
import uk.ac.hutton.ics.brapi.server.base.BaseServerResource;
import uk.ac.hutton.ics.brapi.server.germplasm.germplasm.BrapiGermplasmIndividualMcpdServerResource;

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
public class McpdServerResource extends BaseServerResource implements BrapiGermplasmIndividualMcpdServerResource
{
	private String germplasmDbId;

	@Override
	public void doInit()
	{
		super.doInit();

		try
		{
			this.germplasmDbId = getRequestAttributes().get("germplasmDbId").toString();
		}
		catch (Exception e)
		{
		}
	}

	@Get
	public BaseResult<Mcpd> getGermplasmMcpd()
	{
		if (StringUtils.isEmpty(germplasmDbId))
			throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST);

		try (Connection conn = Database.getConnection();
			 DSLContext context = Database.getContext(conn))
		{
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
								 .leftJoin(PEDIGREEDEFINITIONS).on(PEDIGREEDEFINITIONS.GERMINATEBASE_ID.eq(GERMINATEBASE.ID))
								 .leftJoin(TAXONOMIES).on(TAXONOMIES.ID.eq(GERMINATEBASE.TAXONOMY_ID))
								 .leftJoin(SYNONYMS).on(SYNONYMS.FOREIGN_ID.eq(GERMINATEBASE.ID).and(SYNONYMS.SYNONYMTYPE_ID.eq(1)))
								 .where(GERMINATEBASE.ID.cast(String.class).eq(germplasmDbId))
								 .limit(pageSize)
								 .offset(pageSize * currentPage)
								 .fetchAnyInto(Mcpd.class);

			if (result != null)
			{
				Map<Integer, JsonArray> synonyms = context.selectFrom(SYNONYMS).where(SYNONYMS.SYNONYMTYPE_ID.eq(1)).and(SYNONYMS.FOREIGN_ID.cast(String.class).eq(result.getGermplasmDbId())).fetchMap(SYNONYMS.FOREIGN_ID, SYNONYMS.SYNONYMS_);
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
				JsonArray synonym = synonyms.get(id);
				if (synonym != null)
				{
					List<String> mapped = new ArrayList<>();
					synonym.forEach(s -> mapped.add(s.getAsString()));
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

			return new BaseResult<>(result, currentPage, pageSize, 1);
		}
		catch (
			SQLException e)

		{
			e.printStackTrace();
			throw new ResourceException(Status.SERVER_ERROR_INTERNAL);
		}
	}
}

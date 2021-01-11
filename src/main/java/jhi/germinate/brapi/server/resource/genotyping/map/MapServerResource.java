package jhi.germinate.brapi.server.resource.genotyping.map;

import org.jooq.*;
import org.jooq.impl.DSL;
import org.restlet.data.Status;
import org.restlet.resource.*;

import java.sql.*;
import java.util.List;

import jhi.germinate.server.Database;
import jhi.germinate.server.util.StringUtils;
import uk.ac.hutton.ics.brapi.resource.base.*;
import uk.ac.hutton.ics.brapi.resource.genotyping.map.Map;
import uk.ac.hutton.ics.brapi.server.base.BaseServerResource;
import uk.ac.hutton.ics.brapi.server.genotyping.map.BrapiMapServerResource;

import static jhi.germinate.server.database.codegen.tables.Datasetmembers.*;
import static jhi.germinate.server.database.codegen.tables.Datasets.*;
import static jhi.germinate.server.database.codegen.tables.Mapdefinitions.*;
import static jhi.germinate.server.database.codegen.tables.Maps.*;

/**
 * @author Sebastian Raubach
 */
public class MapServerResource extends BaseServerResource implements BrapiMapServerResource
{
	public static final String PARAM_COMMON_CROP_NAME = "commonCropName";
	public static final String PARAM_MAP_PUI          = "mapPUI";
	public static final String PARAM_SCIENTIFIC_NAME  = "scientificName";
	public static final String PARAM_PROGRAM_DB_ID    = "programDbId";
	public static final String PARAM_TRIAL_DB_ID      = "trialDbId";
	public static final String PARAM_STUDY_DB_ID      = "studyDbId";

	private String mapPUI;
	private String programDbId;
	private String trialDbId;
	private String studyDbId;
	private String scientificName;
	private String commonCropName;

	@Override
	public void doInit()
	{
		super.doInit();

		this.mapPUI = getQueryValue(PARAM_MAP_PUI);
		this.programDbId = getQueryValue(PARAM_PROGRAM_DB_ID);
		this.scientificName = getQueryValue(PARAM_SCIENTIFIC_NAME);
		this.commonCropName = getQueryValue(PARAM_COMMON_CROP_NAME);
		this.trialDbId = getQueryValue(PARAM_TRIAL_DB_ID);
		this.studyDbId = getQueryValue(PARAM_STUDY_DB_ID);
	}

	@Get
	public BaseResult<ArrayResult<Map>> getMaps()
	{
		try (DSLContext context = Database.getContext())
		{
			SelectJoinStep<?> step = context.select(
				MAPS.ID.as("mapDbId"),
				MAPS.NAME.as("mapName"),
				DSL.countDistinct(MAPDEFINITIONS.CHROMOSOME).as("linkageGroupCount"),
				DSL.count(MAPDEFINITIONS.MARKER_ID).as("markerCount"),
				DSL.val("Genetic").as("type"),
				MAPS.CREATED_ON.as("publishedDate")
			)
											.hint("SQL_CALC_FOUND_ROWS")
											.from(MAPS)
											.leftJoin(MAPDEFINITIONS).on(MAPDEFINITIONS.MAP_ID.eq(MAPS.ID));

			step.where(MAPS.VISIBILITY.eq(true));

			// Filter on studyDbId (datasets.id)
			if (!StringUtils.isEmpty(studyDbId))
			{
				step.where(DSL.exists(DSL.selectOne()
										 .from(DATASETMEMBERS)
										 .where(DATASETMEMBERS.FOREIGN_ID.eq(MAPDEFINITIONS.MARKER_ID))
										 .and(DATASETMEMBERS.DATASETMEMBERTYPE_ID.eq(1))
										 .and(DATASETMEMBERS.DATASET_ID.cast(String.class).eq(studyDbId))));
			}

			// Filter on trialDbId (experiments.id)
			if (!StringUtils.isEmpty(trialDbId))
			{
				step.where(DSL.exists(DSL.selectOne()
										 .from(DATASETMEMBERS)
										 .leftJoin(DATASETS).on(DATASETS.ID.eq(DATASETMEMBERS.DATASET_ID))
										 .where(DATASETMEMBERS.FOREIGN_ID.eq(MAPDEFINITIONS.MARKER_ID))
										 .and(DATASETMEMBERS.DATASETMEMBERTYPE_ID.eq(1))
										 .and(DATASETS.IS_EXTERNAL.eq(false))
										 .and(DATASETS.DATASET_STATE_ID.eq(1))
										 .and(DATASETS.EXPERIMENT_ID.cast(String.class).eq(trialDbId))));
			}

			if (!StringUtils.isEmpty(mapPUI))
				step.where(MAPS.NAME.eq(mapPUI));

			List<Map> result = step.groupBy(MAPS.ID)
								   .limit(pageSize)
								   .offset(pageSize * currentPage)
								   .fetchInto(Map.class);

			long totalCount = context.fetchOne("SELECT FOUND_ROWS()").into(Long.class);
			return new BaseResult<>(new ArrayResult<Map>()
				.setData(result), currentPage, pageSize, totalCount);
		}
	}
}

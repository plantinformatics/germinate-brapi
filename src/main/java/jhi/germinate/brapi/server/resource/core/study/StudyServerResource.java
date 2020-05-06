package jhi.germinate.brapi.server.resource.core.study;

import org.jooq.*;
import org.jooq.impl.DSL;
import org.restlet.data.Status;
import org.restlet.resource.*;

import java.sql.Date;
import java.sql.*;
import java.util.*;

import jhi.germinate.brapi.resource.base.*;
import jhi.germinate.brapi.resource.study.Study;
import jhi.germinate.server.Database;
import jhi.germinate.server.auth.*;
import jhi.germinate.server.util.*;

import static jhi.germinate.server.database.tables.ViewTableDatasets.*;

/**
 * @author Sebastian Raubach
 */
public class StudyServerResource extends StudyBaseResource<ArrayResult<Study>>
{
	public static final String PARAM_CROP_COMMON_NAME           = "cropCommonName";
	public static final String PARAM_STUDY_TYPE                 = "studyType";
	public static final String PARAM_PROGRAM_DB_ID              = "programDbId";
	public static final String PARAM_LOCATION_DB_ID             = "locationDbId";
	public static final String PARAM_SEASON_DB_ID               = "seasonDbId";
	public static final String PARAM_TRIAL_DB_ID                = "trialDbId";
	public static final String PARAM_STUDY_DB_ID                = "studyDbId";
	public static final String PARAM_STUDY_NAME                 = "studyName";
	public static final String PARAM_STUDY_CODE                 = "studyCode";
	public static final String PARAM_STUDY_PUI                  = "studyPUI";
	public static final String PARAM_GERMPLASM_DB_ID            = "germplasmDbId";
	public static final String PARAM_OBSERVATION_VARIABLE_DB_ID = "observationVariableDbId";
	public static final String PARAM_ACTIVE                     = "active";

	private String  cropCommonName;
	private String  studyType;
	private String  programDbId;
	private String  locationDbId;
	private String  seasonDbId;
	private String  trialDbId;
	private String  studyDbId;
	private String  studyName;
	private String  studyCode;
	private String  studyPUI;
	private String  germplasmDbId;
	private String  observationVariableDbId;
	private Boolean active;

	@Override
	public void doInit()
	{
		super.doInit();

		this.cropCommonName = getQueryValue(PARAM_CROP_COMMON_NAME);
		this.studyType = getQueryValue(PARAM_STUDY_TYPE);
		this.programDbId = getQueryValue(PARAM_PROGRAM_DB_ID);
		this.locationDbId = getQueryValue(PARAM_LOCATION_DB_ID);
		this.seasonDbId = getQueryValue(PARAM_SEASON_DB_ID);
		this.trialDbId = getQueryValue(PARAM_TRIAL_DB_ID);
		this.studyDbId = getQueryValue(PARAM_STUDY_DB_ID);
		this.studyName = getQueryValue(PARAM_STUDY_NAME);
		this.studyCode = getQueryValue(PARAM_STUDY_CODE);
		this.studyPUI = getQueryValue(PARAM_STUDY_PUI);
		this.germplasmDbId = getQueryValue(PARAM_GERMPLASM_DB_ID);
		this.observationVariableDbId = getQueryValue(PARAM_OBSERVATION_VARIABLE_DB_ID);
		String isActive = getQueryValue(PARAM_ACTIVE);

		if (!StringUtils.isEmpty(isActive))
			this.active = Boolean.parseBoolean(isActive);
	}

	@Post
	@MinUserType(UserType.AUTH_USER)
	public BaseResult<ArrayResult<Study>> postJson(Study[] newStudies)
	{

		throw new ResourceException(Status.SERVER_ERROR_NOT_IMPLEMENTED);
	}

	@Override
	public BaseResult<ArrayResult<Study>> getJson()
	{
		try (Connection conn = Database.getConnection();
			 DSLContext context = Database.getContext(conn))
		{
			List<Condition> conditions = new ArrayList<>();

			if (!StringUtils.isEmpty(studyType))
				conditions.add(VIEW_TABLE_DATASETS.DATASET_TYPE.eq(studyType));
			if (!StringUtils.isEmpty(seasonDbId))
				conditions.add(DSL.year(VIEW_TABLE_DATASETS.START_DATE).cast(String.class).eq(seasonDbId));
			if (!StringUtils.isEmpty(trialDbId))
				conditions.add(VIEW_TABLE_DATASETS.EXPERIMENT_ID.cast(String.class).eq(trialDbId));
			if (!StringUtils.isEmpty(studyDbId))
				conditions.add(VIEW_TABLE_DATASETS.DATASET_ID.cast(String.class).eq(studyDbId));
			if (!StringUtils.isEmpty(studyName))
				conditions.add(VIEW_TABLE_DATASETS.DATASET_NAME.eq(studyName));
			if (active != null)
			{
				if (active)
					conditions.add(VIEW_TABLE_DATASETS.END_DATE.isNull().or(VIEW_TABLE_DATASETS.END_DATE.ge(new Date(System.currentTimeMillis()))));
				else
					conditions.add(VIEW_TABLE_DATASETS.END_DATE.isNotNull().and(VIEW_TABLE_DATASETS.END_DATE.le(new Date(System.currentTimeMillis()))));
			}

			List<Study> result = getStudies(context, conditions);

			long totalCount = context.fetchOne("SELECT FOUND_ROWS()").into(Long.class);
			return new BaseResult<>(new ArrayResult<Study>()
				.setData(result), currentPage, pageSize, totalCount);
		}
		catch (SQLException e)
		{
			e.printStackTrace();
			throw new ResourceException(Status.SERVER_ERROR_INTERNAL);
		}
	}
}

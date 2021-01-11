package jhi.germinate.brapi.server.resource.core.trial;

import org.jooq.*;
import org.jooq.impl.DSL;
import org.restlet.data.Status;
import org.restlet.resource.*;

import java.sql.Date;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.*;

import jhi.germinate.server.Database;
import jhi.germinate.server.util.StringUtils;
import uk.ac.hutton.ics.brapi.resource.base.*;
import uk.ac.hutton.ics.brapi.resource.core.trial.Trial;
import uk.ac.hutton.ics.brapi.server.core.trial.BrapiTrialServerResource;

import static jhi.germinate.server.database.codegen.tables.Datasets.*;
import static jhi.germinate.server.database.codegen.tables.ViewTableExperiments.*;

/**
 * @author Sebastian Raubach
 */
public class TrialServerResource extends TrialBaseServerResource implements BrapiTrialServerResource
{
	private static final String PARAM_ACTIVE                    = "active";
	private static final String PARAM_COMMON_CROP_NAME          = "commonCropName";
	private static final String PARAM_CONTACT_DB_ID             = "contactDbId";
	private static final String PARAM_PROGRAM_DB_ID             = "programDbId";
	private static final String PARAM_LOCATION_DB_ID            = "locationDbId";
	private static final String PARAM_SEARCH_DATE_RANGE_START   = "searchDateRangeStart";
	private static final String PARAM_SEARCH_DATE_RANGE_END     = "searchDateRangeEnd";
	private static final String PARAM_STUDY_DB_ID               = "studyDbId";
	private static final String PARAM_TRIAL_DB_ID               = "trialDbId";
	private static final String PARAM_TRIAL_NAME                = "trialName";
	private static final String PARAM_TRIAL_PUI                 = "trialPUI";
	private static final String PARAM_EXTERNAL_REFERENCE_ID     = "externalReferenceID";
	private static final String PARAM_EXTERNAL_REFERENCE_SOURCE = "externalReferenceSource";

	private Boolean active;
	private String  commonCropName;
	private String  contactDbId;
	private String  programDbId;
	private String  locationDbId;
	private Date    searchDateRangeStart;
	private Date    searchDateRangeEnd;
	private String  studyDbId;
	private String  trialDbId;
	private String  trialName;
	private String  trialPUI;
	private String  externalReferenceID;
	private String  externalReferenceSource;

	@Override
	public void doInit()
	{
		super.doInit();

		String isActive = getQueryValue(PARAM_ACTIVE);

		if (!StringUtils.isEmpty(isActive))
			this.active = Boolean.parseBoolean(isActive);

		this.commonCropName = getQueryValue(PARAM_COMMON_CROP_NAME);
		this.contactDbId = getQueryValue(PARAM_CONTACT_DB_ID);
		this.programDbId = getQueryValue(PARAM_PROGRAM_DB_ID);
		this.locationDbId = getQueryValue(PARAM_LOCATION_DB_ID);

		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		try
		{
			this.searchDateRangeStart = new Date(sdf.parse(getQueryValue(PARAM_SEARCH_DATE_RANGE_START)).getTime());
		}
		catch (Exception e)
		{
		}
		try
		{
			this.searchDateRangeEnd = new Date(sdf.parse(getQueryValue(PARAM_SEARCH_DATE_RANGE_END)).getTime());
		}
		catch (Exception e)
		{
		}
		this.studyDbId = getQueryValue(PARAM_STUDY_DB_ID);
		this.trialDbId = getQueryValue(PARAM_TRIAL_DB_ID);
		this.trialName = getQueryValue(PARAM_TRIAL_NAME);
		this.trialPUI = getQueryValue(PARAM_TRIAL_PUI);
		this.externalReferenceID = getQueryValue(PARAM_EXTERNAL_REFERENCE_ID);
		this.externalReferenceSource = getQueryValue(PARAM_EXTERNAL_REFERENCE_SOURCE);
	}

	@Get
	public BaseResult<ArrayResult<Trial>> getTrials()
	{
		try (DSLContext context = Database.getContext())
		{
			List<Condition> conditions = new ArrayList<>();

			if (searchDateRangeStart != null)
				conditions.add(VIEW_TABLE_EXPERIMENTS.EXPERIMENT_DATE.ge(searchDateRangeStart));
			if (searchDateRangeEnd != null)
				conditions.add(VIEW_TABLE_EXPERIMENTS.EXPERIMENT_DATE.le(searchDateRangeEnd));
			if (!StringUtils.isEmpty(studyDbId))
				conditions.add(DSL.exists(DSL.selectOne().from(DATASETS).where(DATASETS.EXPERIMENT_ID.eq(VIEW_TABLE_EXPERIMENTS.EXPERIMENT_ID)).and(DATASETS.ID.cast(String.class).eq(studyDbId))));
			if (!StringUtils.isEmpty(trialDbId))
				conditions.add(VIEW_TABLE_EXPERIMENTS.EXPERIMENT_ID.cast(String.class).eq(trialDbId));
			if (!StringUtils.isEmpty(trialName))
				conditions.add(VIEW_TABLE_EXPERIMENTS.EXPERIMENT_NAME.eq(trialName));

			List<Trial> result = getTrials(context, conditions);

			long totalCount = context.fetchOne("SELECT FOUND_ROWS()").into(Long.class);
			return new BaseResult<>(new ArrayResult<Trial>()
				.setData(result), currentPage, pageSize, totalCount);
		}
	}

	@Post
	public BaseResult<ArrayResult<Trial>> postTrials(Trial[] trials)
	{
		throw new ResourceException(Status.SERVER_ERROR_NOT_IMPLEMENTED);
	}
}

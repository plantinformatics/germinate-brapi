package jhi.germinate.brapi.server.resource.genotyping.variant;

import org.jooq.*;
import org.jooq.impl.DSL;
import org.restlet.data.Status;
import org.restlet.resource.ResourceException;

import java.sql.*;
import java.util.*;

import jhi.germinate.brapi.resource.base.ArrayResult;
import jhi.germinate.brapi.resource.base.BaseResult;
import jhi.germinate.brapi.resource.variant.VariantSet;
import jhi.germinate.server.Database;
import jhi.germinate.server.util.StringUtils;

import static jhi.germinate.server.database.tables.Datasetmembers.*;
import static jhi.germinate.server.database.tables.Datasets.*;

/**
 * @author Sebastian Raubach
 */
public class VariantSetServerResource extends VariantSetBaseServerResource<ArrayResult<VariantSet>>
{
	public static final String PARAM_VARIANT_SET_DB_ID = "variantSetDbId";
	public static final String PARAM_VARIANT_DB_ID     = "variantDbId";
	public static final String PARAM_CALL_SET_DB_ID    = "callSetDbId";
	public static final String PARAM_STUDY_DB_ID       = "studyDbId";
	public static final String PARAM_STUDY_NAME        = "studyName";

	private String variantSetDbId;
	private String variantDbId;
	private String callSetDbId;
	private String studyDbId;
	private String studyName;

	@Override
	public void doInit()
	{
		super.doInit();

		this.variantSetDbId = getQueryValue(PARAM_VARIANT_SET_DB_ID);
		this.variantDbId = getQueryValue(PARAM_VARIANT_DB_ID);
		this.callSetDbId = getQueryValue(PARAM_CALL_SET_DB_ID);
		this.studyDbId = getQueryValue(PARAM_STUDY_DB_ID);
		this.studyName = getQueryValue(PARAM_STUDY_NAME);
	}

	@Override
	public BaseResult<ArrayResult<VariantSet>> getJson()
	{
		try (Connection conn = Database.getConnection();
			 DSLContext context = Database.getContext(conn))
		{
			List<Condition> conditions = new ArrayList<>();

			if (!StringUtils.isEmpty(variantSetDbId))
				conditions.add(DATASETS.ID.cast(String.class).eq(variantSetDbId));
			if (!StringUtils.isEmpty(variantDbId))
				conditions.add(DSL.exists(DSL.selectOne().from(DATASETMEMBERS).where(DATASETMEMBERS.DATASET_ID.eq(DATASETS.ID)).and(DATASETMEMBERS.DATASETMEMBERTYPE_ID.eq(1)).and(DATASETMEMBERS.FOREIGN_ID.cast(String.class).eq(variantDbId))));
			if (!StringUtils.isEmpty(callSetDbId))
				conditions.add(DSL.exists(DSL.selectOne().from(DATASETMEMBERS).where(DATASETMEMBERS.DATASET_ID.eq(DATASETS.ID)).and(DATASETMEMBERS.DATASETMEMBERTYPE_ID.eq(2)).and(DATASETMEMBERS.FOREIGN_ID.cast(String.class).eq(callSetDbId))));
			if (!StringUtils.isEmpty(studyDbId))
				conditions.add(DATASETS.ID.cast(String.class).eq(studyDbId));
			if (!StringUtils.isEmpty(studyName))
				conditions.add(DATASETS.NAME.cast(String.class).eq(studyName));

			List<VariantSet> result = getVariantSets(context, conditions);

			long totalCount = context.fetchOne("SELECT FOUND_ROWS()").into(Long.class);
			return new BaseResult<>(new ArrayResult<VariantSet>()
				.setData(result), currentPage, pageSize, totalCount);
		}
		catch (SQLException e)
		{
			e.printStackTrace();
			throw new ResourceException(Status.SERVER_ERROR_INTERNAL);
		}
	}
}

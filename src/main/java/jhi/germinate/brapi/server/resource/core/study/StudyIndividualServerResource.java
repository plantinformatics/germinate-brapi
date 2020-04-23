package jhi.germinate.brapi.server.resource.core.study;

import org.jooq.DSLContext;
import org.restlet.data.Status;
import org.restlet.resource.*;

import java.sql.*;
import java.util.*;

import jhi.germinate.brapi.resource.study.StudyResult;
import jhi.germinate.brapi.resource.base.BaseResult;
import jhi.germinate.server.Database;
import jhi.germinate.server.auth.*;
import jhi.germinate.server.util.CollectionUtils;

import static jhi.germinate.server.database.tables.ViewTableDatasets.*;

/**
 * @author Sebastian Raubach
 */
public class StudyIndividualServerResource extends StudyBaseResource<StudyResult>
{
	private String studyDbId;

	@Override
	public void doInit()
	{
		super.doInit();

		try
		{
			this.studyDbId = getRequestAttributes().get("studyDbId").toString();
		}
		catch (Exception e)
		{
		}
	}

	@Put
	@MinUserType(UserType.DATA_CURATOR)
	public BaseResult<StudyResult> putJson(StudyResult newStudy)
	{
		throw new ResourceException(Status.SERVER_ERROR_NOT_IMPLEMENTED);
	}

	@Override
	public BaseResult<StudyResult> getJson()
	{
		try (Connection conn = Database.getConnection();
			 DSLContext context = Database.getContext(conn))
		{
			List<StudyResult> results = getStudies(context, Collections.singletonList(VIEW_TABLE_DATASETS.DATASET_ID.cast(String.class).eq(studyDbId)));
			StudyResult result = CollectionUtils.isEmpty(results) ? null : results.get(0);

			long totalCount = context.fetchOne("SELECT FOUND_ROWS()").into(Long.class);
			return new BaseResult<>(result, currentPage, pageSize, totalCount);
		}
		catch (SQLException e)
		{
			e.printStackTrace();
			throw new ResourceException(Status.SERVER_ERROR_INTERNAL);
		}
	}
}

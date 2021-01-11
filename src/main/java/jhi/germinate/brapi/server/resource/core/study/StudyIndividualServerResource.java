package jhi.germinate.brapi.server.resource.core.study;

import org.jooq.DSLContext;
import org.restlet.data.Status;
import org.restlet.resource.*;

import java.sql.*;
import java.util.*;

import jhi.germinate.server.Database;
import jhi.germinate.server.auth.*;
import jhi.germinate.server.util.CollectionUtils;
import uk.ac.hutton.ics.brapi.resource.base.BaseResult;
import uk.ac.hutton.ics.brapi.resource.core.study.Study;
import uk.ac.hutton.ics.brapi.server.core.study.BrapiStudyIndividualServerResource;

import static jhi.germinate.server.database.codegen.tables.ViewTableDatasets.*;

/**
 * @author Sebastian Raubach
 */
public class StudyIndividualServerResource extends StudyBaseResource implements BrapiStudyIndividualServerResource
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
	public BaseResult<Study> putStudyById(Study newStudy)
	{
		throw new ResourceException(Status.SERVER_ERROR_NOT_IMPLEMENTED);
	}

	@Get
	public BaseResult<Study> getStudyById()
	{
		try (DSLContext context = Database.getContext())
		{
			List<Study> results = getStudies(context, Collections.singletonList(VIEW_TABLE_DATASETS.DATASET_ID.cast(String.class).eq(studyDbId)));
			Study result = CollectionUtils.isEmpty(results) ? null : results.get(0);

			return new BaseResult<>(result, currentPage, pageSize, 1);
		}
	}
}

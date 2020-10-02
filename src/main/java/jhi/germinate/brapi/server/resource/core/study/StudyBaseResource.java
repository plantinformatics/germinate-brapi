package jhi.germinate.brapi.server.resource.core.study;

import org.jooq.*;

import java.util.*;
import java.util.stream.Collectors;

import jhi.germinate.brapi.server.util.DateUtils;
import jhi.germinate.server.database.codegen.tables.pojos.ViewTableDatasets;
import jhi.germinate.server.util.*;
import uk.ac.hutton.ics.brapi.resource.base.*;
import uk.ac.hutton.ics.brapi.resource.core.study.Study;
import uk.ac.hutton.ics.brapi.server.base.BaseServerResource;

import static jhi.germinate.server.database.codegen.tables.ViewTableCollaborators.*;
import static jhi.germinate.server.database.codegen.tables.ViewTableDatasets.*;

/**
 * @author Sebastian Raubach
 */
public abstract class StudyBaseResource extends BaseServerResource
{
	protected List<Study> getStudies(DSLContext context, List<Condition> conditions)
	{
		Map<Integer, List<Contact>> collaborators = new HashMap<>();

		context.selectFrom(VIEW_TABLE_COLLABORATORS)
			   .forEach(r -> {
				   Integer id = r.getDatasetId();

				   List<Contact> list = collaborators.get(id);

				   if (list == null)
					   list = new ArrayList<>();

				   list.add(new Contact()
					   .setContactDbId(Integer.toString(r.getCollaboratorId()))
					   .setEmail(r.getCollaboratorEmail())
					   .setInstituteName(r.getInstitutionName())
					   .setName(StringUtils.join(" ", r.getCollaboratorFirstName(), r.getCollaboratorLastName())));

				   collaborators.put(id, list);
			   });

		SelectConditionStep<?> step = context.select()
											 .hint("SQL_CALC_FOUND_ROWS")
											 .from(VIEW_TABLE_DATASETS)
											 .where(VIEW_TABLE_DATASETS.DATASET_STATE.eq("public")) // TODO: Check user permissions
											 .and(VIEW_TABLE_DATASETS.IS_EXTERNAL.eq(false));

		if (conditions != null)
		{
			for (Condition condition : conditions)
				step.and(condition);
		}


		List<ViewTableDatasets> datasets = step.limit(pageSize)
											   .offset(pageSize * currentPage)
											   .fetchInto(ViewTableDatasets.class);

		return datasets.stream()
					   .map(r -> new Study()
						   .setActive(r.getEndDate() != null && r.getEndDate().getTime() < System.currentTimeMillis())
						   .setContacts(collaborators.get(r.getDatasetId()))
						   .setEndDate(DateUtils.getTimestamp(r.getEndDate()))
						   .setLastUpdate(new LastUpdate()
							   .setTimestamp(r.getUpdatedOn())
							   .setVersion(r.getVersion()))
						   .setLicense(r.getLicenseName())
						   .setLocationDbId(!CollectionUtils.isEmpty(r.getLocations()) ? Integer.toString(r.getLocations()[0].getLocationId()) : null)
						   .setLocationName(!CollectionUtils.isEmpty(r.getLocations()) ? r.getLocations()[0].getLocationName() : null)
						   .setStartDate(DateUtils.getTimestamp(r.getStartDate()))
						   .setStudyDbId(Integer.toString(r.getDatasetId()))
						   .setStudyDescription(r.getDatasetDescription())
						   .setStudyName(r.getDatasetName())
						   .setStudyType(r.getDatasetType())
						   .setTrialDbId(Integer.toString(r.getExperimentId()))
						   .setTrialName(r.getExperimentName())
					   )
					   .collect(Collectors.toList());
	}
}

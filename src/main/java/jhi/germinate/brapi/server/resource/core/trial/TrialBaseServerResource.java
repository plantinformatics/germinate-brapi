package jhi.germinate.brapi.server.resource.core.trial;

import org.jooq.*;
import org.jooq.impl.DSL;

import java.util.*;
import java.util.stream.Collectors;

import jhi.germinate.brapi.resource.Contact;
import jhi.germinate.brapi.resource.trial.*;
import jhi.germinate.brapi.server.resource.BaseServerResource;
import jhi.germinate.server.database.tables.pojos.ViewTableExperiments;
import jhi.germinate.server.util.StringUtils;

import static jhi.germinate.server.database.tables.Datasets.*;
import static jhi.germinate.server.database.tables.Experiments.*;
import static jhi.germinate.server.database.tables.ViewTableCollaborators.*;
import static jhi.germinate.server.database.tables.ViewTableExperiments.*;
import static jhi.germinate.server.database.tables.ViewTableLicenses.*;

/**
 * @author Sebastian Raubach
 */
public abstract class TrialBaseServerResource<T> extends BaseServerResource<T>
{
	protected List<Trial> getTrials(DSLContext context, List<Condition> conditions)
	{
		Map<Integer, List<Contact>> collaborators = new HashMap<>();
		context.select()
			   .from(VIEW_TABLE_COLLABORATORS)
			   .leftJoin(DATASETS).on(DATASETS.ID.eq(VIEW_TABLE_COLLABORATORS.DATASET_ID))
			   .leftJoin(EXPERIMENTS).on(EXPERIMENTS.ID.eq(DATASETS.EXPERIMENT_ID))
			   .forEach(r -> {
				   Integer id = r.get(EXPERIMENTS.ID);

				   List<Contact> list = collaborators.get(id);

				   if (list == null)
					   list = new ArrayList<>();

				   list.add(new Contact()
					   .setContactDbId(Integer.toString(r.get(VIEW_TABLE_COLLABORATORS.COLLABORATOR_ID)))
					   .setEmail(r.get(VIEW_TABLE_COLLABORATORS.COLLABORATOR_EMAIL))
					   .setInstituteName(r.get(VIEW_TABLE_COLLABORATORS.INSTITUTION_NAME))
					   .setName(StringUtils.join(" ", r.get(VIEW_TABLE_COLLABORATORS.COLLABORATOR_FIRST_NAME), r.get(VIEW_TABLE_COLLABORATORS.COLLABORATOR_LAST_NAME))));

				   collaborators.put(id, list);
			   });
		// Get all licenses mapped to the experiment
		Map<Integer, List<Authorship>> authorship = new HashMap<>();
		context.select()
			   .from(VIEW_TABLE_LICENSES)
			   .leftJoin(DATASETS).on(DATASETS.ID.eq(VIEW_TABLE_LICENSES.DATASET_ID))
			   .leftJoin(EXPERIMENTS).on(EXPERIMENTS.ID.eq(DATASETS.EXPERIMENT_ID))
			   .forEach(r -> {
				   Integer id = r.get(EXPERIMENTS.ID);

				   List<Authorship> list = authorship.get(id);

				   if (list == null)
					   list = new ArrayList<>();

				   list.add(new Authorship()
					   .setLicense(r.get(VIEW_TABLE_LICENSES.LICENSE_NAME)));
			   });

		SelectConditionStep<?> step = context.select()
											 .hint("SQL_CALC_FOUND_ROWS")
											 .from(VIEW_TABLE_EXPERIMENTS)
											 .where(DSL.exists(DSL.selectOne().from(DATASETS).where(DATASETS.EXPERIMENT_ID.eq(VIEW_TABLE_EXPERIMENTS.EXPERIMENT_ID).and(DATASETS.DATASET_STATE_ID.eq(1)))));

		if (conditions != null)
		{
			for (Condition condition : conditions)
				step.and(condition);
		}

		List<ViewTableExperiments> datasets = step.limit(pageSize)
												  .offset(pageSize * currentPage)
												  .fetchInto(ViewTableExperiments.class);

		return datasets.stream()
					   .map(r -> new Trial()
						   .setActive(false)
						   .setContacts(collaborators.get(r.getExperimentId()))
						   .setDatasetAuthorships(authorship.get(r.getExperimentId()))
						   .setEndDate(getSimpleDate(r.getExperimentDate()))
						   .setStartDate(getSimpleDate(r.getExperimentDate()))
						   .setTrialDbId(Integer.toString(r.getExperimentId()))
						   .setTrialDescription(r.getExperimentDescription())
						   .setTrialName(r.getExperimentName())
					   )
					   .collect(Collectors.toList());
	}
}

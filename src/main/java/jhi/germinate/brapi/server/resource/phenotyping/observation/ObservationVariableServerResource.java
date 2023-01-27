package jhi.germinate.brapi.server.resource.phenotyping.observation;

import jakarta.annotation.security.PermitAll;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import jhi.germinate.resource.enums.UserType;
import jhi.germinate.server.Database;
import jhi.germinate.server.database.codegen.enums.PhenotypesDatatype;
import jhi.germinate.server.database.codegen.tables.records.*;
import jhi.germinate.server.database.pojo.TraitRestrictions;
import jhi.germinate.server.util.*;
import org.jooq.*;
import org.jooq.impl.DSL;
import uk.ac.hutton.ics.brapi.resource.base.*;
import uk.ac.hutton.ics.brapi.resource.germplasm.attribute.*;
import uk.ac.hutton.ics.brapi.resource.phenotyping.observation.ObservationVariable;
import uk.ac.hutton.ics.brapi.server.base.BaseServerResource;
import uk.ac.hutton.ics.brapi.server.phenotyping.observation.BrapiObservationVariableServerResource;

import java.io.IOException;
import java.sql.*;
import java.util.*;
import java.util.stream.Collectors;

import static jhi.germinate.server.database.codegen.tables.Datasets.*;
import static jhi.germinate.server.database.codegen.tables.Phenotypedata.*;
import static jhi.germinate.server.database.codegen.tables.Phenotypes.*;
import static jhi.germinate.server.database.codegen.tables.Units.*;

@Path("brapi/v2/variables")
public class ObservationVariableServerResource extends BaseServerResource implements BrapiObservationVariableServerResource
{
	@Override
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Secured(UserType.DATA_CURATOR)
	public BaseResult<ArrayResult<ObservationVariable>> postObservationVariables(List<ObservationVariable> newVariables)
		throws IOException, SQLException
	{
		// If there are no new variables, or any of them don't have a trait or any of them have a scale without name or data type, return BAD_REQUEST
		if (CollectionUtils.isEmpty(newVariables) || newVariables.stream().anyMatch(ov -> ov.getTrait() == null || StringUtils.isEmpty(ov.getTrait().getTraitName()) || (ov.getScale() != null && (StringUtils.isEmpty(ov.getScale().getDataType()) || StringUtils.isEmpty(ov.getScale().getScaleName())))))
		{
			resp.sendError(Response.Status.BAD_REQUEST.getStatusCode());
			return null;
		}

		List<Integer> ids = new ArrayList<>();

		try (Connection conn = Database.getConnection())
		{
			DSLContext context = Database.getContext(conn);

			for (ObservationVariable ov : newVariables)
			{
				PhenotypesDatatype dataType;

				switch (ov.getScale().getDataType())
				{
					case "Date":
						dataType = PhenotypesDatatype.date;
						break;
					case "Numeric":
						dataType = PhenotypesDatatype.numeric;
						break;
					case "Ordinal":
						dataType = PhenotypesDatatype.categorical;
						break;
					case "Text":
					default:
						dataType = PhenotypesDatatype.text;
						break;
				}

				UnitsRecord unit = ov.getScale() != null ? context.selectFrom(UNITS)
																  .where(UNITS.UNIT_NAME.eq(ov.getScale().getScaleName()))
																  .fetchAny() : null;
				PhenotypesRecord trait = context.selectFrom(PHENOTYPES)
												.where(PHENOTYPES.NAME.eq(ov.getTrait().getTraitName()))
												.and(PHENOTYPES.UNIT_ID.isNotDistinctFrom(unit == null ? null : unit.getId()))
												.and(PHENOTYPES.DATATYPE.eq(dataType))
												.fetchAny();

				if (ov.getScale() != null && unit == null)
				{
					unit = context.newRecord(UNITS);
					unit.setUnitName(ov.getScale().getScaleName());
					unit.store();
				}

				if (trait == null)
				{
					trait = context.newRecord(PHENOTYPES);
					trait.setName(ov.getTrait().getTraitName());
					trait.setDatatype(dataType);

					if (ov.getScale() != null && ov.getScale().getValidValues() != null) {
						ValidValues vv = ov.getScale().getValidValues();
						TraitRestrictions tr = new TraitRestrictions();

						if (!StringUtils.isEmpty(vv.getMinimumValue())) {
							try {
								tr.setMin(Double.parseDouble(vv.getMinimumValue()));
							} catch (NumberFormatException e) {
								// Ignore this, invalid value
							}
						}
						if (!StringUtils.isEmpty(vv.getMaximumValue())) {
							try {
								tr.setMax(Double.parseDouble(vv.getMaximumValue()));
							} catch (NumberFormatException e) {
								// Ignore this, invalid value
							}
						}
						if (!CollectionUtils.isEmpty(vv.getCategories())) {
							tr.setCategories(new String[][] { vv.getCategories().stream().map(Category::getValue).toArray(String[]::new) });
						}

						trait.setRestrictions(tr);
					}

					if (unit != null)
						trait.setUnitId(unit.getId());

					trait.store();
				}

				ids.add(trait.getId());
			}

			return getVariables(context, Collections.singletonList(PHENOTYPEDATA.ID.in(ids)));
		}
	}

	@Override
	@GET
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Secured
	@PermitAll
	public BaseResult<ArrayResult<ObservationVariable>> getObservationVariables(
		@QueryParam("observationVariableDbId") String observationVariableDbId,
		@QueryParam("observationVariableName") String observationVariableName,
		@QueryParam("observationVariablePUI") String observationVariablePUI,
		@QueryParam("traitClass") String traitClass,
		@QueryParam("methodDbId") String methodDbId,
		@QueryParam("methodName") String methodName,
		@QueryParam("methodPUI") String methodPUI,
		@QueryParam("scaleDbId") String scaleDbId,
		@QueryParam("scaleName") String scaleName,
		@QueryParam("scalePUI") String scalePUI,
		@QueryParam("traitDbId") String traitDbId,
		@QueryParam("traitName") String traitName,
		@QueryParam("traitPUI") String traitPUI,
		@QueryParam("ontologyDbId") String ontologyDbId,
		@QueryParam("commonCropName") String commonCropName,
		@QueryParam("programDbId") String programDbId,
		@QueryParam("trialDbId") String trialDbId,
		@QueryParam("studyDbId") String studyDbId,
		@QueryParam("externalReferenceId") String externalReferenceId,
		@QueryParam("externalReferenceSource") String externalReferenceSource)
		throws IOException, SQLException
	{
		try (Connection conn = Database.getConnection())
		{
			DSLContext context = Database.getContext(conn);

			List<Condition> conditions = new ArrayList<>();

			if (!StringUtils.isEmpty(traitDbId))
			{
				try
				{
					Integer traitId = Integer.parseInt(traitDbId);
					conditions.add(PHENOTYPES.ID.eq(traitId));
				}
				catch (NumberFormatException e)
				{
					// Ignore this. Invalid id specified.
				}
			}
			if (!StringUtils.isEmpty(traitName))
				conditions.add(PHENOTYPES.NAME.eq(traitName));
			if (!StringUtils.isEmpty(studyDbId))
			{
				try
				{
					Integer datasetId = Integer.parseInt(studyDbId);
					conditions.add(DSL.exists(DSL.selectOne().from(PHENOTYPEDATA).where(PHENOTYPEDATA.DATASET_ID.eq(datasetId)).and(PHENOTYPEDATA.PHENOTYPE_ID.eq(PHENOTYPES.ID))));
				}
				catch (NumberFormatException e)
				{
					// Ignore this. Invalid id specified.
				}
			}
			if (!StringUtils.isEmpty(trialDbId))
			{
				try
				{
					Integer experimentId = Integer.parseInt(studyDbId);
					conditions.add(DSL.exists(DSL.selectOne().from(PHENOTYPEDATA).leftJoin(DATASETS).on(DATASETS.ID.eq(PHENOTYPEDATA.DATASET_ID)).where(DATASETS.EXPERIMENT_ID.eq(experimentId)).and(PHENOTYPEDATA.PHENOTYPE_ID.eq(PHENOTYPES.ID))));
				}
				catch (NumberFormatException e)
				{
					// Ignore this. Invalid id specified.
				}
			}

			return getVariables(context, conditions);
		}
	}

	private BaseResult<ArrayResult<ObservationVariable>> getVariables(DSLContext context, List<Condition> conditions)
	{
		SelectOnConditionStep<Record> step = context.select()
													.from(PHENOTYPES)
													.leftJoin(UNITS).on(UNITS.ID.eq(PHENOTYPES.UNIT_ID));

		if (!CollectionUtils.isEmpty(conditions))
			conditions.forEach(c -> step.where(conditions));

		List<ObservationVariable> variables = step.limit(pageSize)
												  .offset(pageSize * page)
												  .stream()
												  .map(t -> {
													  ObservationVariable variable = new ObservationVariable().setObservationVariableDbId(Integer.toString(t.get(PHENOTYPES.ID)))
																											  .setObservationVariableName(t.get(PHENOTYPES.NAME));

													  PhenotypesDatatype dataType = t.get(PHENOTYPES.DATATYPE);
													  TraitRestrictions restrictions = t.get(PHENOTYPES.RESTRICTIONS);
													  Integer unitId = t.get(UNITS.ID);
													  String unitName = t.get(UNITS.UNIT_NAME);
													  Scale scale = new Scale();

													  switch (dataType)
													  {
														  case date:
															  scale.setDataType("Date");
															  break;
														  case numeric:
															  scale.setDataType("Numeric");
															  break;
														  case categorical:
															  scale.setDataType("Ordinal");
															  break;
														  case text:
														  default:
															  scale.setDataType("Text");
													  }

													  if (unitId != null && !StringUtils.isEmpty(unitName))
													  {
														  scale.setScaleDbId(Integer.toString(unitId))
															   .setScaleName(unitName);

														  if (restrictions != null)
														  {
															  ValidValues vv = new ValidValues();

															  if (restrictions.getCategories() != null)
															  {
																  List<Category> categories = new ArrayList<>();

																  for (String[] cats : restrictions.getCategories())
																  {
																	  for (String value : cats)
																	  {
																		  categories.add(new Category().setValue(value).setLabel(value));
																	  }
																  }

																  vv.setCategories(categories);
															  }
															  if (restrictions.getMin() != null)
																  vv.setMinimumValue(Integer.toString((int) Math.floor(restrictions.getMin())));
															  if (restrictions.getMax() != null)
																  vv.setMaximumValue(Integer.toString((int) Math.ceil(restrictions.getMax())));

															  scale.setValidValues(vv);
														  }
													  }

													  variable.setScale(scale);

													  return variable;
												  }).collect(Collectors.toList());

		long totalCount = context.fetchOne("SELECT FOUND_ROWS()").into(Long.class);
		return new BaseResult<>(new ArrayResult<ObservationVariable>().setData(variables), page, pageSize, totalCount);
	}

	@GET
	@Path("/{observationVariableDbId}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Secured
	@PermitAll
	public BaseResult<ObservationVariable> getObservationVariableById(@PathParam("observationVariableDbId") String observationVariableDbId)
		throws IOException, SQLException
	{
		resp.sendError(Response.Status.NOT_IMPLEMENTED.getStatusCode());
		return null;
	}

	@PUT
	@Path("/{observationVariableDbId}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Secured(UserType.DATA_CURATOR)
	public BaseResult<ObservationVariable> putObservationVariableById(@PathParam("observationVariableDbId") String observationVariableDbId, ObservationVariable observationVariable)
		throws IOException, SQLException
	{
		resp.sendError(Response.Status.NOT_IMPLEMENTED.getStatusCode());
		return null;
	}
}

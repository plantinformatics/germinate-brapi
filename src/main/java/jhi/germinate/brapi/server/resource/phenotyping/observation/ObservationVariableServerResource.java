package jhi.germinate.brapi.server.resource.phenotyping.observation;

import jhi.germinate.server.Database;
import jhi.germinate.server.database.codegen.enums.PhenotypesDatatype;
import jhi.germinate.server.database.pojo.*;
import jhi.germinate.server.util.StringUtils;
import org.jooq.DSLContext;
import org.restlet.data.Status;
import org.restlet.resource.*;
import uk.ac.hutton.ics.brapi.resource.base.*;
import uk.ac.hutton.ics.brapi.resource.germplasm.attribute.*;
import uk.ac.hutton.ics.brapi.resource.phenotyping.observation.*;
import uk.ac.hutton.ics.brapi.server.base.BaseServerResource;
import uk.ac.hutton.ics.brapi.server.phenotyping.observation.*;

import java.sql.*;
import java.util.*;
import java.util.stream.Collectors;

import static jhi.germinate.server.database.codegen.tables.Phenotypes.*;
import static jhi.germinate.server.database.codegen.tables.Units.*;

public class ObservationVariableServerResource extends BaseServerResource implements BrapiObservationVariableServerResource
{
	@Get
	@Override
	public BaseResult<ArrayResult<ObservationVariable>> getObservationVariables()
	{
		try (Connection conn = Database.getConnection();
			 DSLContext context = Database.getContext(conn))
		{
			List<ObservationVariable> variables = context.select()
														 .from(PHENOTYPES)
														 .leftJoin(UNITS).on(UNITS.ID.eq(PHENOTYPES.UNIT_ID))
														 .limit(pageSize)
														 .offset(pageSize * currentPage)
														 .stream()
														 .map(t -> {
															 ObservationVariable variable = new ObservationVariable()
																 .setObservationVariableDbId(Integer.toString(t.get(PHENOTYPES.ID)))
																 .setObservationVariableName(t.get(PHENOTYPES.NAME));

															 PhenotypesDatatype dataType = t.get(PHENOTYPES.DATATYPE);
															 TraitRestrictions restrictions = t.get(PHENOTYPES.RESTRICTIONS);
															 Integer unitId = t.get(UNITS.ID);
															 String unitName = t.get(UNITS.UNIT_NAME);

															 if (unitId != null && !StringUtils.isEmpty(unitName))
															 {
																 Scale scale = new Scale()
																	 .setScaleDbId(Integer.toString(unitId))
																	 .setScaleName(unitName);

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
																				 categories.add(new Category()
																					 .setValue(value)
																					 .setLabel(value));
																			 }
																		 }

																		 vv.setCategories(categories);
																	 }
																	 if (restrictions.getMin() != null)
																	 {
																		 vv.setMin((int) Math.floor(restrictions.getMin()));
																	 }
																	 if (restrictions.getMin() != null)
																	 {
																		 vv.setMax((int) Math.ceil(restrictions.getMax()));
																	 }

																	 scale.setValidValues(vv);
																 }

																 variable.setScale(scale);
															 }

															 return variable;
														 })
														 .collect(Collectors.toList());

			long totalCount = context.fetchOne("SELECT FOUND_ROWS()").into(Long.class);
			return new BaseResult<>(new ArrayResult<ObservationVariable>()
				.setData(variables), currentPage, pageSize, totalCount);
		}
		catch (SQLException e)
		{
			e.printStackTrace();
			throw new ResourceException(Status.SERVER_ERROR_INTERNAL);
		}
	}

	@Post
	@Override
	public BaseResult<ArrayResult<ObservationVariable>> postObservationVariables(List<ObservationVariable> list)
	{
		throw new ResourceException(Status.SERVER_ERROR_NOT_IMPLEMENTED);
	}
}

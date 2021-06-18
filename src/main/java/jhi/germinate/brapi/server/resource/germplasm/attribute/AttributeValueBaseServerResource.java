package jhi.germinate.brapi.server.resource.germplasm.attribute;

import org.jooq.*;

import java.util.List;

import jhi.germinate.server.util.CollectionUtils;
import uk.ac.hutton.ics.brapi.resource.germplasm.attribute.AttributeValue;
import uk.ac.hutton.ics.brapi.server.base.BaseServerResource;

import static jhi.germinate.server.database.codegen.tables.ViewTableGermplasmAttributes.*;

/**
 * @author Sebastian Raubach
 */
public abstract class AttributeValueBaseServerResource extends BaseServerResource
{
	protected List<AttributeValue> getAttributeValues(DSLContext context, List<Condition> conditions)
	{
		SelectJoinStep<?> step = context.select(
			VIEW_TABLE_GERMPLASM_ATTRIBUTES.ATTRIBUTE_ID.as("attributeDbId"),
			VIEW_TABLE_GERMPLASM_ATTRIBUTES.ATTRIBUTE_NAME.as("attributeName"),
			VIEW_TABLE_GERMPLASM_ATTRIBUTES.ATTRIBUTE_VALUE_ID.as("attributeValueDbId"),
			VIEW_TABLE_GERMPLASM_ATTRIBUTES.CREATED_ON.as("determinedDate"),
			VIEW_TABLE_GERMPLASM_ATTRIBUTES.GERMPLASM_ID.as("germplasmDbId"),
			VIEW_TABLE_GERMPLASM_ATTRIBUTES.GERMPLASM_NAME.as("germplasmName"),
			VIEW_TABLE_GERMPLASM_ATTRIBUTES.ATTRIBUTE_VALUE.as("value")
		)
										.hint("SQL_CALC_FOUND_ROWS")
										.from(VIEW_TABLE_GERMPLASM_ATTRIBUTES);

		if (!CollectionUtils.isEmpty(conditions))
		{
			for (Condition condition : conditions)
				step.where(condition);
		}

		return step.limit(pageSize)
				   .offset(pageSize * page)
				   .fetchInto(AttributeValue.class);
	}
}
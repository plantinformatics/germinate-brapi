package jhi.germinate.brapi.server.resource.germplasm.attribute;

import org.jooq.*;

import java.util.List;

import jhi.germinate.brapi.resource.attribute.AttributeValue;
import jhi.germinate.brapi.server.resource.BaseServerResource;
import jhi.germinate.server.util.CollectionUtils;

import static jhi.germinate.server.database.tables.ViewTableGermplasmAttributes.*;

/**
 * @author Sebastian Raubach
 */
public abstract class AttributeValueBaseServerResource<T> extends BaseServerResource<T>
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
				   .offset(pageSize * currentPage)
				   .fetchInto(AttributeValue.class);
	}
}

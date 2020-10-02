package jhi.germinate.brapi.server.resource.germplasm.attribute;

import org.jooq.*;
import org.jooq.impl.DSL;

import java.util.List;

import jhi.germinate.server.util.CollectionUtils;
import uk.ac.hutton.ics.brapi.resource.germplasm.attribute.Attribute;
import uk.ac.hutton.ics.brapi.server.base.BaseServerResource;

import static jhi.germinate.server.database.codegen.tables.Attributes.*;

/**
 * @author Sebastian Raubach
 */
public abstract class AttributeBaseServerResource extends BaseServerResource
{
	protected List<Attribute> getAttributes(DSLContext context, List<Condition> conditions)
	{
		SelectConditionStep<?> step = context.select(
			ATTRIBUTES.ID.as("attributeDbId"),
			ATTRIBUTES.NAME.as("attributeName"),
			ATTRIBUTES.DESCRIPTION.as("attributeDescription"),
			DSL.inline("en").as("language"),
			ATTRIBUTES.CREATED_ON.as("submissionTimestamp")
		)
											 .hint("SQL_CALC_FOUND_ROWS")
											 .from(ATTRIBUTES)
											 .where(ATTRIBUTES.TARGET_TABLE.eq("germinatebase"));

		if (!CollectionUtils.isEmpty(conditions))
		{
			for (Condition condition : conditions)
				step.and(condition);
		}

		return step.limit(pageSize)
				   .offset(pageSize * currentPage)
				   .fetchInto(Attribute.class);
	}
}

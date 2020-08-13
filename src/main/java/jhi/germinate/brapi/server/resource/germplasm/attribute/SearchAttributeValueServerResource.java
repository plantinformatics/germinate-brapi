package jhi.germinate.brapi.server.resource.germplasm.attribute;

import org.jooq.*;
import org.restlet.data.Status;
import org.restlet.resource.*;

import java.sql.*;
import java.util.*;

import jhi.germinate.server.Database;
import jhi.germinate.server.util.CollectionUtils;
import uk.ac.hutton.ics.brapi.resource.base.*;
import uk.ac.hutton.ics.brapi.resource.germplasm.attribute.*;
import uk.ac.hutton.ics.brapi.server.germplasm.attribute.BrapiSearchAttributeValueServerResource;

import static jhi.germinate.server.database.tables.ViewTableGermplasmAttributes.*;

/**
 * @author Sebastian Raubach
 */
public class SearchAttributeValueServerResource extends AttributeValueBaseServerResource implements BrapiSearchAttributeValueServerResource
{
	@Post
	public BaseResult<ArrayResult<AttributeValue>> postAttributeValueSearch(AttributeValueSearch search)
	{
		try (Connection conn = Database.getConnection();
			 DSLContext context = Database.getContext(conn))
		{
			List<Condition> conditions = new ArrayList<>();

			if (!CollectionUtils.isEmpty(search.getAttributeValueDbIds()))
				conditions.add(VIEW_TABLE_GERMPLASM_ATTRIBUTES.ATTRIBUTE_VALUE_ID.cast(String.class).in(search.getAttributeValueDbIds()));
			if (!CollectionUtils.isEmpty(search.getAttributeDbIds()))
				conditions.add(VIEW_TABLE_GERMPLASM_ATTRIBUTES.ATTRIBUTE_ID.cast(String.class).in(search.getAttributeDbIds()));
			if (!CollectionUtils.isEmpty(search.getAttributeNames()))
				conditions.add(VIEW_TABLE_GERMPLASM_ATTRIBUTES.ATTRIBUTE_NAME.in(search.getAttributeNames()));
			if (!CollectionUtils.isEmpty(search.getGermplasmDbIds()))
				conditions.add(VIEW_TABLE_GERMPLASM_ATTRIBUTES.GERMPLASM_ID.cast(String.class).in(search.getGermplasmDbIds()));
			if (!CollectionUtils.isEmpty(search.getGermplasmNames()))
				conditions.add(VIEW_TABLE_GERMPLASM_ATTRIBUTES.GERMPLASM_NAME.in(search.getGermplasmNames()));
			if (!CollectionUtils.isEmpty(search.getDataTypes()))
				conditions.add(VIEW_TABLE_GERMPLASM_ATTRIBUTES.ATTRIBUTE_TYPE.in(search.getDataTypes()));


			List<AttributeValue> av = getAttributeValues(context, conditions);

			long totalCount = context.fetchOne("SELECT FOUND_ROWS()").into(Long.class);
			return new BaseResult<>(new ArrayResult<AttributeValue>()
				.setData(av), currentPage, pageSize, totalCount);
		}
		catch (SQLException e)
		{
			e.printStackTrace();
			throw new ResourceException(Status.SERVER_ERROR_INTERNAL);
		}
	}

	@Post
	public BaseResult<SearchResult> postAttributeValueSearchAsync(AttributeValueSearch attributeValueSearch)
	{
		throw new ResourceException(Status.SERVER_ERROR_NOT_IMPLEMENTED);
	}

	@Get
	public BaseResult<ArrayResult<AttributeValue>> getAttributeValueSearchAsync()
	{
		throw new ResourceException(Status.SERVER_ERROR_NOT_IMPLEMENTED);
	}


}

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
import uk.ac.hutton.ics.brapi.server.germplasm.attribute.BrapiSearchAttributeServerResource;

import static jhi.germinate.server.database.codegen.tables.Attributes.*;

/**
 * @author Sebastian Raubach
 */
public class SearchAttributeServerResource extends AttributeBaseServerResource implements BrapiSearchAttributeServerResource
{
	@Post
	public BaseResult<ArrayResult<Attribute>> postAttributeSearch(AttributeSearch search)
	{
		try (DSLContext context = Database.getContext())
		{
			List<Condition> conditions = new ArrayList<>();

			if (!CollectionUtils.isEmpty(search.getAttributeDbIds()))
				conditions.add(ATTRIBUTES.ID.cast(String.class).in(search.getAttributeDbIds()));
			if (!CollectionUtils.isEmpty(search.getAttributeNames()))
				conditions.add(ATTRIBUTES.NAME.in(search.getAttributeNames()));

			List<Attribute> av = getAttributes(context, conditions);

			long totalCount = context.fetchOne("SELECT FOUND_ROWS()").into(Long.class);
			return new BaseResult<>(new ArrayResult<Attribute>()
				.setData(av), currentPage, pageSize, totalCount);
		}
	}

	@Post
	public BaseResult<SearchResult> postAttributeSearchAsync(AttributeSearch attributeSearch)
	{
		throw new ResourceException(Status.SERVER_ERROR_NOT_IMPLEMENTED);
	}

	@Get
	public BaseResult<ArrayResult<Attribute>> getAttributeSearchAsync()
	{
		throw new ResourceException(Status.SERVER_ERROR_NOT_IMPLEMENTED);
	}
}

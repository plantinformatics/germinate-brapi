package jhi.germinate.brapi.server.resource.germplasm.attribute;

import org.jooq.DSLContext;
import org.restlet.data.Status;
import org.restlet.resource.*;

import java.sql.*;
import java.util.*;

import jhi.germinate.brapi.resource.base.BaseResult;
import jhi.germinate.brapi.resource.attribute.AttributeValue;
import jhi.germinate.server.Database;
import jhi.germinate.server.auth.*;
import jhi.germinate.server.database.tables.records.AttributedataRecord;
import jhi.germinate.server.util.*;

import static jhi.germinate.server.database.tables.Attributedata.*;
import static jhi.germinate.server.database.tables.ViewTableGermplasmAttributes.*;

/**
 * @author Sebastian Raubach
 */
public class AttributeValueIndividualServerResource extends AttributeValueBaseServerResource<AttributeValue>
{
	private String attributeValueDbId;

	@Override
	public void doInit()
	{
		super.doInit();

		try
		{
			this.attributeValueDbId = getRequestAttributes().get("attributeValueDbId").toString();
		}
		catch (Exception e)
		{
		}
	}

	@Put
	@MinUserType(UserType.DATA_CURATOR)
	public BaseResult<AttributeValue> putJson(AttributeValue toUpdate)
	{
		if (StringUtils.isEmpty(attributeValueDbId) || toUpdate == null || toUpdate.getAttributeValueDbId() != null && !Objects.equals(toUpdate.getAttributeValueDbId(), attributeValueDbId))
			throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST);

		try (Connection conn = Database.getConnection();
			 DSLContext context = Database.getContext(conn))
		{
			AttributedataRecord record = context.selectFrom(ATTRIBUTEDATA)
												.where(ATTRIBUTEDATA.ID.cast(String.class).eq(toUpdate.getAttributeValueDbId()))
												.fetchAny();

			if (record == null)
			{
				throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST);
			}
			else
			{
				record.setValue(toUpdate.getValue());
				record.setCreatedOn(toUpdate.getDeterminedDate());
				record.store();

				return getJson();
			}
		}
		catch (SQLException e)
		{
			e.printStackTrace();
			throw new ResourceException(Status.SERVER_ERROR_INTERNAL);
		}
	}

	@Override
	public BaseResult<AttributeValue> getJson()
	{
		try (Connection conn = Database.getConnection();
			 DSLContext context = Database.getContext(conn))
		{
			List<AttributeValue> av = getAttributeValues(context, Collections.singletonList(VIEW_TABLE_GERMPLASM_ATTRIBUTES.ATTRIBUTE_VALUE_ID.cast(String.class).eq(attributeValueDbId)));

			if (!CollectionUtils.isEmpty(av))
				return new BaseResult<>(av.get(0), currentPage, pageSize, 1);
			else
				return new BaseResult<>(null, currentPage, pageSize, 0);
		}
		catch (SQLException e)
		{
			e.printStackTrace();
			throw new ResourceException(Status.SERVER_ERROR_INTERNAL);
		}
	}
}

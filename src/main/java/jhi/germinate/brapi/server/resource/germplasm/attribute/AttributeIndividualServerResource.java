package jhi.germinate.brapi.server.resource.germplasm.attribute;

import org.jooq.DSLContext;
import org.restlet.data.Status;
import org.restlet.resource.*;

import java.sql.*;
import java.util.*;

import jhi.germinate.server.Database;
import jhi.germinate.server.auth.*;
import jhi.germinate.server.database.tables.records.AttributesRecord;
import jhi.germinate.server.util.*;
import uk.ac.hutton.ics.brapi.resource.base.BaseResult;
import uk.ac.hutton.ics.brapi.resource.germplasm.attribute.Attribute;
import uk.ac.hutton.ics.brapi.server.germplasm.attribute.BrapiAttributeIndividualServerResource;

import static jhi.germinate.server.database.tables.Attributes.*;

/**
 * @author Sebastian Raubach
 */
public class AttributeIndividualServerResource extends AttributeBaseServerResource implements BrapiAttributeIndividualServerResource
{
	private String attributeDbId;

	@Override
	public void doInit()
	{
		super.doInit();

		try
		{
			this.attributeDbId = getRequestAttributes().get("attributeDbId").toString();
		}
		catch (Exception e)
		{
		}
	}

	@Put
	@MinUserType(UserType.DATA_CURATOR)
	public BaseResult<Attribute> putAttributeById(Attribute toUpdate)
	{
		if (StringUtils.isEmpty(attributeDbId) || toUpdate == null || toUpdate.getAttributeDbId() != null && !Objects.equals(toUpdate.getAttributeDbId(), attributeDbId))
			throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST);

		try (Connection conn = Database.getConnection();
			 DSLContext context = Database.getContext(conn))
		{
			AttributesRecord record = context.selectFrom(ATTRIBUTES)
											 .where(ATTRIBUTES.ID.cast(String.class).eq(toUpdate.getAttributeDbId()))
											 .fetchAny();

			if (record == null)
			{
				throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST);
			}
			else
			{
				record.setName(toUpdate.getAttributeName());
				record.setDescription(toUpdate.getAttributeDescription());
				record.store();

				return getAttributeById();
			}
		}
		catch (SQLException e)
		{
			e.printStackTrace();
			throw new ResourceException(Status.SERVER_ERROR_INTERNAL);
		}
	}

	@Get
	public BaseResult<Attribute> getAttributeById()
	{
		try (Connection conn = Database.getConnection();
			 DSLContext context = Database.getContext(conn))
		{
			List<Attribute> attributes = getAttributes(context, Collections.singletonList(ATTRIBUTES.ID.cast(String.class).eq(attributeDbId)));

			if (CollectionUtils.isEmpty(attributes))
				return new BaseResult<>(null, currentPage, pageSize, 0);
			else
				return new BaseResult<>(attributes.get(0), currentPage, pageSize, 1);
		}
		catch (SQLException e)
		{
			e.printStackTrace();
			throw new ResourceException(Status.SERVER_ERROR_INTERNAL);
		}
	}
}
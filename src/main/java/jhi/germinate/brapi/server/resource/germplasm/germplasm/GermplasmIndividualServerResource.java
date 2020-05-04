package jhi.germinate.brapi.server.resource.germplasm.germplasm;

import org.jooq.DSLContext;
import org.restlet.data.Status;
import org.restlet.resource.*;

import java.sql.*;
import java.util.*;

import jhi.germinate.brapi.resource.base.BaseResult;
import jhi.germinate.brapi.resource.germplasm.Germplasm;
import jhi.germinate.server.Database;
import jhi.germinate.server.auth.*;
import jhi.germinate.server.util.*;

import static jhi.germinate.server.database.tables.Germinatebase.*;

/**
 * @author Sebastian Raubach
 */
public class GermplasmIndividualServerResource extends GermplasmBaseServerResource<Germplasm>
{
	private String germplasmDbId;

	@Override
	public void doInit()
	{
		super.doInit();

		try
		{
			this.germplasmDbId = getRequestAttributes().get("germplasmDbId").toString();
		}
		catch (Exception e)
		{
		}
	}

	@Put
	@MinUserType(UserType.DATA_CURATOR)
	public BaseResult<Germplasm> putJson(Germplasm newGermplasm)
	{
		if (StringUtils.isEmpty(germplasmDbId) || newGermplasm == null || newGermplasm.getGermplasmDbId() != null && !Objects.equals(newGermplasm.getGermplasmDbId(), germplasmDbId))
			throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST);

		try (Connection conn = Database.getConnection();
			 DSLContext context = Database.getContext(conn))
		{
			addGermplasm(context, newGermplasm, true);

			List<Germplasm> germplasm = getGermplasm(context, Collections.singletonList(GERMINATEBASE.ID.cast(String.class).eq(germplasmDbId)));

			if (!CollectionUtils.isEmpty(germplasm))
				return new BaseResult<>(germplasm.get(0), currentPage, pageSize, 1);
			else
				return new BaseResult<>(null, currentPage, pageSize, 0);
		}
		catch (SQLException e)
		{
			e.printStackTrace();
			throw new ResourceException(Status.SERVER_ERROR_INTERNAL);
		}
	}

	@Override
	public BaseResult<Germplasm> getJson()
	{
		try (Connection conn = Database.getConnection();
			 DSLContext context = Database.getContext(conn))
		{
			List<Germplasm> germplasms = getGermplasm(context, Collections.singletonList(GERMINATEBASE.ID.cast(String.class).eq(germplasmDbId)));

			Germplasm germplasm = CollectionUtils.isEmpty(germplasms) ? null : germplasms.get(0);
			return new BaseResult<>(germplasm, currentPage, pageSize, 1);
		}
		catch (SQLException e)
		{
			e.printStackTrace();
			throw new ResourceException(Status.SERVER_ERROR_INTERNAL);
		}
	}
}

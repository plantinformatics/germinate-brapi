package jhi.germinate.brapi.server.resource.core.germplasm;

import org.jooq.DSLContext;
import org.restlet.data.Status;
import org.restlet.resource.ResourceException;

import java.sql.*;
import java.util.*;

import jhi.germinate.brapi.resource.base.BaseResult;
import jhi.germinate.brapi.resource.germplasm.Germplasm;
import jhi.germinate.server.Database;
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

	@Override
	public BaseResult<Germplasm> getJson()
	{
		if (StringUtils.isEmpty(germplasmDbId))
			throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST);

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

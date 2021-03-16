package jhi.germinate.brapi.server.resource.germplasm.germplasm;

import org.jooq.DSLContext;
import org.restlet.data.Status;
import org.restlet.resource.*;

import java.sql.*;
import java.util.*;

import jhi.germinate.server.Database;
import jhi.germinate.server.auth.*;
import jhi.germinate.server.util.*;
import uk.ac.hutton.ics.brapi.resource.base.*;
import uk.ac.hutton.ics.brapi.resource.germplasm.germplasm.Germplasm;
import uk.ac.hutton.ics.brapi.server.germplasm.germplasm.BrapiGermplasmIndividualServerResource;

import static jhi.germinate.server.database.codegen.tables.Germinatebase.*;

/**
 * @author Sebastian Raubach
 */
public class GermplasmIndividualServerResource extends GermplasmBaseServerResource implements BrapiGermplasmIndividualServerResource
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
	public BaseResult<Germplasm> putGermplasmById(Germplasm newGermplasm)
	{
		if (StringUtils.isEmpty(germplasmDbId) || newGermplasm == null || newGermplasm.getGermplasmDbId() != null && !Objects.equals(newGermplasm.getGermplasmDbId(), germplasmDbId))
			throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST);

		try (DSLContext context = Database.getContext())
		{
			addGermplasm(context, newGermplasm, true);

			BaseResult<ArrayResult<Germplasm>> tempResult = getGermplasm(context, Collections.singletonList(GERMINATEBASE.ID.cast(String.class).eq(germplasmDbId)));

			if (!CollectionUtils.isEmpty(tempResult.getResult().getData()))
				return new BaseResult<>(tempResult.getResult().getData().get(0), currentPage, pageSize, 1);
			else
				return new BaseResult<>(null, currentPage, pageSize, 0);
		}
	}

	@Get
	public BaseResult<Germplasm> getGermplasmById()
	{
		try (DSLContext context = Database.getContext())
		{
			BaseResult<ArrayResult<Germplasm>> tempResult = getGermplasm(context, Collections.singletonList(GERMINATEBASE.ID.cast(String.class).eq(germplasmDbId)));

			Germplasm germplasm = CollectionUtils.isEmpty(tempResult.getResult().getData()) ? null : tempResult.getResult().getData().get(0);
			return new BaseResult<>(germplasm, currentPage, pageSize, 1);
		}
	}
}

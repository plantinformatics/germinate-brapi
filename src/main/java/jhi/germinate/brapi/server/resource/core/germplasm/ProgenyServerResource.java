package jhi.germinate.brapi.server.resource.core.germplasm;

import org.jooq.DSLContext;
import org.restlet.data.Status;
import org.restlet.resource.ResourceException;

import java.sql.*;
import java.util.List;

import jhi.germinate.brapi.resource.base.BaseResult;
import jhi.germinate.brapi.resource.germplasm.*;
import jhi.germinate.server.Database;
import jhi.germinate.server.util.StringUtils;

import static jhi.germinate.server.database.tables.Germinatebase.*;
import static jhi.germinate.server.database.tables.Pedigreedefinitions.*;
import static jhi.germinate.server.database.tables.Pedigrees.*;

/**
 * @author Sebastian Raubach
 */
public class ProgenyServerResource extends GermplasmBaseServerResource<Progeny>
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
	public BaseResult<Progeny> getJson()
	{
		if (StringUtils.isEmpty(germplasmDbId))
			throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST);

		try (Connection conn = Database.getConnection();
			 DSLContext context = Database.getContext(conn))
		{
			Progeny result = context.select(
				GERMINATEBASE.ID.cast(String.class).as("germplasmDbId"),
				GERMINATEBASE.NAME.as("germplasmName"),
				PEDIGREEDEFINITIONS.DEFINITION.as("pedigree")
			)
									 .from(PEDIGREEDEFINITIONS)
									 .leftJoin(GERMINATEBASE).on(GERMINATEBASE.ID.eq(PEDIGREEDEFINITIONS.GERMINATEBASE_ID))
									 .where(PEDIGREEDEFINITIONS.GERMINATEBASE_ID.cast(String.class).eq(germplasmDbId))
									 .fetchAnyInto(Progeny.class);
			List<Parent> children = context.select(
				GERMINATEBASE.ID.cast(String.class).as("germplasmDbId"),
				GERMINATEBASE.NAME.as("germplasmName"),
				PEDIGREES.RELATIONSHIP_TYPE.cast(String.class).as("parentType")
			).from(PEDIGREES)
										  .leftJoin(GERMINATEBASE).on(GERMINATEBASE.ID.eq(PEDIGREES.GERMINATEBASE_ID))
										  .where(PEDIGREES.PARENT_ID.cast(String.class).eq(germplasmDbId))
										  .fetchInto(Parent.class);

			result.setProgeny(children);

			return new BaseResult<>(result, currentPage, pageSize, 1);
		}
		catch (SQLException e)
		{
			e.printStackTrace();
			throw new ResourceException(Status.SERVER_ERROR_INTERNAL);
		}
	}
}

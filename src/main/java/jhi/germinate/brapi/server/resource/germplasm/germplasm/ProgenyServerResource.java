package jhi.germinate.brapi.server.resource.germplasm.germplasm;

import org.jooq.DSLContext;
import org.restlet.data.Status;
import org.restlet.resource.*;

import java.sql.*;
import java.util.List;

import jhi.germinate.server.Database;
import jhi.germinate.server.util.StringUtils;
import uk.ac.hutton.ics.brapi.resource.base.BaseResult;
import uk.ac.hutton.ics.brapi.resource.germplasm.germplasm.*;
import uk.ac.hutton.ics.brapi.server.germplasm.germplasm.BrapiGermplasmIndividualProgenyServerResource;

import static jhi.germinate.server.database.codegen.tables.Germinatebase.*;
import static jhi.germinate.server.database.codegen.tables.Pedigreedefinitions.*;
import static jhi.germinate.server.database.codegen.tables.Pedigrees.*;

/**
 * @author Sebastian Raubach
 */
public class ProgenyServerResource extends GermplasmBaseServerResource implements BrapiGermplasmIndividualProgenyServerResource
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

	@Get
	public BaseResult<Progeny> getGermplasmByIdProgeny()
	{
		if (StringUtils.isEmpty(germplasmDbId))
			throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST);

		try (DSLContext context = Database.getContext())
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
	}
}

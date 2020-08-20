package jhi.germinate.brapi.server.resource.germplasm.germplasm;

import org.jooq.DSLContext;
import org.restlet.data.Status;
import org.restlet.resource.*;

import java.sql.*;
import java.util.List;
import java.util.stream.Collectors;

import jhi.germinate.server.Database;
import jhi.germinate.server.util.StringUtils;
import uk.ac.hutton.ics.brapi.resource.base.BaseResult;
import uk.ac.hutton.ics.brapi.resource.germplasm.germplasm.*;
import uk.ac.hutton.ics.brapi.server.germplasm.germplasm.BrapiGermplasmIndividualPedigreeServerResource;

import static jhi.germinate.server.database.tables.Germinatebase.*;
import static jhi.germinate.server.database.tables.Pedigreedefinitions.*;
import static jhi.germinate.server.database.tables.Pedigrees.*;

/**
 * @author Sebastian Raubach
 */
public class PedigreeServerResource extends GermplasmBaseServerResource implements BrapiGermplasmIndividualPedigreeServerResource
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
	public BaseResult<Pedigree> getGermplasmByIdPedigree()
	{
		if (StringUtils.isEmpty(germplasmDbId))
			throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST);

		try (Connection conn = Database.getConnection();
			 DSLContext context = Database.getContext(conn))
		{
			Pedigree result = context.select(
				GERMINATEBASE.ID.cast(String.class).as("germplasmDbId"),
				GERMINATEBASE.NAME.as("germplasmName"),
				PEDIGREEDEFINITIONS.DEFINITION.as("pedigree")
			)
									 .from(PEDIGREEDEFINITIONS)
									 .leftJoin(GERMINATEBASE).on(GERMINATEBASE.ID.eq(PEDIGREEDEFINITIONS.GERMINATEBASE_ID))
									 .where(PEDIGREEDEFINITIONS.GERMINATEBASE_ID.cast(String.class).eq(germplasmDbId))
									 .fetchAnyInto(Pedigree.class);
			List<Parent> parents = context.select(
				GERMINATEBASE.ID.cast(String.class).as("germplasmDbId"),
				GERMINATEBASE.NAME.as("germplasmName"),
				PEDIGREES.RELATIONSHIP_TYPE.cast(String.class).as("parentType")
			).from(PEDIGREES)
										  .leftJoin(GERMINATEBASE).on(GERMINATEBASE.ID.eq(PEDIGREES.PARENT_ID))
										  .where(PEDIGREES.GERMINATEBASE_ID.cast(String.class).eq(germplasmDbId))
										  .fetchInto(Parent.class);

			List<String> parentIds = parents.stream()
											.map(Parent::getGermplasmDbId)
											.collect(Collectors.toList());

			List<Sibling> siblings = context.select(
				GERMINATEBASE.ID.cast(String.class).as("germplasmDbId"),
				GERMINATEBASE.NAME.as("germplasmName")
			).from(PEDIGREES).leftJoin(GERMINATEBASE).on(GERMINATEBASE.ID.eq(PEDIGREES.GERMINATEBASE_ID))
											.where(PEDIGREES.PARENT_ID.in(parentIds))
											.and(PEDIGREES.GERMINATEBASE_ID.cast(String.class).notEqual(germplasmDbId))
											.fetchInto(Sibling.class);

			result.setParents(parents)
				  .setSiblings(siblings);

			return new BaseResult<>(result, currentPage, pageSize, 1);
		}
		catch (SQLException e)
		{
			e.printStackTrace();
			throw new ResourceException(Status.SERVER_ERROR_INTERNAL);
		}
	}
}

package jhi.germinate.brapi.server.resource.genotyping.call;

import org.jooq.*;
import org.jooq.impl.DSL;
import org.restlet.data.Status;
import org.restlet.resource.*;

import java.sql.*;
import java.util.*;

import jhi.germinate.server.Database;
import jhi.germinate.server.util.CollectionUtils;
import uk.ac.hutton.ics.brapi.resource.base.*;
import uk.ac.hutton.ics.brapi.resource.genotyping.call.*;
import uk.ac.hutton.ics.brapi.server.genotyping.call.BrapiSearchCallSetServerResource;

import static jhi.germinate.server.database.tables.Datasetmembers.*;
import static jhi.germinate.server.database.tables.Germinatebase.*;

/**
 * @author Sebastian Raubach
 */
public class SearchCallSetServerResource extends CallSetBaseServerResource implements BrapiSearchCallSetServerResource
{
	@Post
	public BaseResult<ArrayResult<CallSet>> postCallSetSearch(CallSetSearch search)
	{
		try (Connection conn = Database.getConnection();
			 DSLContext context = Database.getContext(conn))
		{
			List<Condition> conditions = new ArrayList<>();

			if (!CollectionUtils.isEmpty(search.getCallSetDbIds()))
				conditions.add(DSL.concat(DATASETMEMBERS.DATASET_ID, DSL.val("-"), GERMINATEBASE.ID).in(search.getCallSetDbIds()));
			if (!CollectionUtils.isEmpty(search.getCallSetNames()))
				conditions.add(GERMINATEBASE.NAME.in(search.getCallSetNames()));
			if (!CollectionUtils.isEmpty(search.getVariantSetDbIds()))
				conditions.add(DATASETMEMBERS.DATASET_ID.cast(String.class).in(search.getVariantSetDbIds()));
			if (!CollectionUtils.isEmpty(search.getGermplasmDbIds()))
				conditions.add(GERMINATEBASE.ID.cast(String.class).in(search.getGermplasmDbIds()));
			if (!CollectionUtils.isEmpty(search.getGermplasmNames()))
				conditions.add(GERMINATEBASE.NAME.in(search.getGermplasmNames()));

			List<CallSet> callSets = getCallSets(context, conditions);

			long totalCount = context.fetchOne("SELECT FOUND_ROWS()").into(Long.class);
			return new BaseResult<>(new ArrayResult<CallSet>()
				.setData(callSets), currentPage, pageSize, totalCount);
		}
		catch (SQLException e)
		{
			e.printStackTrace();
			throw new ResourceException(Status.SERVER_ERROR_INTERNAL);
		}
	}

	@Post
	public BaseResult<SearchResult> postCallSetSearchAsync(CallSetSearch callSetSearch)
	{
		throw new ResourceException(Status.SERVER_ERROR_NOT_IMPLEMENTED);
	}

	@Get
	public BaseResult<ArrayResult<CallSet>> getCallSetSearchAsync()
	{
		throw new ResourceException(Status.SERVER_ERROR_NOT_IMPLEMENTED);
	}
}

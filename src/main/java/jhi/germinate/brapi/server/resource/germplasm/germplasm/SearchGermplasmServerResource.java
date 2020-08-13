package jhi.germinate.brapi.server.resource.germplasm.germplasm;

import org.jooq.*;
import org.jooq.impl.DSL;
import org.restlet.data.Status;
import org.restlet.resource.*;

import java.sql.*;
import java.util.*;
import java.util.stream.Collectors;

import jhi.germinate.server.Database;
import jhi.germinate.server.auth.*;
import jhi.germinate.server.database.tables.Germinatebase;
import jhi.germinate.server.util.CollectionUtils;
import uk.ac.hutton.ics.brapi.resource.base.*;
import uk.ac.hutton.ics.brapi.resource.germplasm.germplasm.*;
import uk.ac.hutton.ics.brapi.server.germplasm.germplasm.BrapiSearchGermplasmServerResource;

import static jhi.germinate.server.database.tables.Germinatebase.*;
import static jhi.germinate.server.database.tables.Synonyms.*;
import static jhi.germinate.server.database.tables.Taxonomies.*;

/**
 * @author Sebastian Raubach
 */
public class SearchGermplasmServerResource extends GermplasmBaseServerResource implements BrapiSearchGermplasmServerResource
{
	@Post
	@MinUserType(UserType.DATA_CURATOR)
	public BaseResult<ArrayResult<Germplasm>> postGermplasmSearch(GermplasmSearch search)
	{
		try (Connection conn = Database.getConnection();
			 DSLContext context = Database.getContext(conn))
		{
			List<Condition> conditions = new ArrayList<>();

			if (!CollectionUtils.isEmpty(search.getGermplasmPUIs()))
				conditions.add(GERMINATEBASE.PUID.in(search.getGermplasmPUIs()));
			if (!CollectionUtils.isEmpty(search.getGermplasmDbIds()))
				conditions.add(GERMINATEBASE.ID.cast(String.class).in(search.getGermplasmDbIds()));
			if (!CollectionUtils.isEmpty(search.getGermplasmNames()))
				conditions.add(GERMINATEBASE.NAME.in(search.getGermplasmNames()));
			if (!CollectionUtils.isEmpty(search.getCommonCropNames()))
				conditions.add(TAXONOMIES.CROPNAME.in(search.getCommonCropNames()));
			if (!CollectionUtils.isEmpty(search.getAccessionNumbers()))
				conditions.add(GERMINATEBASE.NAME.in(search.getAccessionNumbers()));
			if (!CollectionUtils.isEmpty(search.getGenus()))
				conditions.add(TAXONOMIES.GENUS.in(search.getGenus()));
			if (!CollectionUtils.isEmpty(search.getSpecies()))
				conditions.add(TAXONOMIES.SPECIES.in(search.getSpecies()));
			if (!CollectionUtils.isEmpty(search.getSynonyms()))
			{
				List<String> cleaned = search.getSynonyms()
											 .stream()
											 .map(s -> s.replaceAll("[^a-zA-Z0-9_-]", ""))
											 .collect(Collectors.toList());

				Condition overall = DSL.condition("JSON_CONTAINS(" + SYNONYMS.SYNONYMS_.getName() + ", '\"" + cleaned.get(0) + "\"')");

				for (int i = 1; i < cleaned.size(); i++)
					overall = overall.or(DSL.condition("JSON_CONTAINS(" + SYNONYMS.SYNONYMS_.getName() + ", '\"" + cleaned.get(i) + "\"')"));

				conditions.add(overall);
			}
			if (!CollectionUtils.isEmpty(search.getParentDbIds()))
				conditions.add(GERMINATEBASE.ENTITYPARENT_ID.cast(String.class).in(search.getParentDbIds()));
			if (!CollectionUtils.isEmpty(search.getProgenyDbIds()))
			{
				Germinatebase g = GERMINATEBASE.as("g");
				conditions.add(DSL.exists(DSL.selectOne().from(g).where(g.ENTITYPARENT_ID.in(GERMINATEBASE.ID).and(g.ID.cast(String.class).in(search.getProgenyDbIds())))));
			}

			List<Germplasm> lists = getGermplasm(context, conditions);

			long totalCount = context.fetchOne("SELECT FOUND_ROWS()").into(Long.class);
			return new BaseResult<>(new ArrayResult<Germplasm>()
				.setData(lists), currentPage, pageSize, totalCount);
		}
		catch (SQLException e)
		{
			e.printStackTrace();
			throw new ResourceException(Status.SERVER_ERROR_INTERNAL);
		}
	}

	@Post
	public BaseResult<SearchResult> postGermplasmSearchAsync(GermplasmSearch germplasmSearch)
	{
		throw new ResourceException(Status.SERVER_ERROR_NOT_IMPLEMENTED);
	}

	@Get
	public BaseResult<ArrayResult<Germplasm>> getGermplasmSearchAsync()
	{
		throw new ResourceException(Status.SERVER_ERROR_NOT_IMPLEMENTED);
	}
}

package jhi.germinate.brapi.server.resource.core.season;

import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.restlet.data.Status;
import org.restlet.resource.ResourceException;

import java.sql.*;
import java.util.List;
import java.util.stream.Collectors;

import jhi.germinate.brapi.resource.*;
import jhi.germinate.brapi.resource.base.BaseResult;
import jhi.germinate.brapi.server.resource.BaseServerResource;
import jhi.germinate.server.Database;

import static jhi.germinate.server.database.tables.Datasets.*;

/**
 * @author Sebastian Raubach
 */
public class SeasonServerResource extends BaseServerResource<ArrayResult<SeasonResult>>
{

	@Override
	public BaseResult<ArrayResult<SeasonResult>> getJson()
	{
		try (Connection conn = Database.getConnection();
			 DSLContext context = Database.getContext(conn))
		{
			List<Integer> years = context.selectDistinct(DSL.year(DATASETS.DATE_START))
										 .hint("SQL_CALC_FOUND_ROWS")
										 .from(DATASETS)
										 .where(DATASETS.DATASET_STATE_ID.eq(1))
										 .and(DATASETS.DATE_START.isNotNull())
										 .limit(pageSize)
										 .offset(pageSize * currentPage)
										 .fetchInto(Integer.class);

			List<SeasonResult> seasons = years.stream()
											  .map(y -> new SeasonResult()
												  .setYear(y)
												  .setSeasonDbId(Integer.toString(y))
												  .setSeasonName(Integer.toString(y)))
											  .collect(Collectors.toList());

			long totalCount = context.fetchOne("SELECT FOUND_ROWS()").into(Long.class);
			return new BaseResult<>(new ArrayResult<SeasonResult>()
				.setData(seasons), currentPage, pageSize, totalCount);
		}
		catch (SQLException e)
		{
			e.printStackTrace();
			throw new ResourceException(Status.SERVER_ERROR_INTERNAL);
		}
	}
}

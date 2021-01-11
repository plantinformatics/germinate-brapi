package jhi.germinate.brapi.server.resource.core.crop;

import org.jooq.DSLContext;
import org.restlet.data.Status;
import org.restlet.resource.*;

import java.sql.*;
import java.util.List;

import jhi.germinate.server.Database;
import uk.ac.hutton.ics.brapi.resource.base.*;
import uk.ac.hutton.ics.brapi.server.base.BaseServerResource;
import uk.ac.hutton.ics.brapi.server.core.crop.BrapiCropServerResource;

import static jhi.germinate.server.database.codegen.tables.Taxonomies.*;

/**
 * @author Sebastian Raubach
 */
public class CropServerResource extends BaseServerResource implements BrapiCropServerResource
{
	@Get
	public BaseResult<ArrayResult<String>> getCommonCropNames()
	{
		try (DSLContext context = Database.getContext())
		{
			List<String> crops = context.selectDistinct(TAXONOMIES.CROPNAME)
										.hint("SQL_CALC_FOUND_ROWS")
										.from(TAXONOMIES)
										.limit(pageSize)
										.offset(pageSize * currentPage)
										.fetch(TAXONOMIES.CROPNAME);

			long totalCount = context.fetchOne("SELECT FOUND_ROWS()").into(Long.class);
			return new BaseResult<>(new ArrayResult<String>()
				.setData(crops), currentPage, pageSize, totalCount);
		}
	}
}

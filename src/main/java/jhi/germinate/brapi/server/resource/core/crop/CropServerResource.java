package jhi.germinate.brapi.server.resource.core.crop;

import jhi.germinate.server.Database;
import jhi.germinate.server.util.Secured;
import org.jooq.DSLContext;
import uk.ac.hutton.ics.brapi.resource.base.*;
import uk.ac.hutton.ics.brapi.server.base.BaseServerResource;
import uk.ac.hutton.ics.brapi.server.core.crop.BrapiCropServerResource;

import javax.annotation.security.PermitAll;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.sql.*;
import java.util.List;

import static jhi.germinate.server.database.codegen.tables.Taxonomies.*;

/**
 * @author Sebastian Raubach
 */
@Path("brapi/v2/commoncropnames")
@Secured
@PermitAll
public class CropServerResource extends BaseServerResource implements BrapiCropServerResource
{
	@Override
	@GET
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public BaseResult<ArrayResult<String>> getCommonCropNames()
		throws IOException, SQLException
	{
		try (Connection conn = Database.getConnection())
		{
			DSLContext context = Database.getContext(conn);
			List<String> crops = context.selectDistinct(TAXONOMIES.CROPNAME)
										.hint("SQL_CALC_FOUND_ROWS")
										.from(TAXONOMIES)
										.limit(pageSize)
										.offset(pageSize * page)
										.fetch(TAXONOMIES.CROPNAME);

			long totalCount = context.fetchOne("SELECT FOUND_ROWS()").into(Long.class);
			return new BaseResult<>(new ArrayResult<String>()
				.setData(crops), page, pageSize, totalCount);
		}
	}
}

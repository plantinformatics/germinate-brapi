package jhi.germinate.brapi.server.resource.genotyping.variant;

import jhi.germinate.server.Database;
import jhi.germinate.server.util.*;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import uk.ac.hutton.ics.brapi.resource.base.BaseResult;
import uk.ac.hutton.ics.brapi.resource.genotyping.variant.Variant;
import uk.ac.hutton.ics.brapi.server.base.BaseServerResource;
import uk.ac.hutton.ics.brapi.server.genotyping.variant.BrapiVariantServerResource;

import javax.annotation.security.PermitAll;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.sql.*;
import java.util.*;

import static jhi.germinate.server.database.codegen.tables.Datasetmembers.*;
import static jhi.germinate.server.database.codegen.tables.ViewTableMarkers.*;

@Path("brapi/v2/variants")
@Secured
@PermitAll
public class VariantServerResource extends BaseServerResource implements BrapiVariantServerResource, VariantBaseServerResource
{
	@GET
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/{variantDbId}")
	public BaseResult<Variant> getVariantById(@PathParam("variantDbId") String variantDbId)
		throws IOException, SQLException
	{
		try (Connection conn = Database.getConnection())
		{
			DSLContext context = Database.getContext(conn);
			List<Variant> variants = getVariantsInternal(context, Collections.singletonList(DSL.concat(DATASETMEMBERS.DATASET_ID, DSL.val("-"), VIEW_TABLE_MARKERS.MARKER_ID).eq(variantDbId)), page, pageSize);

			if (CollectionUtils.isEmpty(variants))
				return new BaseResult<>(null, page, pageSize, 0);
			else
				return new BaseResult<>(variants.get(0), page, pageSize, 1);
		}
	}
}

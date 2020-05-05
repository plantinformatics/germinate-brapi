package jhi.germinate.brapi.server.resource.genotyping.call;

import org.jooq.DSLContext;
import org.restlet.data.Status;
import org.restlet.resource.*;

import java.sql.*;

import jhi.germinate.brapi.resource.base.*;
import jhi.germinate.brapi.resource.call.*;
import jhi.germinate.brapi.server.resource.TokenBaseServerResource;
import jhi.germinate.server.Database;

/**
 * @author Sebastian Raubach
 */
public class SearchCallServerResource extends TokenBaseServerResource<ArrayResult<Call>>
{
	@Override
	public TokenBaseResult<ArrayResult<Call>> getJson()
	{
		throw new ResourceException(Status.SERVER_ERROR_NOT_IMPLEMENTED);
	}

	@Post
	public TokenBaseResult<ArrayResult<Call>> postJson(CallSearch search)
	{
		try (Connection conn = Database.getConnection();
			 DSLContext context = Database.getContext(conn))
		{
			// TODO: implement
			return null;
		}
		catch (SQLException e)
		{
			e.printStackTrace();
			throw new ResourceException(Status.SERVER_ERROR_INTERNAL);
		}
	}
}

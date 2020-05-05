package jhi.germinate.brapi.server.resource.genotyping.variant;

import org.jooq.DSLContext;
import org.restlet.data.Status;
import org.restlet.resource.ResourceException;

import java.sql.*;

import jhi.germinate.brapi.resource.base.*;
import jhi.germinate.brapi.resource.call.Call;
import jhi.germinate.brapi.server.resource.TokenBaseServerResource;
import jhi.germinate.server.Database;

/**
 * @author Sebastian Raubach
 */
public class VariantCallServerResource extends TokenBaseServerResource<VariantCallResult<Call>>
{
	private static final String PARAM_EXPAND_HOMOZYGOTES = "expandHomozygotes";
	private static final String PARAM_UNKNOWN_STRING     = "unknownString";
	private static final String PARAM_SEP_PHASED         = "sepPhased";
	private static final String PARAM_SEP_UNPHASED       = "sepUnphased";

	private String variantDbId;

	private String expandHomozygotes;
	private String unknownString;
	private String sepPhased;
	private String sepUnphased;

	@Override
	public void doInit()
	{
		super.doInit();

		this.expandHomozygotes = getQueryValue(PARAM_EXPAND_HOMOZYGOTES);
		this.unknownString = getQueryValue(PARAM_UNKNOWN_STRING);
		this.sepPhased = getQueryValue(PARAM_SEP_PHASED);
		this.sepUnphased = getQueryValue(PARAM_SEP_UNPHASED);

		try
		{
			this.variantDbId = getRequestAttributes().get("variantDbId").toString();
		}
		catch (Exception e)
		{
		}
	}

	@Override
	public TokenBaseResult<VariantCallResult<Call>> getJson()
	{
		try (Connection conn = Database.getConnection();
			 DSLContext context = Database.getContext(conn))
		{
			// TODO
			return null;
		}
		catch (SQLException e)
		{
			e.printStackTrace();
			throw new ResourceException(Status.SERVER_ERROR_INTERNAL);
		}
	}
}

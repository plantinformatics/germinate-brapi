package jhi.germinate.brapi.server.resource.genotyping.call;

import org.jooq.DSLContext;
import org.restlet.data.Status;
import org.restlet.resource.ResourceException;

import java.sql.*;

import jhi.germinate.brapi.resource.base.*;
import jhi.germinate.brapi.resource.call.Call;
import jhi.germinate.brapi.server.resource.TokenBaseServerResource;
import jhi.germinate.brapi.server.util.GenotypeEncodingParams;
import jhi.germinate.server.Database;
import jhi.germinate.server.util.StringUtils;

/**
 * @author Sebastian Raubach
 */
public class CallServerResource extends TokenBaseServerResource<ArrayResult<Call>>
{
	private static final String PARAM_EXPAND_HOMOZYGOTES = "expandHomozygotes";
	private static final String PARAM_UNKNOWN_STRING     = "unknownString";
	private static final String PARAM_SEP_PHASED         = "sepPhased";
	private static final String PARAM_SEP_UNPHASED       = "sepUnphased";
	private static final String PARAM_CALL_SET_DB_ID     = "callSetDbId";
	private static final String PARAM_VARIANT_DB_ID      = "variantDbId";
	private static final String PARAM_VARIANT_SET_DB_ID  = "variantSetDbId";

	private String callSetDbId;
	private String variantDbId;
	private String variantSetDbId;

	private GenotypeEncodingParams params = new GenotypeEncodingParams();

	@Override
	public void doInit()
	{
		super.doInit();

		try
		{
			String expand = getQueryValue(PARAM_EXPAND_HOMOZYGOTES);

			if (!StringUtils.isEmpty(expand))
				params.setCollapse(!Boolean.parseBoolean(expand));
		}
		catch (Exception e)
		{
		}
		String unknownString = getQueryValue(PARAM_UNKNOWN_STRING);
		if (unknownString != null)
			params.setUnknownString(unknownString);
		String sepPhased = getQueryValue(PARAM_SEP_PHASED);
		if (sepPhased != null)
			params.setSepPhased(sepPhased);
		String sepUnphased = getQueryValue(PARAM_SEP_UNPHASED);
		if (sepUnphased != null)
			params.setSepUnphased(sepUnphased);

		this.callSetDbId = getQueryValue(PARAM_CALL_SET_DB_ID);
		this.variantDbId = getQueryValue(PARAM_VARIANT_DB_ID);
		this.variantSetDbId = getQueryValue(PARAM_VARIANT_SET_DB_ID);
	}

	@Override
	public TokenBaseResult<ArrayResult<Call>> getJson()
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

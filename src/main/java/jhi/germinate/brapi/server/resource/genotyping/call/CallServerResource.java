package jhi.germinate.brapi.server.resource.genotyping.call;


import org.restlet.data.Status;
import org.restlet.resource.*;

import jhi.germinate.brapi.server.util.GenotypeEncodingParams;
import jhi.germinate.server.util.StringUtils;
import uk.ac.hutton.ics.brapi.resource.base.TokenBaseResult;
import uk.ac.hutton.ics.brapi.resource.genotyping.call.*;
import uk.ac.hutton.ics.brapi.server.base.TokenBaseServerResource;
import uk.ac.hutton.ics.brapi.server.genotyping.call.BrapiCallServerResource;

/**
 * @author Sebastian Raubach
 */
public class CallServerResource extends TokenBaseServerResource implements BrapiCallServerResource
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

	@Get
	public TokenBaseResult<CallResult<Call>> getCalls()
	{
		throw new ResourceException(Status.SERVER_ERROR_NOT_IMPLEMENTED);
	}
}

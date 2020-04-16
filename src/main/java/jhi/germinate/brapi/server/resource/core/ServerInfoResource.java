package jhi.germinate.brapi.server.resource.core;

import java.util.*;
import java.util.stream.Collectors;

import jhi.germinate.brapi.resource.BrapiCallResult;
import jhi.germinate.brapi.resource.base.*;
import jhi.germinate.brapi.server.resource.BaseServerResource;

/**
 * @author Sebastian Raubach
 */
public class ServerInfoResource extends BaseServerResource<BrapiCallResult>
{
	public static final  String          PARAM_DATA_TYPE = "dataType";
	private static final List<BrapiCall> CALLS           = new ArrayList<>();

	static
	{
		CALLS.add(new BrapiCall("serverinfo")
			.addDataType(BrapiCall.DataType.json)
			.addMethod(BrapiCall.Method.GET)
			.addVersion(BrapiCall.Version.TWO_ZERO));
	}

	private BrapiCall.DataType dataType = null;

	@Override
	public void doInit()
	{
		super.doInit();

		try
		{
			this.dataType = BrapiCall.DataType.valueOf(getQueryValue(PARAM_DATA_TYPE));
		}
		catch (Exception e)
		{
		}
	}

	@Override
	public BaseResult<BrapiCallResult> getJson()
	{
		List<BrapiCall> calls = CALLS;

		if (dataType != null)
		{
			calls = calls.stream()
						 .filter(c -> c.getDataTypes().contains(dataType)) // Get the calls that support the query data type
						 .collect(Collectors.toCollection(ArrayList::new));
		}

		int start = currentPage * pageSize;
		int end = Math.min(start + pageSize, calls.size());

		calls = calls.subList(start, end);

		return new BaseResult<>(new BrapiCallResult() // TODO: Set other things.
													  .setCalls(calls), currentPage, pageSize, calls.size());

	}
}

package jhi.germinate.brapi.server.resource.core;

import org.restlet.resource.Get;

import java.util.*;
import java.util.stream.Collectors;

import uk.ac.hutton.ics.brapi.resource.base.*;
import uk.ac.hutton.ics.brapi.resource.core.serverinfo.ServerInfo;
import uk.ac.hutton.ics.brapi.server.base.BaseServerResource;
import uk.ac.hutton.ics.brapi.server.core.serverinfo.BrapiServerInfoResource;

/**
 * @author Sebastian Raubach
 */
public class ServerInfoResource extends BaseServerResource implements BrapiServerInfoResource
{
	public static final  String          PARAM_DATA_TYPE = "dataType";
	private static final List<BrapiCall> CALLS           = new ArrayList<>();

	static
	{
		// SERVERINFO
		CALLS.add(new BrapiCall("serverinfo")
			.addDataType(BrapiCall.DataType.json)
			.addMethod(BrapiCall.Method.GET)
			.addVersion(BrapiCall.Version.TWO_ZERO));

		// CROPS
		CALLS.add(new BrapiCall("commoncropnames")
			.addDataType(BrapiCall.DataType.json)
			.addMethod(BrapiCall.Method.GET)
			.addVersion(BrapiCall.Version.TWO_ZERO));

		// LISTS
		CALLS.add(new BrapiCall("lists")
			.addDataType(BrapiCall.DataType.json)
			.addMethod(BrapiCall.Method.GET)
			.addMethod(BrapiCall.Method.POST)
			.addVersion(BrapiCall.Version.TWO_ZERO));
		CALLS.add(new BrapiCall("lists/{listDbId}")
			.addDataType(BrapiCall.DataType.json)
			.addMethod(BrapiCall.Method.GET)
			.addMethod(BrapiCall.Method.PUT)
			.addVersion(BrapiCall.Version.TWO_ZERO));
		CALLS.add(new BrapiCall("lists/{listDbId}/items")
			.addDataType(BrapiCall.DataType.json)
			.addMethod(BrapiCall.Method.POST)
			.addVersion(BrapiCall.Version.TWO_ZERO));
		CALLS.add(new BrapiCall("search/lists")
			.addDataType(BrapiCall.DataType.json)
			.addMethod(BrapiCall.Method.POST)
			.addVersion(BrapiCall.Version.TWO_ZERO));

		// LOCATIONS
		CALLS.add(new BrapiCall("locations")
			.addDataType(BrapiCall.DataType.json)
			.addMethod(BrapiCall.Method.GET)
			.addVersion(BrapiCall.Version.TWO_ZERO));
		CALLS.add(new BrapiCall("locations/{locationDbId}")
			.addDataType(BrapiCall.DataType.json)
			.addMethod(BrapiCall.Method.GET)
			.addVersion(BrapiCall.Version.TWO_ZERO));
		CALLS.add(new BrapiCall("search/locations")
			.addDataType(BrapiCall.DataType.json)
			.addMethod(BrapiCall.Method.POST)
			.addVersion(BrapiCall.Version.TWO_ZERO));

		// SEASONS
		CALLS.add(new BrapiCall("seasons")
			.addDataType(BrapiCall.DataType.json)
			.addMethod(BrapiCall.Method.GET)
			.addVersion(BrapiCall.Version.TWO_ZERO));
		CALLS.add(new BrapiCall("seasons/{seasonDbId}")
			.addDataType(BrapiCall.DataType.json)
			.addMethod(BrapiCall.Method.GET)
			.addVersion(BrapiCall.Version.TWO_ZERO));

		// STUDY
		CALLS.add(new BrapiCall("studies")
			.addDataType(BrapiCall.DataType.json)
			.addMethod(BrapiCall.Method.GET)
			.addVersion(BrapiCall.Version.TWO_ZERO));
		CALLS.add(new BrapiCall("studies/{studyDbId}")
			.addDataType(BrapiCall.DataType.json)
			.addMethod(BrapiCall.Method.GET)
			.addVersion(BrapiCall.Version.TWO_ZERO));
		CALLS.add(new BrapiCall("search/studies")
			.addDataType(BrapiCall.DataType.json)
			.addMethod(BrapiCall.Method.POST)
			.addVersion(BrapiCall.Version.TWO_ZERO));
		CALLS.add(new BrapiCall("studytypes")
			.addDataType(BrapiCall.DataType.json)
			.addMethod(BrapiCall.Method.GET)
			.addVersion(BrapiCall.Version.TWO_ZERO));

		// TRIALS
		CALLS.add(new BrapiCall("trials")
			.addDataType(BrapiCall.DataType.json)
			.addMethod(BrapiCall.Method.GET)
			.addVersion(BrapiCall.Version.TWO_ZERO));
		CALLS.add(new BrapiCall("trials/{trialDbId}")
			.addDataType(BrapiCall.DataType.json)
			.addMethod(BrapiCall.Method.GET)
			.addVersion(BrapiCall.Version.TWO_ZERO));

		// CALLSETS
		CALLS.add(new BrapiCall("callsets")
			.addDataType(BrapiCall.DataType.json)
			.addMethod(BrapiCall.Method.GET)
			.addVersion(BrapiCall.Version.TWO_ZERO));
		CALLS.add(new BrapiCall("callsets/{callSetDbId}")
			.addDataType(BrapiCall.DataType.json)
			.addMethod(BrapiCall.Method.GET)
			.addVersion(BrapiCall.Version.TWO_ZERO));
		CALLS.add(new BrapiCall("callsets/{callSetDbId}/calls")
			.addDataType(BrapiCall.DataType.json)
			.addMethod(BrapiCall.Method.GET)
			.addVersion(BrapiCall.Version.TWO_ZERO));

		// MAPS
		CALLS.add(new BrapiCall("maps")
			.addDataType(BrapiCall.DataType.json)
			.addMethod(BrapiCall.Method.GET)
			.addVersion(BrapiCall.Version.TWO_ZERO));
		CALLS.add(new BrapiCall("maps/{mapDbId}")
			.addDataType(BrapiCall.DataType.json)
			.addMethod(BrapiCall.Method.GET)
			.addVersion(BrapiCall.Version.TWO_ZERO));
		CALLS.add(new BrapiCall("maps/{mapDbId}/linkagegroups")
			.addDataType(BrapiCall.DataType.json)
			.addMethod(BrapiCall.Method.GET)
			.addVersion(BrapiCall.Version.TWO_ZERO));
		CALLS.add(new BrapiCall("markerpositions")
			.addDataType(BrapiCall.DataType.json)
			.addMethod(BrapiCall.Method.GET)
			.addVersion(BrapiCall.Version.TWO_ZERO));
		CALLS.add(new BrapiCall("search/markerpositions")
			.addDataType(BrapiCall.DataType.json)
			.addMethod(BrapiCall.Method.POST)
			.addVersion(BrapiCall.Version.TWO_ZERO));

		// VARIANT
		CALLS.add(new BrapiCall("variants")
			.addDataType(BrapiCall.DataType.json)
			.addMethod(BrapiCall.Method.GET)
			.addVersion(BrapiCall.Version.TWO_ZERO));
		CALLS.add(new BrapiCall("variants/{variantDbId}")
			.addDataType(BrapiCall.DataType.json)
			.addMethod(BrapiCall.Method.GET)
			.addVersion(BrapiCall.Version.TWO_ZERO));
		CALLS.add(new BrapiCall("variants/{variantDbId}/calls")
			.addDataType(BrapiCall.DataType.json)
			.addMethod(BrapiCall.Method.GET)
			.addVersion(BrapiCall.Version.TWO_ZERO));
		CALLS.add(new BrapiCall("search/variants")
			.addDataType(BrapiCall.DataType.json)
			.addMethod(BrapiCall.Method.POST)
			.addVersion(BrapiCall.Version.TWO_ZERO));
		CALLS.add(new BrapiCall("variantsets")
			.addDataType(BrapiCall.DataType.json)
			.addMethod(BrapiCall.Method.GET)
			.addVersion(BrapiCall.Version.TWO_ZERO));
		CALLS.add(new BrapiCall("variantsets/{variantSetDbId}")
			.addDataType(BrapiCall.DataType.json)
			.addMethod(BrapiCall.Method.GET)
			.addVersion(BrapiCall.Version.TWO_ZERO));
		CALLS.add(new BrapiCall("search/variantset")
			.addDataType(BrapiCall.DataType.json)
			.addMethod(BrapiCall.Method.POST)
			.addVersion(BrapiCall.Version.TWO_ZERO));

		// BREEDING METHOD
		CALLS.add(new BrapiCall("breedingmethod")
			.addDataType(BrapiCall.DataType.json)
			.addMethod(BrapiCall.Method.GET)
			.addVersion(BrapiCall.Version.TWO_ZERO));
		CALLS.add(new BrapiCall("breedingmethod/{breedingMethodDbId}")
			.addDataType(BrapiCall.DataType.json)
			.addMethod(BrapiCall.Method.GET)
			.addVersion(BrapiCall.Version.TWO_ZERO));

		// GERMPLASM
		CALLS.add(new BrapiCall("germplasm")
			.addDataType(BrapiCall.DataType.json)
			.addMethod(BrapiCall.Method.GET)
			.addMethod(BrapiCall.Method.POST)
			.addVersion(BrapiCall.Version.TWO_ZERO));
		CALLS.add(new BrapiCall("germplasm/{germplasmDbId}")
			.addDataType(BrapiCall.DataType.json)
			.addMethod(BrapiCall.Method.GET)
			.addMethod(BrapiCall.Method.PUT)
			.addVersion(BrapiCall.Version.TWO_ZERO));
		CALLS.add(new BrapiCall("germplasm/{germplasmDbId}/mcpd")
			.addDataType(BrapiCall.DataType.json)
			.addMethod(BrapiCall.Method.GET)
			.addVersion(BrapiCall.Version.TWO_ZERO));
		CALLS.add(new BrapiCall("germplasm/{germplasmDbId}/pedigree")
			.addDataType(BrapiCall.DataType.json)
			.addMethod(BrapiCall.Method.GET)
			.addVersion(BrapiCall.Version.TWO_ZERO));
		CALLS.add(new BrapiCall("germplasm/{germplasmDbId}/progeny")
			.addDataType(BrapiCall.DataType.json)
			.addMethod(BrapiCall.Method.GET)
			.addVersion(BrapiCall.Version.TWO_ZERO));
		CALLS.add(new BrapiCall("search/germplasm")
			.addDataType(BrapiCall.DataType.json)
			.addMethod(BrapiCall.Method.POST)
			.addVersion(BrapiCall.Version.TWO_ZERO));

		// GERMPLASM ATTRIBUTES
		CALLS.add(new BrapiCall("attributes")
			.addDataType(BrapiCall.DataType.json)
			.addMethod(BrapiCall.Method.GET)
			.addMethod(BrapiCall.Method.POST)
			.addVersion(BrapiCall.Version.TWO_ZERO));
		CALLS.add(new BrapiCall("attributes/{attributeDbId}")
			.addDataType(BrapiCall.DataType.json)
			.addMethod(BrapiCall.Method.GET)
			.addMethod(BrapiCall.Method.PUT)
			.addVersion(BrapiCall.Version.TWO_ZERO));
		CALLS.add(new BrapiCall("attributes/categories")
			.addDataType(BrapiCall.DataType.json)
			.addMethod(BrapiCall.Method.GET)
			.addVersion(BrapiCall.Version.TWO_ZERO));
		CALLS.add(new BrapiCall("search/attributes")
			.addDataType(BrapiCall.DataType.json)
			.addMethod(BrapiCall.Method.POST)
			.addVersion(BrapiCall.Version.TWO_ZERO));
		CALLS.add(new BrapiCall("attributevalues")
			.addDataType(BrapiCall.DataType.json)
			.addMethod(BrapiCall.Method.GET)
			.addMethod(BrapiCall.Method.POST)
			.addVersion(BrapiCall.Version.TWO_ZERO));
		CALLS.add(new BrapiCall("attributevalues/{attributeValueDbId}")
			.addDataType(BrapiCall.DataType.json)
			.addMethod(BrapiCall.Method.PUT)
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

	@Get
	public BaseResult<ServerInfo> getServerinfo()
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

		return new BaseResult<>(new ServerInfo() // TODO: Set other things.
												 .setCalls(calls), currentPage, pageSize, calls.size());

	}
}

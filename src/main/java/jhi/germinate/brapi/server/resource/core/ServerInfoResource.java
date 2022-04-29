package jhi.germinate.brapi.server.resource.core;

import jhi.germinate.resource.enums.UserType;
import jhi.germinate.server.util.Secured;
import uk.ac.hutton.ics.brapi.resource.base.*;
import uk.ac.hutton.ics.brapi.resource.core.serverinfo.ServerInfo;
import uk.ac.hutton.ics.brapi.server.base.BaseServerResource;
import uk.ac.hutton.ics.brapi.server.core.serverinfo.BrapiServerInfoResource;

import jakarta.ws.rs.*;
import java.io.IOException;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Sebastian Raubach
 */
@Path("brapi/v2/serverinfo")
public class ServerInfoResource extends BaseServerResource implements BrapiServerInfoResource
{
	private static final List<BrapiCall> CALLS = new ArrayList<>();

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

		// CALLS
		CALLS.add(new BrapiCall("calls")
			.addDataType(BrapiCall.DataType.json)
			.addMethod(BrapiCall.Method.GET)
			.addVersion(BrapiCall.Version.TWO_ZERO));
		CALLS.add(new BrapiCall("search/calls")
			.addDataType(BrapiCall.DataType.json)
			.addMethod(BrapiCall.Method.POST)
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
		CALLS.add(new BrapiCall("search/callsets")
			.addDataType(BrapiCall.DataType.json)
			.addMethod(BrapiCall.Method.POST)
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
		CALLS.add(new BrapiCall("variantsets/{variantSetDbId}/calls")
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
		CALLS.add(new BrapiCall("search/attributevalues")
			.addDataType(BrapiCall.DataType.json)
			.addMethod(BrapiCall.Method.POST)
			.addVersion(BrapiCall.Version.TWO_ZERO));

		// OBSERVATION VARIABLES
		CALLS.add(new BrapiCall("variables")
			.addDataType(BrapiCall.DataType.json)
			.addMethod(BrapiCall.Method.GET)
			.addVersion(BrapiCall.Version.TWO_ZERO));
	}

	@GET
	@Consumes({"application/json"})
	@Produces({"application/json"})
	public BaseResult<ServerInfo> getServerinfo(@QueryParam("dataType") String dataType)
		throws SQLException, IOException
	{
		List<BrapiCall> calls = CALLS;

		if (dataType != null)
		{
			calls = calls.stream()
						 .filter(c -> c.getDataTypes().contains(dataType)) // Get the calls that support the query data type
						 .collect(Collectors.toCollection(ArrayList::new));
		}

		int start = page * pageSize;
		int end = Math.min(start + pageSize, calls.size());

		calls = calls.subList(start, end);

		return new BaseResult<>(new ServerInfo() // TODO: Set other things.
												 .setCalls(calls), page, pageSize, calls.size());
	}
}

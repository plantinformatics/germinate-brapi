package jhi.germinate.brapi.server.resource.genotyping.call;

import org.restlet.data.Status;
import org.restlet.resource.*;

import uk.ac.hutton.ics.brapi.resource.base.*;
import uk.ac.hutton.ics.brapi.resource.genotyping.call.*;
import uk.ac.hutton.ics.brapi.server.base.TokenBaseServerResource;
import uk.ac.hutton.ics.brapi.server.genotyping.call.BrapiSearchCallServerResource;

/**
 * @author Sebastian Raubach
 */
public class SearchCallServerResource extends TokenBaseServerResource implements BrapiSearchCallServerResource
{
	@Post
	public TokenBaseResult<CallResult<Call>> postCallSearch(CallSearch search)
	{
		throw new ResourceException(Status.SERVER_ERROR_NOT_IMPLEMENTED);
	}

	@Post
	public BaseResult<SearchResult> postCallSearchAsync(CallSearch callSearch)
	{
		throw new ResourceException(Status.SERVER_ERROR_NOT_IMPLEMENTED);
	}

	@Get
	public TokenBaseResult<CallResult<Call>> getCallSearchAsync()
	{
		throw new ResourceException(Status.SERVER_ERROR_NOT_IMPLEMENTED);
	}
}

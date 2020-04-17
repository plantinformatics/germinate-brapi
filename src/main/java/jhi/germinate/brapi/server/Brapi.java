package jhi.germinate.brapi.server;

import org.restlet.resource.ServerResource;
import org.restlet.routing.Router;

import jhi.germinate.brapi.server.resource.core.ServerInfoResource;
import jhi.germinate.brapi.server.resource.core.crop.CropServerResource;
import jhi.germinate.brapi.server.resource.core.list.*;
import jhi.germinate.brapi.server.resource.core.location.LocationServerResource;
import jhi.germinate.brapi.server.resource.core.season.SeasonServerResource;

/**
 * @author Sebastian Raubach
 */
public class Brapi
{
	private String urlPrefix;
	private Router router;

	public Brapi(String urlPrefix, Router router)
	{
		this.urlPrefix = urlPrefix;
		this.router = router;

		init();
	}

	private void init()
	{
		// CORE
		attachToRouter(router, "/commoncropnames", CropServerResource.class);
		attachToRouter(router, "/lists", ListServerResource.class);
		attachToRouter(router, "/lists/{listDbId}", ListIndividualServerResource.class);
		attachToRouter(router, "/lists/{listDbId}/items", ListModificationServerResource.class);
		attachToRouter(router, "/locations", LocationServerResource.class);
		attachToRouter(router, "/search/lists", SearchListServerResource.class);
		attachToRouter(router, "/seasons", SeasonServerResource.class);
		attachToRouter(router, "/serverinfo", ServerInfoResource.class);
	}

	private void attachToRouter(Router router, String url, Class<? extends ServerResource> clazz)
	{
		router.attach(urlPrefix + url, clazz);
		router.attach(urlPrefix + url + "/", clazz);
	}
}

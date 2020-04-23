package jhi.germinate.brapi.server;

import org.restlet.resource.ServerResource;
import org.restlet.routing.Router;

import jhi.germinate.brapi.server.resource.core.ServerInfoResource;
import jhi.germinate.brapi.server.resource.core.crop.CropServerResource;
import jhi.germinate.brapi.server.resource.core.list.*;
import jhi.germinate.brapi.server.resource.core.location.*;
import jhi.germinate.brapi.server.resource.core.season.*;
import jhi.germinate.brapi.server.resource.core.study.*;
import jhi.germinate.brapi.server.resource.genotyping.map.*;
import jhi.germinate.brapi.server.resource.genotyping.marker.*;

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
		attachToRouter(router, "/search/lists", SearchListServerResource.class);
		attachToRouter(router, "/locations", LocationServerResource.class);
		attachToRouter(router, "/locations/{locationDbId}", LocationIndividualServerResource.class);
		attachToRouter(router, "/search/locations", SearchLocationServerResource.class);
		attachToRouter(router, "/seasons", SeasonServerResource.class);
		attachToRouter(router, "/season/{seasonDbId}", SeasonIndividualServerResource.class);
		attachToRouter(router, "/studies", StudyServerResource.class);
		attachToRouter(router, "/studies/{studyDbId}", StudyIndividualServerResource.class);
		attachToRouter(router, "/studytypes", StudyTypesServerResource.class);
		attachToRouter(router, "/serverinfo", ServerInfoResource.class);

		//GENOTYPING
		attachToRouter(router, "/maps", MapServerResource.class);
		attachToRouter(router, "/maps/{mapDbId}", MapIndividualServerResource.class);
		attachToRouter(router, "/maps/{mapDbId}/linkagegroups", MapLinkageGroupServerResource.class);
		attachToRouter(router, "/markerpositions", MarkerPositionServerResource.class);
		attachToRouter(router, "/search/markerpositions", SearchMarkerPositionServerResource.class);
	}

	private void attachToRouter(Router router, String url, Class<? extends ServerResource> clazz)
	{
		router.attach(urlPrefix + url, clazz);
		router.attach(urlPrefix + url + "/", clazz);
	}
}

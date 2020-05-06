package jhi.germinate.brapi.server;

import org.restlet.resource.ServerResource;
import org.restlet.routing.Router;

import jhi.germinate.brapi.server.resource.core.ServerInfoResource;
import jhi.germinate.brapi.server.resource.core.crop.CropServerResource;
import jhi.germinate.brapi.server.resource.core.list.*;
import jhi.germinate.brapi.server.resource.core.location.*;
import jhi.germinate.brapi.server.resource.core.season.*;
import jhi.germinate.brapi.server.resource.core.study.*;
import jhi.germinate.brapi.server.resource.core.trial.*;
import jhi.germinate.brapi.server.resource.genotyping.call.*;
import jhi.germinate.brapi.server.resource.genotyping.map.*;
import jhi.germinate.brapi.server.resource.genotyping.marker.*;
import jhi.germinate.brapi.server.resource.genotyping.variant.*;
import jhi.germinate.brapi.server.resource.germplasm.attribute.*;
import jhi.germinate.brapi.server.resource.germplasm.breedingmethod.*;
import jhi.germinate.brapi.server.resource.germplasm.germplasm.*;

/**
 * @author Sebastian Raubach
 */
public class Brapi
{
	private String urlPrefix;
	private Router routerAuth;
	private Router routerUnauth;

	public Brapi(String urlPrefix, Router routerAuth, Router routerUnauth)
	{
		this.urlPrefix = urlPrefix;
		this.routerAuth = routerAuth;
		this.routerUnauth = routerUnauth;

		init();
	}

	private void init()
	{
		// CORE
		attachToRouter(routerAuth, "/commoncropnames", CropServerResource.class);
		attachToRouter(routerAuth, "/lists", ListServerResource.class);
		attachToRouter(routerAuth, "/lists/{listDbId}", ListIndividualServerResource.class);
		attachToRouter(routerAuth, "/lists/{listDbId}/items", ListModificationServerResource.class);
		attachToRouter(routerAuth, "/search/lists", SearchListServerResource.class);
		attachToRouter(routerAuth, "/locations", LocationServerResource.class);
		attachToRouter(routerAuth, "/locations/{locationDbId}", LocationIndividualServerResource.class);
		attachToRouter(routerAuth, "/search/locations", SearchLocationServerResource.class);
		attachToRouter(routerAuth, "/seasons", SeasonServerResource.class);
		attachToRouter(routerAuth, "/season/{seasonDbId}", SeasonIndividualServerResource.class);
		attachToRouter(routerAuth, "/studies", StudyServerResource.class);
		attachToRouter(routerAuth, "/studies/{studyDbId}", StudyIndividualServerResource.class);
		attachToRouter(routerAuth, "/search/studies", SearchStudyServerResource.class);
		attachToRouter(routerAuth, "/studytypes", StudyTypesServerResource.class);
		attachToRouter(routerAuth, "/trials", TrialServerResource.class);
		attachToRouter(routerAuth, "/trials/{trialDbId}", TrialIndividualServerResource.class);
		attachToRouter(routerUnauth, "/serverinfo", ServerInfoResource.class);

		//GENOTYPING
		// TODO: write implementation
		attachToRouter(routerAuth, "/calls", CallServerResource.class);
		// TODO: write implementation
		attachToRouter(routerAuth, "/search/calls", SearchCallServerResource.class);
		attachToRouter(routerAuth, "/callsets", CallSetServerResource.class);
		attachToRouter(routerAuth, "/callsets/{callSetDbId}", CallSetIndividualServerResource.class);
		// TODO: write implementation
		attachToRouter(routerAuth, "/callsets/{callSetDbId}/calls", CallSetCallServerResource.class);
		attachToRouter(routerAuth, "/search/callsets", SearchCallSetServerResource.class);
		attachToRouter(routerAuth, "/maps", MapServerResource.class);
		attachToRouter(routerAuth, "/maps/{mapDbId}", MapIndividualServerResource.class);
		attachToRouter(routerAuth, "/maps/{mapDbId}/linkagegroups", MapLinkageGroupServerResource.class);
		attachToRouter(routerAuth, "/markerpositions", MarkerPositionServerResource.class);
		attachToRouter(routerAuth, "/search/markerpositions", SearchMarkerPositionServerResource.class);
		attachToRouter(routerAuth, "/variants", VariantServerResource.class);
		attachToRouter(routerAuth, "/variants/{variantDbId}", VariantIndividualServerResource.class);
		// TODO: Write implementation
		attachToRouter(routerAuth, "/variants/{variantDbId}/calls", VariantCallServerResource.class);
		attachToRouter(routerAuth, "/search/variants", SearchVariantServerResource.class);
		attachToRouter(routerAuth, "/variantsets", VariantSetServerResource.class);
		attachToRouter(routerAuth, "/variantsets/{variantSetDbId}", VariantSetServerResource.class);
		// TODO: implement
//		attachToRouter(routerAuth, "/variantsets/{variantSetDbId}/calls", VariantSetCallServerResource.class);
		attachToRouter(routerAuth, "/search/variantset", SearchVariantSetServerResource.class);

		// GERMPLASM
		attachToRouter(routerAuth, "/breedingmethod", BreedingMethodServerResource.class);
		attachToRouter(routerAuth, "/breedingmethod/{breedingMethodDbId}", BreedingMethodIndividualServerResource.class);
		attachToRouter(routerAuth, "/germplasm", GermplasmServerResource.class);
		attachToRouter(routerAuth, "/germplasm/{germplasmDbId}", GermplasmIndividualServerResource.class);
		attachToRouter(routerAuth, "/germplasm/{germplasmDbId}/mcpd", McpdServerResource.class);
		attachToRouter(routerAuth, "/germplasm/{germplasmDbId}/pedigree", PedigreeServerResource.class);
		attachToRouter(routerAuth, "/germplasm/{germplasmDbId}/progeny", ProgenyServerResource.class);
		attachToRouter(routerAuth, "/search/germplasm", SearchGermplasmServerResource.class);

		// GERMPLASM ATTRIBUTES
		attachToRouter(routerAuth, "/attributes", AttributeServerResource.class);
		attachToRouter(routerAuth, "/attributes/{attributeDbId}", AttributeIndividualServerResource.class);
		attachToRouter(routerAuth, "/attributes/categories", AttributeCategoryServerResource.class);
		attachToRouter(routerAuth, "/search/attributes", SearchAttributeServerResource.class);
		attachToRouter(routerAuth, "/attributevalues", AttributeValueServerResource.class);
		attachToRouter(routerAuth, "/attributevalues/{attributeValueDbId}", AttributeValueIndividualServerResource.class);
		attachToRouter(routerAuth, "/search/attributevalues", SearchAttributeValueServerResource.class);
	}

	private void attachToRouter(Router router, String url, Class<? extends ServerResource> clazz)
	{
		router.attach(urlPrefix + url, clazz);
		router.attach(urlPrefix + url + "/", clazz);
	}
}

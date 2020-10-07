package jhi.germinate.brapi.server;

import jhi.germinate.brapi.server.resource.phenotyping.observation.ObservationVariableServerResource;
import org.jooq.tools.StringUtils;
import org.restlet.resource.ServerResource;
import org.restlet.routing.Router;

import javax.servlet.http.HttpServletRequest;

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
import jhi.germinate.brapi.server.resource.other.GenotypeFileServerResource;
import jhi.germinate.server.auth.CustomVerifier;

/**
 * @author Sebastian Raubach
 */
public class Brapi
{
	public static Brapi BRAPI;

	public final  String         baseUrl;
	public final  String         urlPrefix;
	public final  String         hdf5BaseFolder;
	private final Router         routerAuth;
	private final Router         routerUnauth;
	private final CustomVerifier verifier;

	public Brapi(String baseUrl, String urlPrefix, Router routerAuth, Router routerUnauth, CustomVerifier verifier, String hdf5BaseFolder)
	{
		if (!StringUtils.isEmpty(baseUrl)) {
			if (baseUrl.endsWith("/"))
				baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
			this.baseUrl = baseUrl;
		} else {
			this.baseUrl = null;
		}
		this.urlPrefix = urlPrefix;
		this.routerAuth = routerAuth;
		this.routerUnauth = routerUnauth;
		this.verifier = verifier;
		this.hdf5BaseFolder = hdf5BaseFolder;

		BRAPI = this;

		init();
	}

	public static String getServerBase(HttpServletRequest req)
	{
		if (StringUtils.isEmpty(BRAPI.baseUrl))
		{
			String scheme = req.getScheme(); // http or https
			String serverName = req.getServerName(); // ics.hutton.ac.uk
			int serverPort = req.getServerPort(); // 80 or 8080 or 443
			String contextPath = req.getContextPath(); // /germinate-baz

			if (serverPort == 80 || serverPort == 443)
				return scheme + "://" + serverName + contextPath; // http://ics.hutton.ac.uk/germinate-baz
			else
				return scheme + "://" + serverName + ":" + serverPort + contextPath; // http://ics.hutton.ac.uk:8080/germinate-baz
		}
		else
		{
			return BRAPI.baseUrl;
		}
	}

	private void init()
	{
		// CORE
		attachToRouter(routerUnauth, "/serverinfo", ServerInfoResource.class);

		attachToRouter(routerAuth, "/commoncropnames", CropServerResource.class);

		attachToRouter(routerAuth, "/lists", ListServerResource.class);
		attachToRouter(routerAuth, "/lists/{listDbId}", ListIndividualServerResource.class);
		attachToRouter(routerAuth, "/lists/{listDbId}/items", ListModificationServerResource.class);
		attachToRouter(routerAuth, "/search/lists", SearchListServerResource.class);

		attachToRouter(routerAuth, "/locations", LocationServerResource.class);
		attachToRouter(routerAuth, "/locations/{locationDbId}", LocationIndividualServerResource.class);
		attachToRouter(routerAuth, "/search/locations", SearchLocationServerResource.class);

		attachToRouter(routerAuth, "/seasons", SeasonServerResource.class);
		attachToRouter(routerAuth, "/seasons/{seasonDbId}", SeasonIndividualServerResource.class);

		attachToRouter(routerAuth, "/studies", StudyServerResource.class);
		attachToRouter(routerAuth, "/studies/{studyDbId}", StudyIndividualServerResource.class);
		attachToRouter(routerAuth, "/search/studies", SearchStudyServerResource.class);
		attachToRouter(routerAuth, "/studytypes", StudyTypesServerResource.class);

		attachToRouter(routerAuth, "/trials", TrialServerResource.class);
		attachToRouter(routerAuth, "/trials/{trialDbId}", TrialIndividualServerResource.class);

		//GENOTYPING
		// TODO: write implementation
		attachToRouter(routerAuth, "/calls", CallServerResource.class);
		// TODO: write implementation
		attachToRouter(routerAuth, "/search/calls", SearchCallServerResource.class);

		attachToRouter(routerAuth, "/callsets", CallSetServerResource.class);
		attachToRouter(routerAuth, "/callsets/{callSetDbId}", CallSetIndividualServerResource.class);
		attachToRouter(routerAuth, "/callsets/{callSetDbId}/calls", CallSetCallServerResource.class);
		attachToRouter(routerAuth, "/search/callsets", SearchCallSetServerResource.class);

		attachToRouter(routerAuth, "/maps", MapServerResource.class);
		attachToRouter(routerAuth, "/maps/{mapDbId}", MapIndividualServerResource.class);
		attachToRouter(routerAuth, "/maps/{mapDbId}/linkagegroups", MapLinkageGroupServerResource.class);

		attachToRouter(routerAuth, "/markerpositions", MarkerPositionServerResource.class);
		attachToRouter(routerAuth, "/search/markerpositions", SearchMarkerPositionServerResource.class);

		attachToRouter(routerAuth, "/variants", VariantServerResource.class);
		attachToRouter(routerAuth, "/variants/{variantDbId}", VariantIndividualServerResource.class);
		attachToRouter(routerAuth, "/variants/{variantDbId}/calls", VariantCallServerResource.class);
		attachToRouter(routerAuth, "/search/variants", SearchVariantServerResource.class);

		attachToRouter(routerAuth, "/variantsets", VariantSetServerResource.class);
		attachToRouter(routerAuth, "/variantsets/{variantSetDbId}", VariantSetIndividualServerResource.class);
		attachToRouter(routerAuth, "/variantsets/{variantSetDbId}/calls", VariantSetCallServerResource.class);
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

		// PHENOTYPING
		attachToRouter(routerAuth, "/variables", ObservationVariableServerResource.class);

		// NON-STANDARD
		attachToRouter(routerAuth, "/files/genotypes/{datasetId}", GenotypeFileServerResource.class);
	}

	private void attachToRouter(Router router, String url, Class<? extends ServerResource> clazz)
	{
		router.attach(urlPrefix + url, clazz);
		router.attach(urlPrefix + url + "/", clazz);
	}
}

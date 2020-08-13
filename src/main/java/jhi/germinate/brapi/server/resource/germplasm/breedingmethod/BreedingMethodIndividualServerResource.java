package jhi.germinate.brapi.server.resource.germplasm.breedingmethod;

import org.restlet.resource.Get;

import uk.ac.hutton.ics.brapi.resource.base.BaseResult;
import uk.ac.hutton.ics.brapi.resource.germplasm.germplasm.BreedingMethod;
import uk.ac.hutton.ics.brapi.server.base.BaseServerResource;
import uk.ac.hutton.ics.brapi.server.germplasm.breedingmethod.BrapiBreedingMethodIndividualServerResource;

/**
 * @author Sebastian Raubach
 */
public class BreedingMethodIndividualServerResource extends BaseServerResource implements BrapiBreedingMethodIndividualServerResource
{
	@Get
	public BaseResult<BreedingMethod> getBreedingMethodById()
	{
		return new BaseResult<>(null, currentPage, pageSize, 0);
	}
}

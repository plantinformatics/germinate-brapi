package jhi.germinate.brapi.server.resource.germplasm.breedingmethod;

import jhi.germinate.brapi.resource.base.BaseResult;
import jhi.germinate.brapi.resource.germplasm.BreedingMethod;
import jhi.germinate.brapi.server.resource.BaseServerResource;

/**
 * @author Sebastian Raubach
 */
public class BreedingMethodIndividualServerResource extends BaseServerResource<BreedingMethod>
{
	@Override
	public BaseResult<BreedingMethod> getJson()
	{
		return new BaseResult<>(null, currentPage, pageSize, 0);
	}
}

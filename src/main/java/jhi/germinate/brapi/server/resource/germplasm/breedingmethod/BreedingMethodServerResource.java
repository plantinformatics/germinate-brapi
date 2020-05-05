package jhi.germinate.brapi.server.resource.germplasm.breedingmethod;

import jhi.germinate.brapi.resource.base.ArrayResult;
import jhi.germinate.brapi.resource.base.BaseResult;
import jhi.germinate.brapi.resource.germplasm.BreedingMethod;
import jhi.germinate.brapi.server.resource.BaseServerResource;

/**
 * @author Sebastian Raubach
 */
public class BreedingMethodServerResource extends BaseServerResource<ArrayResult<BreedingMethod>>
{
	@Override
	public BaseResult<ArrayResult<BreedingMethod>> getJson()
	{
		return new BaseResult<>(new ArrayResult<>(), currentPage, pageSize, 0);
	}
}

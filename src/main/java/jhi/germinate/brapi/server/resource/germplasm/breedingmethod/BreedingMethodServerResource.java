package jhi.germinate.brapi.server.resource.germplasm.breedingmethod;

import org.restlet.resource.Get;

import uk.ac.hutton.ics.brapi.resource.base.*;
import uk.ac.hutton.ics.brapi.resource.germplasm.germplasm.BreedingMethod;
import uk.ac.hutton.ics.brapi.server.base.BaseServerResource;
import uk.ac.hutton.ics.brapi.server.germplasm.breedingmethod.BrapiBreedingMethodServerResource;

/**
 * @author Sebastian Raubach
 */
public class BreedingMethodServerResource extends BaseServerResource implements BrapiBreedingMethodServerResource
{
	@Get
	public BaseResult<ArrayResult<BreedingMethod>> getBreedingMethods()
	{
		return new BaseResult<>(new ArrayResult<>(), currentPage, pageSize, 0);
	}
}

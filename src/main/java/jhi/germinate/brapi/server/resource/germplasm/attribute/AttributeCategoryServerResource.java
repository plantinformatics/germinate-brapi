package jhi.germinate.brapi.server.resource.germplasm.attribute;

import org.restlet.resource.Get;

import uk.ac.hutton.ics.brapi.resource.base.*;
import uk.ac.hutton.ics.brapi.resource.germplasm.attribute.Category;
import uk.ac.hutton.ics.brapi.server.base.BaseServerResource;
import uk.ac.hutton.ics.brapi.server.germplasm.attribute.BrapiAttributeCategoryServerResource;

/**
 * @author Sebastian Raubach
 */
public class AttributeCategoryServerResource extends BaseServerResource implements BrapiAttributeCategoryServerResource
{
	@Get
	public BaseResult<ArrayResult<Category>> getAttributeCategories()
	{
		return new BaseResult<>(null, currentPage, pageSize, 0);
	}
}

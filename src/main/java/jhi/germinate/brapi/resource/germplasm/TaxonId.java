package jhi.germinate.brapi.resource.germplasm;

/**
 * @author Sebastian Raubach
 */
public class TaxonId
{
	private String sourceName;
	private String taxonId;

	public String getSourceName()
	{
		return sourceName;
	}

	public TaxonId setSourceName(String sourceName)
	{
		this.sourceName = sourceName;
		return this;
	}

	public String getTaxonId()
	{
		return taxonId;
	}

	public TaxonId setTaxonId(String taxonId)
	{
		this.taxonId = taxonId;
		return this;
	}
}

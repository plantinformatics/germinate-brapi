package jhi.germinate.brapi.resource.attribute;

/**
 * @author Sebastian Raubach
 */
public class Category
{
	private String label;
	private String value;

	public String getLabel()
	{
		return label;
	}

	public Category setLabel(String label)
	{
		this.label = label;
		return this;
	}

	public String getValue()
	{
		return value;
	}

	public Category setValue(String value)
	{
		this.value = value;
		return this;
	}
}

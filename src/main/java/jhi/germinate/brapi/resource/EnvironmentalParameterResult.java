package jhi.germinate.brapi.resource;

/**
 * @author Sebastian Raubach
 */
public class EnvironmentalParameterResult
{
	private String description;
	private String parameterName;
	private String parameterPUI;
	private String unit;
	private String unitPUI;
	private String value;
	private String valuePUI;

	public String getDescription()
	{
		return description;
	}

	public EnvironmentalParameterResult setDescription(String description)
	{
		this.description = description;
		return this;
	}

	public String getParameterName()
	{
		return parameterName;
	}

	public EnvironmentalParameterResult setParameterName(String parameterName)
	{
		this.parameterName = parameterName;
		return this;
	}

	public String getParameterPUI()
	{
		return parameterPUI;
	}

	public EnvironmentalParameterResult setParameterPUI(String parameterPUI)
	{
		this.parameterPUI = parameterPUI;
		return this;
	}

	public String getUnit()
	{
		return unit;
	}

	public EnvironmentalParameterResult setUnit(String unit)
	{
		this.unit = unit;
		return this;
	}

	public String getUnitPUI()
	{
		return unitPUI;
	}

	public EnvironmentalParameterResult setUnitPUI(String unitPUI)
	{
		this.unitPUI = unitPUI;
		return this;
	}

	public String getValue()
	{
		return value;
	}

	public EnvironmentalParameterResult setValue(String value)
	{
		this.value = value;
		return this;
	}

	public String getValuePUI()
	{
		return valuePUI;
	}

	public EnvironmentalParameterResult setValuePUI(String valuePUI)
	{
		this.valuePUI = valuePUI;
		return this;
	}
}

package jhi.germinate.brapi.resource.base;

import java.util.List;

/**
 * @author Sebastian Raubach
 */
public class VariantCallResult<T>
{
	private List<T> data;
	private Boolean expandHomozygotes;
	private String  sepPhased;
	private String  sepUnphased;
	private String  unknownString;

	public List<T> getData()
	{
		return data;
	}

	public VariantCallResult<T> setData(List<T> data)
	{
		this.data = data;
		return this;
	}

	public Boolean getExpandHomozygotes()
	{
		return expandHomozygotes;
	}

	public VariantCallResult<T> setExpandHomozygotes(Boolean expandHomozygotes)
	{
		this.expandHomozygotes = expandHomozygotes;
		return this;
	}

	public String getSepPhased()
	{
		return sepPhased;
	}

	public VariantCallResult<T> setSepPhased(String sepPhased)
	{
		this.sepPhased = sepPhased;
		return this;
	}

	public String getSepUnphased()
	{
		return sepUnphased;
	}

	public VariantCallResult<T> setSepUnphased(String sepUnphased)
	{
		this.sepUnphased = sepUnphased;
		return this;
	}

	public String getUnknownString()
	{
		return unknownString;
	}

	public VariantCallResult<T> setUnknownString(String unknownString)
	{
		this.unknownString = unknownString;
		return this;
	}
}

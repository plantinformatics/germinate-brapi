package jhi.germinate.brapi.resource.base;

import java.util.List;

/**
 * @author Sebastian Raubach
 */
public class ArrayResult<T>
{
	private List<T> data;

	public List<T> getData()
	{
		return data;
	}

	public ArrayResult<T> setData(List<T> data)
	{
		this.data = data;
		return this;
	}
}

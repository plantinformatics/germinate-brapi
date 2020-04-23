package jhi.germinate.brapi.resource;

/**
 * @author Sebastian Raubach
 */
public class ObservationLevelResult
{
	private String levelName;
	private Integer levelOrder;

	public String getLevelName()
	{
		return levelName;
	}

	public ObservationLevelResult setLevelName(String levelName)
	{
		this.levelName = levelName;
		return this;
	}

	public Integer getLevelOrder()
	{
		return levelOrder;
	}

	public ObservationLevelResult setLevelOrder(Integer levelOrder)
	{
		this.levelOrder = levelOrder;
		return this;
	}
}

package jhi.germinate.brapi.resource.season;

/**
 * @author Sebastian Raubach
 */
public class SeasonResult
{
	private String seasonDbId;
	private String seasonName;
	private int    year;

	public String getSeasonDbId()
	{
		return seasonDbId;
	}

	public SeasonResult setSeasonDbId(String seasonDbId)
	{
		this.seasonDbId = seasonDbId;
		return this;
	}

	public String getSeasonName()
	{
		return seasonName;
	}

	public SeasonResult setSeasonName(String seasonName)
	{
		this.seasonName = seasonName;
		return this;
	}

	public int getYear()
	{
		return year;
	}

	public SeasonResult setYear(int year)
	{
		this.year = year;
		return this;
	}
}

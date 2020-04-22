package jhi.germinate.brapi.resource;

import java.util.Map;

/**
 * @author Sebastian Raubach
 */
public class LinkageGroupResult
{
	private Map<String, String> additionalInfo;
	private String              linkageGroupName;
	private Integer             markerCount;
	private Long                maxPosition;

	public Map<String, String> getAdditionalInfo()
	{
		return additionalInfo;
	}

	public LinkageGroupResult setAdditionalInfo(Map<String, String> additionalInfo)
	{
		this.additionalInfo = additionalInfo;
		return this;
	}

	public String getLinkageGroupName()
	{
		return linkageGroupName;
	}

	public LinkageGroupResult setLinkageGroupName(String linkageGroupName)
	{
		this.linkageGroupName = linkageGroupName;
		return this;
	}

	public Integer getMarkerCount()
	{
		return markerCount;
	}

	public LinkageGroupResult setMarkerCount(Integer markerCount)
	{
		this.markerCount = markerCount;
		return this;
	}

	public Long getMaxPosition()
	{
		return maxPosition;
	}

	public LinkageGroupResult setMaxPosition(Long maxPosition)
	{
		this.maxPosition = maxPosition;
		return this;
	}
}

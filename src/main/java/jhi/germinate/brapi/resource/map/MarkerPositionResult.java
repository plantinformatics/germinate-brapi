package jhi.germinate.brapi.resource.map;

import java.util.Map;

/**
 * @author Sebastian Raubach
 */
public class MarkerPositionResult
{
	private Map<String, String> additionalInfo;
	private String              linkageGroupName;
	private String              mapDbId;
	private String              mapName;
	private Long                position;
	private String              variantDbId;
	private String              variantName;

	public Map<String, String> getAdditionalInfo()
	{
		return additionalInfo;
	}

	public MarkerPositionResult setAdditionalInfo(Map<String, String> additionalInfo)
	{
		this.additionalInfo = additionalInfo;
		return this;
	}

	public String getLinkageGroupName()
	{
		return linkageGroupName;
	}

	public MarkerPositionResult setLinkageGroupName(String linkageGroupName)
	{
		this.linkageGroupName = linkageGroupName;
		return this;
	}

	public String getMapDbId()
	{
		return mapDbId;
	}

	public MarkerPositionResult setMapDbId(String mapDbId)
	{
		this.mapDbId = mapDbId;
		return this;
	}

	public String getMapName()
	{
		return mapName;
	}

	public MarkerPositionResult setMapName(String mapName)
	{
		this.mapName = mapName;
		return this;
	}

	public Long getPosition()
	{
		return position;
	}

	public MarkerPositionResult setPosition(Long position)
	{
		this.position = position;
		return this;
	}

	public String getVariantDbId()
	{
		return variantDbId;
	}

	public MarkerPositionResult setVariantDbId(String variantDbId)
	{
		this.variantDbId = variantDbId;
		return this;
	}

	public String getVariantName()
	{
		return variantName;
	}

	public MarkerPositionResult setVariantName(String variantName)
	{
		this.variantName = variantName;
		return this;
	}
}

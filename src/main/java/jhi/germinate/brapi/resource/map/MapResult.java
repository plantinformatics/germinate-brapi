package jhi.germinate.brapi.resource.map;

import java.sql.Timestamp;
import java.util.Map;

/**
 * @author Sebastian Raubach
 */
public class MapResult
{
	private Map<String, String> additionalInfo = null;
	private String              comments;
	private String              commonCropName;
	private String              documentationURL;
	private Integer             linkageGroupCount;
	private String              mapDbId;
	private String              mapName;
	private String              mapPUI;
	private Integer             markerCount;
	private Timestamp           publishedDate;
	private String              scientificName;
	private String              type;
	private String              unit;

	public Map<String, String> getAdditionalInfo()
	{
		return additionalInfo;
	}

	public MapResult setAdditionalInfo(Map<String, String> additionalInfo)
	{
		this.additionalInfo = additionalInfo;
		return this;
	}

	public String getComments()
	{
		return comments;
	}

	public MapResult setComments(String comments)
	{
		this.comments = comments;
		return this;
	}

	public String getCommonCropName()
	{
		return commonCropName;
	}

	public MapResult setCommonCropName(String commonCropName)
	{
		this.commonCropName = commonCropName;
		return this;
	}

	public String getDocumentationURL()
	{
		return documentationURL;
	}

	public MapResult setDocumentationURL(String documentationURL)
	{
		this.documentationURL = documentationURL;
		return this;
	}

	public Integer getLinkageGroupCount()
	{
		return linkageGroupCount;
	}

	public MapResult setLinkageGroupCount(Integer linkageGroupCount)
	{
		this.linkageGroupCount = linkageGroupCount;
		return this;
	}

	public String getMapDbId()
	{
		return mapDbId;
	}

	public MapResult setMapDbId(String mapDbId)
	{
		this.mapDbId = mapDbId;
		return this;
	}

	public String getMapName()
	{
		return mapName;
	}

	public MapResult setMapName(String mapName)
	{
		this.mapName = mapName;
		return this;
	}

	public String getMapPUI()
	{
		return mapPUI;
	}

	public MapResult setMapPUI(String mapPUI)
	{
		this.mapPUI = mapPUI;
		return this;
	}

	public Integer getMarkerCount()
	{
		return markerCount;
	}

	public MapResult setMarkerCount(Integer markerCount)
	{
		this.markerCount = markerCount;
		return this;
	}

	public Timestamp getPublishedDate()
	{
		return publishedDate;
	}

	public MapResult setPublishedDate(Timestamp publishedDate)
	{
		this.publishedDate = publishedDate;
		return this;
	}

	public String getScientificName()
	{
		return scientificName;
	}

	public MapResult setScientificName(String scientificName)
	{
		this.scientificName = scientificName;
		return this;
	}

	public String getType()
	{
		return type;
	}

	public MapResult setType(String type)
	{
		this.type = type;
		return this;
	}

	public String getUnit()
	{
		return unit;
	}

	public MapResult setUnit(String unit)
	{
		this.unit = unit;
		return this;
	}
}

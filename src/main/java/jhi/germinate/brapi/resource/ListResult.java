package jhi.germinate.brapi.resource;

import java.sql.Timestamp;
import java.util.*;

import jhi.germinate.brapi.resource.base.Reference;

/**
 * @author Sebastian Raubach
 */
public class ListResult
{
	private Map<String, String> additionalInfo;
	private List<String>        data;
	private Timestamp           dateCreated;
	private Timestamp           dateModified;
	private List<Reference>     externalReferences;
	private String              listDbId;
	private String              listDescription;
	private String              listName;
	private String              listOwnerName;
	private String              listOwnerPersonDbId;
	private Long                listSize;
	private String              listSource;
	private String              listType;

	public Map<String, String> getAdditionalInfo()
	{
		return additionalInfo;
	}

	public ListResult setAdditionalInfo(Map<String, String> additionalInfo)
	{
		this.additionalInfo = additionalInfo;
		return this;
	}

	public List<String> getData()
	{
		return data;
	}

	public ListResult setData(List<String> data)
	{
		this.data = data;
		return this;
	}

	public Timestamp getDateCreated()
	{
		return dateCreated;
	}

	public ListResult setDateCreated(Timestamp dateCreated)
	{
		this.dateCreated = dateCreated;
		return this;
	}

	public Timestamp getDateModified()
	{
		return dateModified;
	}

	public ListResult setDateModified(Timestamp dateModified)
	{
		this.dateModified = dateModified;
		return this;
	}

	public List<Reference> getExternalReferences()
	{
		return externalReferences;
	}

	public ListResult setExternalReferences(List<Reference> externalReferences)
	{
		this.externalReferences = externalReferences;
		return this;
	}

	public String getListDbId()
	{
		return listDbId;
	}

	public ListResult setListDbId(String listDbId)
	{
		this.listDbId = listDbId;
		return this;
	}

	public String getListDescription()
	{
		return listDescription;
	}

	public ListResult setListDescription(String listDescription)
	{
		this.listDescription = listDescription;
		return this;
	}

	public String getListName()
	{
		return listName;
	}

	public ListResult setListName(String listName)
	{
		this.listName = listName;
		return this;
	}

	public String getListOwnerName()
	{
		return listOwnerName;
	}

	public ListResult setListOwnerName(String listOwnerName)
	{
		this.listOwnerName = listOwnerName;
		return this;
	}

	public String getListOwnerPersonDbId()
	{
		return listOwnerPersonDbId;
	}

	public ListResult setListOwnerPersonDbId(String listOwnerPersonDbId)
	{
		this.listOwnerPersonDbId = listOwnerPersonDbId;
		return this;
	}

	public Long getListSize()
	{
		return listSize;
	}

	public ListResult setListSize(Long listSize)
	{
		this.listSize = listSize;
		return this;
	}

	public String getListSource()
	{
		return listSource;
	}

	public ListResult setListSource(String listSource)
	{
		this.listSource = listSource;
		return this;
	}

	public String getListType()
	{
		return listType;
	}

	public ListResult setListType(String listType)
	{
		this.listType = listType;
		return this;
	}
}

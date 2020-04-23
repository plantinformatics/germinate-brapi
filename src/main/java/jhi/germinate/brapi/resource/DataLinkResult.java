package jhi.germinate.brapi.resource;

import java.net.URI;

/**
 * @author Sebastian Raubach
 */
public class DataLinkResult
{
	private String dataFormat;
	private String description;
	private String fileFormat;
	private String name;
	private String provenance;
	private String scientificType;
	private URI    url;
	private String version;

	public String getDataFormat()
	{
		return dataFormat;
	}

	public DataLinkResult setDataFormat(String dataFormat)
	{
		this.dataFormat = dataFormat;
		return this;
	}

	public String getDescription()
	{
		return description;
	}

	public DataLinkResult setDescription(String description)
	{
		this.description = description;
		return this;
	}

	public String getFileFormat()
	{
		return fileFormat;
	}

	public DataLinkResult setFileFormat(String fileFormat)
	{
		this.fileFormat = fileFormat;
		return this;
	}

	public String getName()
	{
		return name;
	}

	public DataLinkResult setName(String name)
	{
		this.name = name;
		return this;
	}

	public String getProvenance()
	{
		return provenance;
	}

	public DataLinkResult setProvenance(String provenance)
	{
		this.provenance = provenance;
		return this;
	}

	public String getScientificType()
	{
		return scientificType;
	}

	public DataLinkResult setScientificType(String scientificType)
	{
		this.scientificType = scientificType;
		return this;
	}

	public URI getUrl()
	{
		return url;
	}

	public DataLinkResult setUrl(URI url)
	{
		this.url = url;
		return this;
	}

	public String getVersion()
	{
		return version;
	}

	public DataLinkResult setVersion(String version)
	{
		this.version = version;
		return this;
	}
}

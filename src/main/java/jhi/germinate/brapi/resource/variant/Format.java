package jhi.germinate.brapi.resource.variant;

import java.net.URI;

/**
 * @author Sebastian Raubach
 */
public class Format
{
	private String dataFormat;
	private String fileFormat;
	private URI    fileURL;

	public String getDataFormat()
	{
		return dataFormat;
	}

	public Format setDataFormat(String dataFormat)
	{
		this.dataFormat = dataFormat;
		return this;
	}

	public String getFileFormat()
	{
		return fileFormat;
	}

	public Format setFileFormat(String fileFormat)
	{
		this.fileFormat = fileFormat;
		return this;
	}

	public URI getFileURL()
	{
		return fileURL;
	}

	public Format setFileURL(URI fileURL)
	{
		this.fileURL = fileURL;
		return this;
	}
}

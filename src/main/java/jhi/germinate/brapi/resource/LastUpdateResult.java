package jhi.germinate.brapi.resource;

import java.sql.Timestamp;

/**
 * @author Sebastian Raubach
 */
public class LastUpdateResult
{
	private Timestamp timestamp;
	private String    version;

	public Timestamp getTimestamp()
	{
		return timestamp;
	}

	public LastUpdateResult setTimestamp(Timestamp timestamp)
	{
		this.timestamp = timestamp;
		return this;
	}

	public String getVersion()
	{
		return version;
	}

	public LastUpdateResult setVersion(String version)
	{
		this.version = version;
		return this;
	}
}

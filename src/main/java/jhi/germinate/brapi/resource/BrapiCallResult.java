package jhi.germinate.brapi.resource;

import java.util.List;

import jhi.germinate.brapi.resource.base.BrapiCall;

/**
 * @author Sebastian Raubach
 */
public class BrapiCallResult
{
	private List<BrapiCall> calls;
	private String          contactEmail;
	private String          documentationURL;
	private String          location;
	private String          organizationName;
	private String          organizationURL;
	private String          serverDescription;
	private String          serverName;

	public List<BrapiCall> getCalls()
	{
		return calls;
	}

	public BrapiCallResult setCalls(List<BrapiCall> calls)
	{
		this.calls = calls;
		return this;
	}

	public String getContactEmail()
	{
		return contactEmail;
	}

	public BrapiCallResult setContactEmail(String contactEmail)
	{
		this.contactEmail = contactEmail;
		return this;
	}

	public String getDocumentationURL()
	{
		return documentationURL;
	}

	public BrapiCallResult setDocumentationURL(String documentationURL)
	{
		this.documentationURL = documentationURL;
		return this;
	}

	public String getLocation()
	{
		return location;
	}

	public BrapiCallResult setLocation(String location)
	{
		this.location = location;
		return this;
	}

	public String getOrganizationName()
	{
		return organizationName;
	}

	public BrapiCallResult setOrganizationName(String organizationName)
	{
		this.organizationName = organizationName;
		return this;
	}

	public String getOrganizationURL()
	{
		return organizationURL;
	}

	public BrapiCallResult setOrganizationURL(String organizationURL)
	{
		this.organizationURL = organizationURL;
		return this;
	}

	public String getServerDescription()
	{
		return serverDescription;
	}

	public BrapiCallResult setServerDescription(String serverDescription)
	{
		this.serverDescription = serverDescription;
		return this;
	}

	public String getServerName()
	{
		return serverName;
	}

	public BrapiCallResult setServerName(String serverName)
	{
		this.serverName = serverName;
		return this;
	}
}

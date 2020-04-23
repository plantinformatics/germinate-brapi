package jhi.germinate.brapi.resource;

/**
 * @author Sebastian Raubach
 */
public class ContactResult
{
	private String contactDbId;
	private String email;
	private String instituteName;
	private String name;
	private String orcid;
	private String type;

	public String getContactDbId()
	{
		return contactDbId;
	}

	public ContactResult setContactDbId(String contactDbId)
	{
		this.contactDbId = contactDbId;
		return this;
	}

	public String getEmail()
	{
		return email;
	}

	public ContactResult setEmail(String email)
	{
		this.email = email;
		return this;
	}

	public String getInstituteName()
	{
		return instituteName;
	}

	public ContactResult setInstituteName(String instituteName)
	{
		this.instituteName = instituteName;
		return this;
	}

	public String getName()
	{
		return name;
	}

	public ContactResult setName(String name)
	{
		this.name = name;
		return this;
	}

	public String getOrcid()
	{
		return orcid;
	}

	public ContactResult setOrcid(String orcid)
	{
		this.orcid = orcid;
		return this;
	}

	public String getType()
	{
		return type;
	}

	public ContactResult setType(String type)
	{
		this.type = type;
		return this;
	}
}

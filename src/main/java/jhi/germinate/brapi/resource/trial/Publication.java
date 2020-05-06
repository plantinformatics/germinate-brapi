package jhi.germinate.brapi.resource.trial;

/**
 * @author Sebastian Raubach
 */
public class Publication
{
	private String publicationPUI;
	private String publicationReference;

	public String getPublicationPUI()
	{
		return publicationPUI;
	}

	public Publication setPublicationPUI(String publicationPUI)
	{
		this.publicationPUI = publicationPUI;
		return this;
	}

	public String getPublicationReference()
	{
		return publicationReference;
	}

	public Publication setPublicationReference(String publicationReference)
	{
		this.publicationReference = publicationReference;
		return this;
	}
}

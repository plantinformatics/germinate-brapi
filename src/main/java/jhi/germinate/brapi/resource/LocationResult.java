package jhi.germinate.brapi.resource;

import java.util.*;

import jhi.germinate.brapi.resource.base.Reference;

/**
 * @author Sebastian Raubach
 */
public class LocationResult
{
	private String              abbreviation;
	private Map<String, String> additionalInfo = null;
	private String              coordinateDescription;
	private String              coordinateUncertainty;
	private Coordinates         coordinates;
	private String              countryCode;
	private String              countryName;
	private String              documentationURL;
	private String              environmentType;
	private String              exposure;
	private List<Reference>     externalReferences;
	private String              instituteAddress;
	private String              instituteName;
	private String              locationDbId;
	private String              locationName;
	private String              locationType;
	private String              siteStatus;
	private String              slope;
	private String              topography;

	public String getAbbreviation()
	{
		return abbreviation;
	}

	public LocationResult setAbbreviation(String abbreviation)
	{
		this.abbreviation = abbreviation;
		return this;
	}

	public Map<String, String> getAdditionalInfo()
	{
		return additionalInfo;
	}

	public LocationResult setAdditionalInfo(Map<String, String> additionalInfo)
	{
		this.additionalInfo = additionalInfo;
		return this;
	}

	public String getCoordinateDescription()
	{
		return coordinateDescription;
	}

	public LocationResult setCoordinateDescription(String coordinateDescription)
	{
		this.coordinateDescription = coordinateDescription;
		return this;
	}

	public String getCoordinateUncertainty()
	{
		return coordinateUncertainty;
	}

	public LocationResult setCoordinateUncertainty(String coordinateUncertainty)
	{
		this.coordinateUncertainty = coordinateUncertainty;
		return this;
	}

	public Coordinates getCoordinates()
	{
		return coordinates;
	}

	public LocationResult setCoordinates(Coordinates coordinates)
	{
		this.coordinates = coordinates;
		return this;
	}

	public String getCountryCode()
	{
		return countryCode;
	}

	public LocationResult setCountryCode(String countryCode)
	{
		this.countryCode = countryCode;
		return this;
	}

	public String getCountryName()
	{
		return countryName;
	}

	public LocationResult setCountryName(String countryName)
	{
		this.countryName = countryName;
		return this;
	}

	public String getDocumentationURL()
	{
		return documentationURL;
	}

	public LocationResult setDocumentationURL(String documentationURL)
	{
		this.documentationURL = documentationURL;
		return this;
	}

	public String getEnvironmentType()
	{
		return environmentType;
	}

	public LocationResult setEnvironmentType(String environmentType)
	{
		this.environmentType = environmentType;
		return this;
	}

	public String getExposure()
	{
		return exposure;
	}

	public LocationResult setExposure(String exposure)
	{
		this.exposure = exposure;
		return this;
	}

	public List<Reference> getExternalReferences()
	{
		return externalReferences;
	}

	public LocationResult setExternalReferences(List<Reference> externalReferences)
	{
		this.externalReferences = externalReferences;
		return this;
	}

	public String getInstituteAddress()
	{
		return instituteAddress;
	}

	public LocationResult setInstituteAddress(String instituteAddress)
	{
		this.instituteAddress = instituteAddress;
		return this;
	}

	public String getInstituteName()
	{
		return instituteName;
	}

	public LocationResult setInstituteName(String instituteName)
	{
		this.instituteName = instituteName;
		return this;
	}

	public String getLocationDbId()
	{
		return locationDbId;
	}

	public LocationResult setLocationDbId(String locationDbId)
	{
		this.locationDbId = locationDbId;
		return this;
	}

	public String getLocationName()
	{
		return locationName;
	}

	public LocationResult setLocationName(String locationName)
	{
		this.locationName = locationName;
		return this;
	}

	public String getLocationType()
	{
		return locationType;
	}

	public LocationResult setLocationType(String locationType)
	{
		this.locationType = locationType;
		return this;
	}

	public String getSiteStatus()
	{
		return siteStatus;
	}

	public LocationResult setSiteStatus(String siteStatus)
	{
		this.siteStatus = siteStatus;
		return this;
	}

	public String getSlope()
	{
		return slope;
	}

	public LocationResult setSlope(String slope)
	{
		this.slope = slope;
		return this;
	}

	public String getTopography()
	{
		return topography;
	}

	public LocationResult setTopography(String topography)
	{
		this.topography = topography;
		return this;
	}

	public static class Coordinates
	{
		private Geometry geometry;
		private String   type;

		public Geometry getGeometry()
		{
			return geometry;
		}

		public Coordinates setGeometry(Geometry geometry)
		{
			this.geometry = geometry;
			return this;
		}

		public String getType()
		{
			return type;
		}

		public Coordinates setType(String type)
		{
			this.type = type;
			return this;
		}
	}

	public static class Geometry
	{
		private double[] coordinates;
		private String   type;

		public double[] getCoordinates()
		{
			return coordinates;
		}

		public Geometry setCoordinates(double[] coordinates)
		{
			this.coordinates = coordinates;
			return this;
		}

		public String getType()
		{
			return type;
		}

		public Geometry setType(String type)
		{
			this.type = type;
			return this;
		}
	}
}
